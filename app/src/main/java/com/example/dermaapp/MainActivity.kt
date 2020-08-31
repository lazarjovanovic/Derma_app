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
import android.text.format.Formatter.formatIpAddress
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import it.sauronsoftware.ftp4j.FTPClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.Format
import java.util.*


class MainActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_MODE = 1001
    private val IMAGE_PICK_CODE = 1000
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(getApplicationContext());

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
            uploadtoftp(image_uri)

              // TEST HTTP 1
//            val rd = RequestData("a", "e", "i")
//            val json_data = JSONObject()
//            json_data.put("username", rd.usr)
//            json_data.put("password", rd.pwd)
//            json_data.put("image", rd.img)
//            val input: ByteArray = json_data.toString().encodeToByteArray()
//
//            //test 3
//            val url = URL("http://192.168.1.5:8004/do_POST")
//            //val url = URL("http://10.0.2.2:8004/do_POST")
//            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
//            con.requestMethod = "POST";
//            //con.setRequestProperty("Content-Type", "application/json; utf-8");
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setRequestProperty("Accept", "application/json");
//            con.doOutput = true;
//
//            CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
//
//            val thread = Thread(Runnable {
//                try {
//                    val os = con.outputStream
//                    val input_data: ByteArray = json_data.toString().encodeToByteArray()
//                    os.write(input_data, 0, input_data.size)
//                    os.flush()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            })
//
//            thread.start()

              // TEST HTTP 2
//            //"https://fierce-cove-29863.herokuapp.com/createAnUser"
//            AndroidNetworking.post("http://10.0.2.2:8004")
//                .addBodyParameter("firstname", "asd")
//                .addBodyParameter("lastname", "das")
//                .setTag("test")
//                .setPriority(Priority.MEDIUM)
//                .build()
//                .getAsJSONObject(object : JSONObjectRequestListener {
//                    override fun onResponse(response: JSONObject) {
//                        // do anything with response
//                    }
//
//                    override fun onError(error: ANError) {
//                        // handle error
//                    }
//                })
//            connectServer()

        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_view.setImageURI(data?.data)
            image_uri = data?.data
            uploadtoftp(image_uri)
        }
    }

    //accessing camera
    private fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New image")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

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

    fun postRequest(postUrl: String, postBody: RequestBody)
    {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(postUrl)
            .post(postBody)
            .build();
    }

    fun uploadtoftp(image_uri: Uri?){
        val thread = Thread(Runnable {
            try {
                val mFtpClient = FTPClient()
                //mFtpClient.connect("10.0.2.2", 8004)
                //mFtpClient.connect("178.220.215.98", 8004)
                mFtpClient.connect("192.168.1.5", 8004)
                mFtpClient.login("derma_app_user", "derma_pass_123")
                mFtpClient.type = FTPClient.TYPE_BINARY

                //if necessary, directly mounted to the defined folder on server
                //val currentdir = mFtpClient.currentDirectory()
                //mFtpClient.changeDirectory("test_folder/")

                //val path = getRealPathFromURI(image_uri)

                val path = getPath(image_uri)
                val path_parts = path?.split("/")
                val img_name = path_parts?.get(path_parts.size - 1)
                mFtpClient.upload(File(path.toString()))

                val wm: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val ip_addr: String = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress())

                val json_data = JSONObject()
                json_data.put("username", "test_usr")
                json_data.put("password", "test_pwd")
                json_data.put("image", img_name)
                json_data.put("method", "process")
                json_data.put("ip", ip_addr)

                val myFile = File(applicationContext.filesDir, "test.txt")
                myFile.writeText(json_data.toString())
                mFtpClient.upload(myFile)

                mFtpClient.disconnect(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (contentResolver != null) {
            val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            contentResolver.query(uri!!, projection, null, null, null)
                ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    fun connectServer() {
        val ipv4Address = "10.0.2.2"
        val portNumber = "8004"
        val postUrl = "http://$ipv4Address:$portNumber/"
        val postBodyText = "Hello"
        val mediaType: MediaType = MediaType.parse("text/plain; charset=utf-8")
        val postBody: RequestBody = RequestBody.create(mediaType, postBodyText)

        postRequest(postUrl, postBody)
    }
}