package com.example.dermaapp.ui.login

import android.content.Context
import android.os.Environment
import android.util.Patterns
import android.widget.Toast
import com.example.dermaapp.R
import com.squareup.okhttp.*
import java.io.*
import java.security.AccessController.getContext
import java.util.concurrent.TimeUnit

class LoginService {
    companion object {
        var url = "http://192.168.1.5:8004/"
        //var url = "http://192.168.0.12:8004/"
        var returnMessage = ""
        fun loginDataValidation(username: String, email:String, password: String): LoginFormState {
            val state = LoginFormState()
            if (!isUserNameValid(username)) {
                state.usernameError = R.string.invalid_username
                state.isDataValid = false
            }
            if (!isEmailValid(email)) {
                state.emailError = R.string.invalid_email
                state.isDataValid = false
            }
            if (!isPasswordValid(password)) {
                state.passwordError = R.string.invalid_password
                state.isDataValid = false
            } else {
                state.isDataValid = true
            }
            return state
        }

        // A placeholder username validation check
        private fun isUserNameValid(username: String): Boolean {
            return !(username.contains('/') || username.contains('\'')) && username.length > 5
        }

        // A placeholder email validation check
        private fun isEmailValid(email : String): Boolean {
            return if (email.contains('@')) {
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            } else {
                false
            }
        }

        // A placeholder password validation check
        private fun isPasswordValid(password: String): Boolean {
            return password.length > 5
        }

        fun loginRequest(username: String, email:String, password: String)
        {
            val urlPath = url + "do_REGISTER_USER"
            val json =
                """{
                    "username":"${username}",
                    "email":"${email}",
                    "password":"${password}"
                }"""
                .trimIndent()

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
            val request = Request.Builder()
                .url(urlPath)
                .method("POST", body)
                .build()

            val client = OkHttpClient()
            client.setConnectTimeout(30, TimeUnit.SECONDS)
            client.setWriteTimeout(60, TimeUnit.SECONDS)
            client.setReadTimeout(60, TimeUnit.SECONDS)
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(request: Request?, e: IOException?) {
                    println("Faild to execute request")
                    returnMessage = "Failed to login. Please check your internet connection"
                }

                override fun onResponse(response: Response?) {
                    val body = response?.body()?.string()
                    println(body)
                    if(body == null || body == "")
                        returnMessage = "Username or email already exist!"
                    else
                        returnMessage = "success"

                    //in file userId.txt write user hashId from server, as proof that user is registered
                    val fileName = "userId.txt"
                    val file = File(Environment.getExternalStorageDirectory(), fileName)
                    var success = true
                    if (!file.exists())
                        success = file.createNewFile()

                    if (success) {
                        try {
                            PrintWriter(file).use { out -> out.println(body) }
                        } catch (e: Exception) {
                            print(e.message)
                        }
                    }
                }
            })
        }
    }
}