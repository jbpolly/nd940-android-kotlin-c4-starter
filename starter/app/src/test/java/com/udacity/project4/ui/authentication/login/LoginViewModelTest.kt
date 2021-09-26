package com.udacity.project4.ui.authentication.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.firebase.FirebaseApp
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class LoginViewModelTest : AutoCloseKoinTest() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var loginViewModel: LoginViewModel


    @Before
    fun setup() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        loginViewModel = LoginViewModel()
    }

    @Test
    fun givenEmail_whenInvalid_thenCanRegister_isFalse() {

        loginViewModel.password.value = "password"
        loginViewModel.email.value = ""
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isFalse()

        loginViewModel.email.value = null
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isFalse()

        loginViewModel.email.value = "invalid.email"
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isFalse()

    }

    @Test
    fun givenPassword_whenInvalid_thenCanRegister_isFalse() {

        loginViewModel.password.value = ""
        loginViewModel.email.value = "email@teste.com"
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isFalse()

        loginViewModel.password.value = null
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isFalse()

    }

    @Test
    fun givenPasswordAndEmail_whenBothValid_thenCanRegister_isTrue() {
        loginViewModel.password.value = "password"
        loginViewModel.email.value = "email@teste.com"
        Truth.assertThat(loginViewModel.canLogin.getOrAwaitValue()).isTrue()
    }


}