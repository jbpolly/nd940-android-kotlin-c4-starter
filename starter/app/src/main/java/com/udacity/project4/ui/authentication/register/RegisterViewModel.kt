package com.udacity.project4.ui.authentication.register

import androidx.lifecycle.*
import com.udacity.project4.core.utils.FirebaseUserLiveData
import com.udacity.project4.core.utils.isValidEmail
import com.udacity.project4.data.models.AuthenticationState

class RegisterViewModel : ViewModel() {

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val canRegister = MediatorLiveData<Boolean>()


    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    init {
        canRegister.addSource(email) {
            canRegister.value = !(email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || email.value?.isValidEmail == false)
        }

        canRegister.addSource(password) {
            canRegister.value = !(email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || email.value?.isValidEmail == false)
        }
    }


}