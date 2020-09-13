package com.example.dermaapp.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    var usernameError: Int? = null,
    var emailError: Int? = null,
    var passwordError: Int? = null,
    var isDataValid: Boolean = false
)