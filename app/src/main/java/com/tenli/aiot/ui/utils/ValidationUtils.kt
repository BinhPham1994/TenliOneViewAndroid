package com.tenli.aiot.ui.utils

object ValidationUtils {
    fun validateEmail(email: String): Boolean = email.isNotEmpty() && Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matches(email)

    fun isNotEmpty(vararg values: String): Boolean = values.all { it.isNotEmpty() }

    fun validatePassword(pw: String, confirm: String): Boolean = pw == confirm && pw.isNotEmpty()

    fun validateUserName(username: String): Boolean = username.isNotEmpty() && username.length < 15

    fun isIPAddress(ip: String): Boolean {
        val regex = Regex("((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}")
        return regex.matches(ip)
    }

    fun isValidUsername(username: String): Boolean {
        val usernameRegex = "^[a-zA-Z0-9_]{3,20}$".toRegex()
        return username.trim().matches(usernameRegex)
    }

    fun isPasswordStrong(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{6,20}$".toRegex()
        return password.matches(passwordRegex)
    }

    fun doPasswordsMatch(pw: String, confirm: String): Boolean {
        return pw == confirm
    }
}