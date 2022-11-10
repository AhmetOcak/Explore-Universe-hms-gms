package com.spaceapp.presentation.forgot_password

sealed interface ForgotPasswordState {
    object Nothing : ForgotPasswordState
    object Loading : ForgotPasswordState
    object Success : ForgotPasswordState
    data class Error(val errorMessage: String?) : ForgotPasswordState
}