package com.huawei.sujith.imageeditor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVisionImpl
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageEditorActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private var permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val mRequestCode = 100
    val mImageCode = 101
    var mPermissionList: MutableList<String> = ArrayList()
    private var executorService: ExecutorService = Executors.newFixedThreadPool(1)
    private var imageView: ImageView? = null
    lateinit var seekBar: SeekBar
    private var string =
        "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}"
    private var authJson: JSONObject? = null
    private val context: Context? = null
    private var bitmap: Bitmap? = null
    private var initCodeState = -2
    private var stopCodeState = -2
    var imageVisionAPI: ImageVisionImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        imageView = findViewById(R.id.iv)
        seekBar = findViewById(R.id.seekBar)
        initAuthJson()
        initFilter(context)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progress = 0
            override fun onProgressChanged(
                seekBar: SeekBar,
                progresValue: Int,
                fromUser: Boolean
            ) {
                progress = progresValue
                startFilter(progress.toString(), "1", "1", authJson)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    private fun initAuthJson() {
        try {
            authJson = JSONObject(string)
        } catch (e: JSONException) {
            System.out.println(e)
        }
    }

    private fun initPermission() {
        mPermissionList.clear()
        for (i in permissions.indices) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permissions[i])
            }
        }
        if (mPermissionList.size > 0) {
            ActivityCompat.requestPermissions(this, permissions, mRequestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasPermissionDismiss = false
        if (mRequestCode == requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data) {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    mImageCode -> try {
                        val uri: Uri? = data.data
                        imageView!!.setImageURI(uri)
                        bitmap = (imageView!!.drawable as BitmapDrawable).bitmap
                    } catch (e: Exception) {
                        System.out.println(e)
                    }
                }
            }
        }
    }

    fun onImageUpload(view: View) {
        getPhoto(this)
    }

    fun getPhoto(act: Activity) {
        val getAlbum = Intent(Intent.ACTION_GET_CONTENT)
        val mimeTypes =
            arrayOf("image/jpeg", "image/png", "image/webp", "image/gif")
        getAlbum.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        getAlbum.type = "image/*"
        getAlbum.addCategory(Intent.CATEGORY_OPENABLE)
        act.startActivityForResult(getAlbum, mImageCode)
    }

    fun initFilter(context: Context?) {
        imageVisionAPI = ImageVision.getInstance(this)
        imageVisionAPI!!.setVisionCallBack(object : ImageVision.VisionCallBack {
            override fun onSuccess(successCode: Int) {
                val initCode = imageVisionAPI!!.init(context, authJson)
                Toast.makeText(this@ImageEditorActivity, "VisionAPI Success", Toast.LENGTH_SHORT)
                    .show()
                initCodeState = initCode
                stopCodeState = -2
            }

            override fun onFailure(errorCode: Int) {
                Toast.makeText(
                    this@ImageEditorActivity,
                    "VisionAPI failure, errorCode = $errorCode",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun startFilter(
        filterType: String,
        intensity: String,
        compress: String,
        authJson: JSONObject?
    ) {
        val runnable = Runnable {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            try {
                taskJson.put("intensity", intensity)
                taskJson.put("filterType", filterType)
                taskJson.put("compressRate", compress)
                jsonObject.put("requestId", "1")
                jsonObject.put("taskJson", taskJson)
                jsonObject.put("authJson", authJson)
                val visionResult = imageVisionAPI!!.getColorFilter(jsonObject, bitmap)
                imageView!!.post {
                    val image = visionResult.image
                    imageView!!.setImageBitmap(image)
                }
            } catch (e: JSONException) {
                Log.e(tag, "JSONException: " + e.message)
            }
        }
        executorService.execute(runnable)
    }

}