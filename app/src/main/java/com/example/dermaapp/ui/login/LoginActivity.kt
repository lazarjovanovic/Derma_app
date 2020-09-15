package com.example.dermaapp.ui.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.dermaapp.MainActivity

import com.example.dermaapp.R

class LoginActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permission = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permission, 1000)
        }

        login.setOnClickListener {
            val loginState =
                LoginService.loginDataValidation(username.text.toString(), email.text.toString(), password.text.toString())
            if (!loginState.isDataValid) {
                if (loginState.usernameError != null) {
                    username.error = getString(loginState.usernameError!!)
                }
                if (loginState.emailError != null) {
                    email.error = getString(loginState.emailError!!)
                }
                if (loginState.passwordError != null) {
                    password.error = getString(loginState.passwordError!!)
                }
            } else {
                //TODO: Set loading
                //loading.isAnimating = true
                LoginService.loginRequest(username.text.toString(), email.text.toString(), password.text.toString())

                //Wait for response - TODO maybe some better approach
                while(LoginService.returnMessage == "")
                    Thread.sleep(2_000)

                if(LoginService.returnMessage != "" && LoginService.returnMessage != "success") {
                    Toast.makeText(this, LoginService.returnMessage, Toast.LENGTH_LONG).show()
                }
                else if(LoginService.returnMessage == "success") {
                    intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}