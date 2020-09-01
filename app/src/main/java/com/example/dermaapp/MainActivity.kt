package com.example.dermaapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.text.format.Formatter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.squareup.okhttp.*
import it.sauronsoftware.ftp4j.FTPClient
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_MODE = 1001
    private val IMAGE_PICK_CODE = 1000
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(applicationContext);

        //button click
        capture_image_btn.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                {
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else{
                    openCamera()
                }
            }
            else{
                openCamera()
            }
        }

        image_from_memory_btn.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    //
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else{
                    pickImageFromGallery()
                }
            }
            else{
                pickImageFromGallery()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (grantResults.size == 4){
                        openCamera()
                    }
                    if (grantResults.size == 2){
                        pickImageFromGallery()
                    }
                }
                else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_MODE){
            image_view.setImageURI(image_uri)
            uploadToHttp(image_uri)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_view.setImageURI(data?.data)
            image_uri = data?.data
            uploadToHttp(image_uri)
        }
    }

    //accessing camera
    private fun openCamera(){
        val values = ContentValues()
        values.put(Images.Media.TITLE, "New image")
        values.put(Images.Media.DESCRIPTION, "From camera")
        image_uri = contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_MODE)
    }

    // get image from gallery
    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    fun uploadToHttp(image_uri: Uri?)
    {
        val path = getPath(image_uri)
        val file = File(path.toString())
        var url = "http://192.168.1.5:8004/do_POST"

        val req = RequestBody.create(MediaType.parse("image/png"), file)

        val request = Request.Builder()
            .url(url)
            .post(req)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                println("Faild to execute request")
            }

            override fun onResponse(response: Response?) {
                val body = response?.body()?.string()
                println(body)
            }
        })
    }

    fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(Images.Media.DATA)
        val cursor =
            contentResolver.query(uri!!, projection, null, null, null)
                ?: return null
        val column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }
}