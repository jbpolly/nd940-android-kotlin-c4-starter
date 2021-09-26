package com.udacity.project4.ui.locationreminders

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.BaseRobot
import com.udacity.project4.R
import com.udacity.project4.core.utils.EspressoIdlingResource
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class RemindersActivityTest{

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        FirebaseAuth.getInstance().signOut()
    }

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

    @Test
    fun fromHome_goToLogin_enterApp_navigateToAdd(){

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText(R.string.welcome_to_nreminder_app)).check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).perform(click())

        onView(withId(R.id.login_title)).check(matches(isDisplayed()))

        onView(withId(R.id.email_field)).check(matches(isDisplayed()))
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("juliana.teste@teste.com"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.password_field)).check(matches(isDisplayed()))
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("abc123"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.enter_button)).perform(click())

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            "juliana.teste@teste.com",
            "abc123"
        )

        BaseRobot().assertOnView(withId(R.id.addReminderFAB), matches(isDisplayed()))

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))

        activityScenario.close()

    }

    @Test
    fun fromHome_goToRegister_registerEnabled(){

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText(R.string.welcome_to_nreminder_app)).check(matches(isDisplayed()))
        onView(withId(R.id.register_button)).perform(click())

        onView(withId(R.id.register_title)).check(matches(isDisplayed()))

        onView(withId(R.id.email_field)).check(matches(isDisplayed()))
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("juliana.teste@teste.com"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.password_field)).check(matches(isDisplayed()))
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("abc123"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.register_button)).perform(click())

        activityScenario.close()

    }

    @Test
    fun fromHome_goToLogin_goBackToHome(){

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText(R.string.welcome_to_nreminder_app)).check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).perform(click())

        onView(withId(R.id.login_title)).check(matches(isDisplayed()))

        onView(withId(R.id.email_field)).check(matches(isDisplayed()))
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("juliana.teste@teste.com"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.password_field)).check(matches(isDisplayed()))
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("abc123"))

        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())

        Espresso.pressBack()
        onView(withText(R.string.welcome_to_nreminder_app)).check(matches(isDisplayed()))

        activityScenario.close()

    }

}

