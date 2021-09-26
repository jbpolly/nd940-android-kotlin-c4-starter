package com.udacity.project4.ui.locationreminders.reminderslist

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.core.base.NavigationCommand
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [29])
class RemindersListViewModelTest : AutoCloseKoinTest() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var reminderDataSource: FakeDataSource


    @Before
    fun setup() {
        reminderDataSource = FakeDataSource()
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description1",
            location = "Location1",
            latitude = 50.0,
            longitude = 30.0
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description2",
            location = "Location2",
            latitude = 20.0,
            longitude = 20.0
        )
        val reminder3 = ReminderDTO(
            title = "Title3",
            description = "Description3",
            location = "Location3",
            latitude = 70.5,
            longitude = 10.3
        )
        reminderDataSource.addTasks(reminder1, reminder2, reminder3)
        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun givenDataSourceHaveReminders_loadReminders_success() {

        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        Truth.assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        Truth.assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isFalse()

        Truth.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).isNotNull()
        Truth.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).isNotEmpty()
        Truth.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).hasSize(3)

        Truth.assertThat(reminderListViewModel.showNoData.getOrAwaitValue()).isFalse()

    }

    @Test
    fun givenDataSourceHaveReminders_loadReminders_error() {
        reminderListViewModel.remindersList.value = listOf()
        (reminderListViewModel.dataSource as FakeDataSource).setReturnError(true)
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        Truth.assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        Truth.assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isFalse()

        Truth.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).isNotNull()
        Truth.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).isEmpty()
        Truth.assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
        Truth.assertThat(reminderListViewModel.showNoData.getOrAwaitValue()).isTrue()


    }

}