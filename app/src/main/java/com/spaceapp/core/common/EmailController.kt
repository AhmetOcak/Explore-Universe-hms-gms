package com.spaceapp.core.common

import android.util.Patterns.EMAIL_ADDRESS

object EmailController {

    fun emailController(email: String): Boolean = EMAIL_ADDRESS.matcher(email).matches()

}