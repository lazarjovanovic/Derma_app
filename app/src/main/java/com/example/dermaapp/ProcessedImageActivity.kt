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
        val lists_string = data?.substringAfter('[')?.substringBeforeLast(']')
        val items = lists_string?.split("; ")
        var main_desease = ""
        var main_description = ""
        var main_validation = ""
        var possibilities = ""
        var i = 0
        if (items != null) {
            while (i < items.size) {
                val pom_str = items[i]
                val pom_str_parts = pom_str.substringAfter('[').substringBefore(']').split(", ")
                if (i == 0)
                {
                    main_desease = pom_str_parts[0].plus(" - ").plus(pom_str_parts[1]).plus("%")
                    main_description = pom_str_parts[2]
                    main_validation = pom_str_parts[3]
                }
                else {
                    possibilities = possibilities.plus(pom_str_parts[0]).plus(" - ").plus(pom_str_parts[1]).plus("%\n")
                }
                i += 1
            }
        }
        val image = intent.getStringExtra("image")
        val image_uri = Uri.parse(image);
        image_view.setImageURI(image_uri)
        textViewDesease.text = main_desease.replace("\'", "")
        textViewOthers.text = possibilities.replace("\'", "")
        textViewDescription.text = main_description.replace("\'", "")
        textViewDiagnosticalProcedures.text = main_validation.replace("\'", "")
    }
}