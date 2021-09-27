package com.udacity.project4

import android.app.Application
import android.content.Context
import android.view.autofill.AutofillManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.core.utils.EspressoIdlingResource
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.ui.locationreminders.RemindersActivity
import com.udacity.project4.ui.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import com.udacity.project4.util.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                ).apply {
                    reminderSelectedLocationStr.value = "Test Location"
                }
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
        //FirebaseAuth.getInstance().signOut()
        val autofillManager: AutofillManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(AutofillManager::class.java)
        autofillManager.disableAutofillServices()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            "juliana.teste@teste.com",
            "abc123"
        )

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun fromHome_goToLogin_enterApp_navigateToAdd() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        BaseRobot().assertOnView(
            withId(R.id.addReminderFAB),
            matches(ViewMatchers.isDisplayed())
        )

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        onView(withId(R.id.selectLocation))
            .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderTitle))
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))

        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.reminderDescription))
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("This is a test description"))

        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.saveReminder))
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        ToastMatcher.onToast(R.string.reminder_saved).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.addReminderFAB)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView)).check(
            RecyclerViewItemCountAssertion.withItemCount(
                1
            )
        )

        onView(withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            ViewMatchers.hasDescendant(withText("Title")), ViewActions.click()
        ))


        activityScenario.close()

    }


}
