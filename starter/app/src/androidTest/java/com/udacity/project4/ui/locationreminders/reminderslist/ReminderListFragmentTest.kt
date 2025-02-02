package com.udacity.project4.ui.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import com.udacity.project4.data.FakeAndroidDataSource
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest

class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: FakeAndroidDataSource

    @Before
    fun setup() {
        stopKoin()//stop the original app koin
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

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                ).apply {
                    reminderSelectedLocationStr.value = "Test Location"
                }
            }
            single { repository as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

    }

//    @Test
//    fun test() {
//        //verify(viewModel.loadReminders())
//        //`when`(viewModel.loadReminders()).thenReturn()
//    }

    @Test
    fun initializeReminder_showRemindersList() {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {

            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.reminderssRecyclerView)).check(withItemCount(3))
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText("Title2")), ViewActions.click())
        )
        scenario.close()
    }

    @Test
    fun clickAddReminder_goToAddReminderScreen() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {

            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
        scenario.close()
    }

}