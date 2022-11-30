package com.jedymatt.cacaovision

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.camera_fragment_container, CameraFragment())
                .setReorderingAllowed(true)
                .commit()
        }
    }
}