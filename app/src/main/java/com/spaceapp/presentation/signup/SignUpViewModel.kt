package com.spaceapp.presentation.signup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.huawei.agconnect.auth.VerifyCodeSettings
import com.spaceapp.core.common.Device
import com.spaceapp.core.common.EmailController
import com.spaceapp.core.common.MobileServiceType
import com.spaceapp.core.common.TaskResult
import com.spaceapp.domain.model.auth.SignUp
import com.spaceapp.domain.model.auth.VerifyEmail
import com.spaceapp.domain.usecase.auth.SignUpUseCase
import com.spaceapp.domain.usecase.auth.VerifyUserEmailUseCase
import com.spaceapp.presentation.utils.SignUpResponseMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val verifyUserEmailUseCase: VerifyUserEmailUseCase,
    private val signUpUseCase: SignUpUseCase,
    application: Application
) : ViewModel() {

    var device: MobileServiceType = Device.mobileServiceType(context = application.applicationContext)

    private val _verifyEmailState = MutableStateFlow<VerifyEmailState>(VerifyEmailState.Nothing)
    val verifyEmailState = _verifyEmailState.asStateFlow()

    private val _signUpInputFieldState = MutableStateFlow<SignUpInputFieldState>(SignUpInputFieldState.Nothing)
    val inputFieldState = _signUpInputFieldState.asStateFlow()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val signUpState = _signUpState.asStateFlow()

    var userEmail by mutableStateOf("")
        private set
    var userPassword by mutableStateOf("")
        private set
    var userConfirmPassword by mutableStateOf("")
        private set

    var verifyCode by mutableStateOf("")
        private set

    fun verifyEmail() =
        viewModelScope.launch(Dispatchers.IO) {
            if (checkSignUpInfo()) {
                if (device == MobileServiceType.HMS) {
                    verifyUserEmailUseCase.hmsAuth(
                        verifyEmail = VerifyEmail(
                            userEmail = userEmail,
                            action = VerifyCodeSettings.ACTION_REGISTER_LOGIN
                        )
                    ).collect() { result ->
                        when (result) {
                            is TaskResult.Success -> {
                                _verifyEmailState.value = VerifyEmailState.Loading
                                result.data
                                    ?.addOnSuccessListener {
                                        _verifyEmailState.value = VerifyEmailState.Success
                                    }
                                    ?.addOnFailureListener {
                                        _verifyEmailState.value = VerifyEmailState.Error(
                                            errorMessage = it.message ?: SignUpResponseMessages.error
                                        )
                                    }
                            }
                            is TaskResult.Error -> {
                                _verifyEmailState.value = VerifyEmailState.Error(errorMessage = result.message)
                            }
                        }
                    }
                } else {
                    verifyUserEmailUseCase.firebaseAuth().collect() { result ->
                        when (result) {
                            is TaskResult.Success -> {
                                //_verifyEmailState.value = VerifyEmailState.Loading
                                result.data
                                    ?.addOnSuccessListener {
                                        FirebaseAuth.getInstance().signOut()
                                        _verifyEmailState.value = VerifyEmailState.Success
                                    }
                                    ?.addOnFailureListener {
                                        _verifyEmailState.value = VerifyEmailState.Error(
                                            errorMessage = it.message ?: SignUpResponseMessages.error
                                        )
                                    }
                            }
                            is TaskResult.Error -> {
                                _verifyEmailState.value = VerifyEmailState.Error(errorMessage = result.message)
                            }
                        }
                    }
                }
            }
        }

    fun signUp() = viewModelScope.launch(Dispatchers.IO) {
        if (device == MobileServiceType.HMS) {
            signUpUseCase.hmsAuth(
                signUp = SignUp(
                    email = userEmail,
                    password = userPassword,
                    verifyCode = verifyCode
                )
            ).collect() { result ->
                when (result) {
                    is TaskResult.Success -> {
                        _signUpState.value = SignUpState.Loading
                        result.data
                            ?.addOnSuccessListener {
                                _signUpState.value = SignUpState.Success
                            }
                            ?.addOnFailureListener {
                                _signUpState.value = SignUpState.Error(
                                    errorMessage = it.localizedMessage ?: SignUpResponseMessages.error
                                )
                            }
                    }
                    is TaskResult.Error -> {
                        _signUpState.value = SignUpState.Error(
                            errorMessage = result.message ?: SignUpResponseMessages.error
                        )
                    }
                }
            }
        } else {
            // In firebase we send email verification link
            // We do not receive any verification codes
            _verifyEmailState.value = VerifyEmailState.Success

            signUpUseCase.firebaseAuth(
                signUp = SignUp(
                    email = userEmail,
                    password = userPassword,
                    verifyCode = ""
                )
            ).collect() { result ->
                when (result) {
                    is TaskResult.Success -> {
                        _verifyEmailState.value = VerifyEmailState.Loading
                        result.data
                            ?.addOnSuccessListener {
                                // if signup success we send a email verification link
                                verifyEmail()
                            }
                            ?.addOnFailureListener {
                                _signUpState.value = SignUpState.Error(
                                    errorMessage = it.localizedMessage ?: SignUpResponseMessages.error
                                )
                            }
                    }
                    is TaskResult.Error -> {
                        _signUpState.value = SignUpState.Error(
                            errorMessage = result.message ?: SignUpResponseMessages.error
                        )
                    }
                }
            }
        }
    }

    private fun checkFields(): Boolean {
        return if (userEmail != "" && userPassword != "" && userConfirmPassword != "") {
            _signUpInputFieldState.value = SignUpInputFieldState.Nothing
            true
        } else {
            _signUpInputFieldState.value = SignUpInputFieldState.Error(errorMessage = SignUpResponseMessages.fill_fields)
            false
        }
    }

    private fun checkEmail(): Boolean {
        return if (EmailController.emailController(userEmail)) {
            _signUpInputFieldState.value = SignUpInputFieldState.Nothing
            true
        } else {
            _signUpInputFieldState.value = SignUpInputFieldState.Error(errorMessage = SignUpResponseMessages.valid_email)
            false
        }
    }

    private fun checkPassword(): Boolean {
        return if (userPassword.length < 8) {
            _signUpInputFieldState.value = SignUpInputFieldState.Error(errorMessage = SignUpResponseMessages.password_length)
            false
        } else {
            _signUpInputFieldState.value = SignUpInputFieldState.Nothing
            true
        }
    }

    private fun confirmPassword(): Boolean {
        return if (userPassword != userConfirmPassword) {
            _signUpInputFieldState.value = SignUpInputFieldState.Error(errorMessage = SignUpResponseMessages.password_match)
            false
        } else {
            true
        }
    }

    private fun checkSignUpInfo(): Boolean =
        checkFields() && checkEmail() && checkPassword() && confirmPassword()

    fun updateUserEmailField(newValue: String) {
        userEmail = newValue
    }

    fun updateUserPasswordField(newValue: String) {
        userPassword = newValue
    }

    fun updateUserConfirmPasswordField(newValue: String) {
        userConfirmPassword = newValue
    }

    fun updateVerifyCodeField(newValue: String) {
        verifyCode = newValue
    }

    // created for error card
    fun resetState() {
        _signUpState.value = SignUpState.Nothing
        _verifyEmailState.value = VerifyEmailState.Nothing
        userEmail = ""
        userPassword = ""
        userConfirmPassword = ""
        verifyCode = ""
    }
}