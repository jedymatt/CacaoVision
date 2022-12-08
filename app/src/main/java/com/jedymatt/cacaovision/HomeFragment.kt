package com.jedymatt.cacaovision

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector


class HomeFragment : Fragment() {
    private val TAG = "HomeFragement"

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imgUri ->
            if (imgUri != null) {
                // get image from imgUri
                var image: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireContext().contentResolver, imgUri
                        )
                    ).copy(Bitmap.Config.ARGB_8888, true)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imgUri)
                }

                // TODO: Redirect to another activity that evaluates the image

                runObjectDetection(image)
            }
        }

    private fun runObjectDetection(bitmap: Bitmap) {

        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable: Boolean ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            val image = TensorImage.fromBitmap(bitmap)

            val options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(5)
                .setScoreThreshold(0.5f).build()
            val detector = ObjectDetector.createFromFileAndOptions(
                requireContext(), "mobilenetv1.tflite", options
            )

            val results = detector.detect(image)
            Log.i(TAG, results.toString())
        }.addOnFailureListener {
            Log.e(TAG, "TfLiteVision failed to initialize: " + it.message)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraButton = view.findViewById<Button>(R.id.camera_button)
        // camera on click
        cameraButton.setOnClickListener {
            // open camera
            activity?.let {
                val intent = Intent(it, CameraActivity::class.java)
                it.startActivity(intent)
            }
        }

        val browseGalleryButton = view.findViewById<Button>(R.id.browse_gallery_button)
        // browse gallery on click
        browseGalleryButton.setOnClickListener {
            fileChooser.launch("image/*")
        }
    }
}















