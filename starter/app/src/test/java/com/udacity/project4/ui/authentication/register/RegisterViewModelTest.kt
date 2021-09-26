package com.udacity.project4.ui.authentication.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
class RegisterViewModelTest : AutoCloseKoinTest() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setup() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        registerViewModel = RegisterViewModel()
    }

    @Test
    fun givenEmail_whenInvalid_thenCanRegister_isFalse() {

        registerViewModel.password.value = "password"
        registerViewModel.email.value = ""
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isFalse()

        registerViewModel.email.value = null
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isFalse()

        registerViewModel.email.value = "invalid.email"
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isFalse()

    }

    @Test
    fun givenPassword_whenInvalid_thenCanRegister_isFalse() {

        registerViewModel.password.value = ""
        registerViewModel.email.value = "email@teste.com"
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isFalse()

        registerViewModel.password.value = null
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isFalse()

    }

    @Test
    fun givenPasswordAndEmail_whenBothValid_thenCanRegister_isTrue() {
        registerViewModel.password.value = "password"
        registerViewModel.email.value = "email@teste.com"
        assertThat(registerViewModel.canRegister.getOrAwaitValue()).isTrue()
    }


}