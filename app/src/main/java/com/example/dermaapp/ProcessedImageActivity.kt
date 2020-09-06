package com.example.dermaapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.image_view
import kotlinx.android.synthetic.main.activity_processed_image.*

class ProcessedImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_processed_image)

        val data = intent.getStringExtra("data")
        val dicts_string = data?.substringAfter('[')?.substringBefore(']')
        val dicts = dicts_string?.split(',')
        //complete
        val image = intent.getStringExtra("image")
        val image_uri = Uri.parse(image);
        image_view.setImageURI(image_uri)
        textViewDesease.text = "asdasdasfasfsafsafsafsafsafasfasfsafasfasfsafasfsafasfasfasfas"
        textViewOthers.text = "others"
        textViewDescription.text = "description"
        textViewDiagnosticalProcedures.text = "procedures"
        val x = 5
    }
}