package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanFragment : Fragment() {

    private val args: ScanFragmentArgs by navArgs()
    private lateinit var cameraView: SurfaceView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var scanNowButton: Button
    private var hasProcessed: Boolean = false

    // Explicitly specify the type for the permission launcher
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            if (checkCameraPermission()) {
                startCameraSource(holder)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraView = view.findViewById(R.id.camera_view)
        scanNowButton = view.findViewById(R.id.buttonScanNow)

        // Initialize the permission launcher with explicit type
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                cameraView.holder.addCallback(surfaceCallback)
            } else {
                Log.e("ScanFragment", "Camera permission not granted")
            }
        }

        setupBarcodeDetector()
        setupCameraSource() // Ensure CameraSource is set up before starting anything
        cameraView.holder.addCallback(surfaceCallback)

        scanNowButton.setOnClickListener {
            if (::barcodeDetector.isInitialized && ::cameraSource.isInitialized) {
                hasProcessed = false
                barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {

                    override fun release() {}

                    override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                        if (!hasProcessed) {
                            val barcodes = detections.detectedItems
                            if (barcodes.size() > 0) {
                                hasProcessed = true
                                val code = barcodes.valueAt(0).displayValue
                                Log.d("QR code value", code)

                                // Launch coroutine to process and save data
                                lifecycleScope.launch {
                                    try {
                                        val newItem = parseQrCode(code)
                                        saveItemToDb(newItem)  // Save to the server
                                    } catch (e: Exception) {
                                        Log.e("ScanFragment", "Error processing QR", e)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun setupBarcodeDetector() {
        barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        // Set the processor immediately after creating the detector
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                // We'll manage detections in the onViewCreated method
            }
        })

        if (!barcodeDetector.isOperational) {
            Log.w("ScanFragment", "Detector dependencies are not yet available.")
        }
    }

    private fun setupCameraSource() {
        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setRequestedPreviewSize(640, 480)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraSource(holder: SurfaceHolder) {
        try {
            if (::cameraSource.isInitialized && ::barcodeDetector.isInitialized) {
                if (checkCameraPermission()) {
                    cameraSource.start(holder)
                } else {
                    // Request permissions
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 0)
                }
            }
        } catch (e: Exception) {
            Log.e("ScanFragment", "Error starting camera source", e)
        }
    }

    // Function to parse the QR code into MyMapItem object
    private fun parseQrCode(code: String): DataItem {
        if (code.startsWith("<") && code.endsWith(">")) {
            val cleanedCode = code.removeSurrounding("<", ">")
            val parts = cleanedCode.split(";")
            if (parts.size == 4) {
                val name = parts[0]
                val value = parts[1]
                val location = parts[2]
                val serial_number = parts[3]

                return DataItem(name, value, location, serial_number)
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "QR code format is incorrect", Toast.LENGTH_SHORT).show()
                }
                throw IllegalArgumentException("QR code format is incorrect")
            }
        } else {
            requireActivity().runOnUiThread {
                Toast.makeText(context, "QR code format is incorrect (<> are missing)", Toast.LENGTH_SHORT).show()
            }
            throw IllegalArgumentException("QR code does not start and end with < and >")
        }
    }

    // Function to save the scanned item to the server dynamically
    private suspend fun saveItemToDb(item: DataItem) {
        try {
            // Here we will retrieve the currently open table from the shared preferences or some other source.
            val tableName = args.tableName // Replace this with dynamic table retrieval from UI/previous screen

            val response = RetrofitClient.instance.addItem(tableName, item)  // Assuming RetrofitClient has this method

            // Handle the response from the server
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Item saved successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate back after saving
                    Navigation.findNavController(requireView()).navigateUp()
                } else if (response.code() == 409) {
                    Toast.makeText(context, "Ошибка добавления. \nОбнаружен Дупликат.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error saving item", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
            Log.e("ScanFragment", "Error saving item", e)
        }
    }
}
