package com.example.dermaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.dermaapp.ui.login.LoginActivity
import java.io.File


class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent: Intent
        val filename = "userId.txt"
        val sd = File(Environment.getExternalStorageDirectory(), filename)
        val isAuthenticated = sd.exists()

        if (isAuthenticated) {
            intent = Intent(this@StartActivity, MainActivity::class.java)
        } else {
            intent = Intent(
                this@StartActivity,
                LoginActivity::class.java
            )
        }

        startActivity(intent)
        finish()
    }
}