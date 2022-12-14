package com.spaceapp.presentation.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.spaceapp.core.common.Device
import com.spaceapp.core.common.EmailController
import com.spaceapp.core.common.MobileServiceType
import com.spaceapp.core.common.TaskResult
import com.spaceapp.domain.model.auth.Login
import com.spaceapp.domain.usecase.auth.LoginUseCase
import com.spaceapp.presentation.utils.SignUpResponseMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    application: Application
) : ViewModel() {

    var device : MobileServiceType = Device.mobileServiceType(context = application.applicationContext)

    private val _loginInputFieldState = MutableStateFlow<LoginInputFieldState>(LoginInputFieldState.Nothing)
    val loginInputFieldState = _loginInputFieldState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Nothing)
    val loginState = _loginState.asStateFlow()

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    fun login() = viewModelScope.launch(Dispatchers.IO) {
        if (checkLoginInfo()) {
            if(device == MobileServiceType.HMS) {
                loginUseCase.hmsAuth(
                    login = Login(
                        userEmail = email,
                        userPassword = password
                    )
                ).collect() { result ->
                    when (result) {
                        is TaskResult.Success -> {
                            _loginState.value = LoginState.Loading
                            result.data
                                ?.addOnSuccessListener {
                                    _loginState.value = LoginState.Success
                                }
                                ?.addOnFailureListener {
                                    _loginState.value = LoginState.Error(errorMessage = it.message ?: SignUpResponseMessages.error)
                                }
                        }
                        is TaskResult.Error -> {
                            _loginState.value = LoginState.Error(errorMessage = result.message ?: SignUpResponseMessages.error)
                        }
                    }
                }
            } else {
                loginUseCase.firebaseAuth(
                    login = Login(
                        userEmail = email,
                        userPassword = password
                    )
                ).collect() { result ->
                    when (result) {
                        is TaskResult.Success -> {
                            _loginState.value = LoginState.Loading
                            result.data
                                ?.addOnSuccessListener {
                                    if (checkUserEmailIsVerified()) {
                                        _loginState.value = LoginState.Success
                                    } else {
                                        _loginState.value = LoginState.Error(errorMessage = SignUpResponseMessages.unverified_email)
                                    }
                                }
                                ?.addOnFailureListener {
                                    _loginState.value = LoginState.Error(errorMessage = it.message ?: SignUpResponseMessages.error)
                                }
                        }
                        is TaskResult.Error -> {
                            _loginState.value = LoginState.Error(errorMessage = result.message ?: SignUpResponseMessages.error)
                        }
                    }
                }
            }
        }
    }

    // created for firebase auth
    private fun checkUserEmailIsVerified() : Boolean {
        return FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
    }

    private fun checkLoginInfo(): Boolean = checkEmail() && checkPassword()

    private fun checkEmail(): Boolean {
        return if (EmailController.emailController(email)) {
            _loginInputFieldState.value = LoginInputFieldState.Nothing
            true
        } else {
            _loginInputFieldState.value = LoginInputFieldState.Error(errorMessage = SignUpResponseMessages.valid_email)
            false
        }
    }

    private fun checkPassword(): Boolean {
        return if (password.length < 8) {
            _loginInputFieldState.value = LoginInputFieldState.Error(errorMessage = SignUpResponseMessages.password_length)
            false
        } else {
            _loginInputFieldState.value = LoginInputFieldState.Nothing
            true
        }
    }

    fun updateEmailField(newValue: String) {
        email = newValue
    }

    fun updatePasswordField(newValue: String) {
        password = newValue
    }

    fun resetLoginInputFieldState() {
        _loginInputFieldState.value = LoginInputFieldState.Nothing
    }

    // created for error card
    fun resetState() {
        _loginState.value = LoginState.Nothing
        email = ""
        password = ""
    }
}