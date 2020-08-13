package com.huawei.sujith.imageeditor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.image.render.ImageRender
import com.huawei.hms.image.render.ImageRender.RenderCallBack
import com.huawei.hms.image.render.ImageRenderImpl
import com.huawei.hms.image.render.RenderView
import com.huawei.hms.image.render.ResultCode
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class ImageRenderActivity : AppCompatActivity() {

    val PERMISSION_CODE = 100
    var imageRenderApi: ImageRenderImpl? = null
    var sourcePath: String? = null
    val SOURCE_PATH = "sources"
    var frameLayout: FrameLayout? = null
    var spinner: Spinner? = null
    var authJson: JSONObject? = null
    var string =
        "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_render)
        sourcePath = filesDir.path + File.separator + SOURCE_PATH
        frameLayout = findViewById(R.id.content)
        spinner = findViewById(R.id.spinner_animations)
        initView()
        initAuthJson()
        initPermission()
    }

    private fun initView() {
        spinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                changeAnimation(spinner?.adapter?.getItem(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initAuthJson() {
        try {
            authJson = JSONObject(string)
        } catch (e: JSONException) {
            System.out.println(e)
        }
    }

    private fun initPermission() {
        val permissioncheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissioncheck == PackageManager.PERMISSION_GRANTED) {
            initData()
            initImageRender()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        }
    }

    private fun initImageRender() {
        ImageRender.getInstance(this, object : RenderCallBack {
            override fun onSuccess(imageRender: ImageRenderImpl) {
                showToast("ImageRenderAPI success")
                imageRenderApi = imageRender
                useImageRender()
            }
            override fun onFailure(i: Int) {
                showToast("ImageRenderAPI failure, errorCode = $i")
            }
        })
    }

    private fun useImageRender() {
        val initResult: Int = imageRenderApi!!.doInit(sourcePath, authJson)
        showToast("DoInit result == $initResult")
        if (initResult == 0) {
            val renderView: RenderView = imageRenderApi!!.getRenderView()
            if (renderView.resultCode == ResultCode.SUCCEED) {
                val view = renderView.view
                if (null != view) {
                    frameLayout!!.addView(view)
                }
            }
        } else {
            showToast("Do init fail, errorCode == $initResult")
        }
    }

    fun startAnimation(view: View?) {
        if (null != imageRenderApi) {
            val playResult: Int = imageRenderApi!!.playAnimation()
            if (playResult == ResultCode.SUCCEED) {
                showToast("animation success")
            }
        } else {
            showToast("animation fail, please init first.")
        }
    }

    private fun initData() {
        if (!Utils.createResourceDirs(sourcePath)) {
            showToast("Create dirs fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this, "AlphaAnimation" + File.separator + "ty.png",
                sourcePath + File.separator + "ty.png"
            )
        ) {
            showToast("Copy resource file fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this,
                "AlphaAnimation" + File.separator + "bj.jpg",
                sourcePath + File.separator + "bj.jpg"
            )
        ) {
            showToast("Copy resource file fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this,
                "AlphaAnimation" + File.separator + "manifest.xml",
                sourcePath + File.separator + "manifest.xml"
            )
        ) {
            showToast("Copy resource file fail, please check permission")
        }
    }

    private fun changeAnimation(filterType: String) {
        if (!Utils.copyAssetsFilesToDirs(this, filterType, sourcePath!!)) {
            showToast("copy files failure, please check permissions")
            return
        }
        if (imageRenderApi != null && frameLayout!!.childCount > 0) {
            frameLayout!!.removeAllViews()
            imageRenderApi!!.removeRenderView()
            val initResult: Int = imageRenderApi!!.doInit(sourcePath, authJson)
            showToast("DoInit result == $initResult")
            if (initResult == 0) {
                val renderView: RenderView = imageRenderApi!!.getRenderView()
                if (renderView.resultCode == ResultCode.SUCCEED) {
                    val view = renderView.view
                    if (null != view) {
                        frameLayout!!.addView(view)
                    }
                }
            } else {
                showToast("Do init fail, errorCode == $initResult")
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            this@ImageRenderActivity,
            text,
            Toast.LENGTH_SHORT
        ).show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData()
                initImageRender()
            } else {
                showToast("permission denied")
            }
        }
    }
}