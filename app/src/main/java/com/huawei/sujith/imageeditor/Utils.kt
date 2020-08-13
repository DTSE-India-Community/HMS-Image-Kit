
package com.huawei.sujith.imageeditor

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object Utils {
    private const val TAG = "Utils"

    /**
     * create demo dir
     *
     * @param dirPath dir path
     * @return result
     */
    fun createResourceDirs(dirPath: String?): Boolean {
        val dir = File(dirPath)
        return if (!dir.exists()) {
            if (dir.parentFile.mkdir()) {
                dir.mkdir()
            } else {
                dir.mkdir()
            }
        } else false
    }

    /**
     * copy assets folders to sdCard
     * @param context context
     * @param foldersName folderName
     * @param path path
     * @return result
     */
    fun copyAssetsFilesToDirs(
        context: Context,
        foldersName: String,
        path: String
    ): Boolean {
        try {
            val files = context.assets.list(foldersName)
            for (file in files!!) {
                if (!copyAssetsFileToDirs(
                        context,
                        foldersName + File.separator + file,
                        path + File.separator + file
                    )
                ) {
                    Log.e(
                        TAG,
                        "Copy resource file fail, please check permission"
                    )
                    return false
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
            return false
        }
        return true
    }

    /**
     * copy resource file to sdCard
     *
     * @param context  context
     * @param fileName fileName
     * @param path     sdCard path
     * @return result
     */
    fun copyAssetsFileToDirs(
        context: Context,
        fileName: String?,
        path: String?
    ): Boolean {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            inputStream = context.assets.open(fileName!!)
            val file = File(path)
            outputStream = FileOutputStream(file)
            val temp = ByteArray(4096)
            var n: Int
            while (-1 != inputStream.read(temp).also { n = it }) {
                outputStream.write(temp, 0, n)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
            return false
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (e: IOException) {
                        Log.e(TAG, e.message!!)
                    }
                }
            }
        }
        return true
    }
}