package com.huawei.sujith.imageeditor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun onImageEditor(view: View) {
        val intent = Intent(this, ImageEditorActivity::class.java)
        startActivity(intent)
    }

    fun onStartAnimation(view: View) {
        val intent = Intent(this, ImageRenderActivity::class.java)
        startActivity(intent)
    }
}