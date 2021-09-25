package com.udacity.project4.ui.authentication.login

import androidx.lifecycle.*
import com.udacity.project4.core.utils.FirebaseUserLiveData
import com.udacity.project4.core.utils.isValidEmail
import com.udacity.project4.data.models.AuthenticationState

class LoginViewModel : ViewModel() {

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val canLogin = MediatorLiveData<Boolean>()

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    init {
        canLogin.addSource(email) {
            canLogin.value = !(email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || email.value?.isValidEmail == false)
        }

        canLogin.addSource(password) {
            canLogin.value = !(email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || email.value?.isValidEmail == false)
        }
    }





}