package com.udacity.project4.ui.authentication.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.core.utils.FirebaseUserLiveData
import com.udacity.project4.data.models.AuthenticationState

class HomeViewModel: ViewModel() {

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

}