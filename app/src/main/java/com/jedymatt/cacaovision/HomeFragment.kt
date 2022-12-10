package com.jedymatt.cacaovision

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment


class HomeFragment : Fragment() {

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imgUri ->
            if (imgUri != null) {
                val intent = Intent(requireContext(), ImageResultActivity::class.java)
                intent.putExtra("image_uri", imgUri)
                startActivity(intent)
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

    companion object {
        private val TAG = "HomeFragment"
    }
}















