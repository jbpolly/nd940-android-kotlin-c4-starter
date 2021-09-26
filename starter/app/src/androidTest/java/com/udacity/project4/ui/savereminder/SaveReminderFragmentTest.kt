package com.udacity.project4.ui.savereminder


import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.ToastMatcher
import com.udacity.project4.core.utils.EspressoIdlingResource
import com.udacity.project4.data.FakeAndroidDataSource
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest: AutoCloseKoinTest() {

    private lateinit var repository: FakeAndroidDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Before
    fun setup() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            "juliana.teste@teste.com",
            "abc123"
        )
        repository = FakeAndroidDataSource()
        repository.addTasks(
            ReminderDTO(
                title = "Title1",
                description = "Description1",
                location = "Location1",
                latitude = 50.0,
                longitude = 30.0
            ),
            ReminderDTO(
                title = "Title2",
                description = "Description2",
                location = "Location2",
                latitude = 20.0,
                longitude = 20.0
            ),
            ReminderDTO(
                title = "Title3",
                description = "Description3",
                location = "Location3",
                latitude = 70.5,
                longitude = 10.3
            )
        )
        loadKoinModules(
            module(override = true) {
                single { repository }
            }
        )
    }


    /*
    * https://github.com/android/android-test/issues/803
    *
    * Toast message assertions not working with android 11 and target sdk 30
    * */
    @Test
    fun initializeSaveReminder_inputValidFields_saveSuccess() = runBlocking {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            it._viewModel.reminderSelectedLocationStr.postValue("Test Location")
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))

        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).perform(typeText("This is a test description"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).perform(click())

        ToastMatcher.onToast(R.string.reminder_saved).check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun initializeSaveReminder_inputInvalidFields_showSnackbar() = runBlocking {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))

        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).perform(typeText("This is a test description"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        scenario.close()
    }






}