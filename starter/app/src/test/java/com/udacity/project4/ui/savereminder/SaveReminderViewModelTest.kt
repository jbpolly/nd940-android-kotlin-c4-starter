package com.udacity.project4.ui.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.core.base.NavigationCommand
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config
import org.mockito.Mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderDataSource: FakeDataSource

    // tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())

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
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun setLocationInfo() {

        val testLatLng = LatLng(30.0, 20.0)
        val testPoi = PointOfInterest(testLatLng, "test_id", "Test Name")
        saveReminderViewModel.setLocationInfo(testLatLng, testPoi.name, testPoi)

        val latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        val longitude = saveReminderViewModel.longitude.getOrAwaitValue()
        val reminderStr = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val selectedPoi = saveReminderViewModel.selectedPOI.getOrAwaitValue()

        assertThat(latitude).isNotNull()
        assertThat(latitude).isEqualTo(30.0)

        assertThat(longitude).isNotNull()
        assertThat(longitude).isEqualTo(20.0)

        assertThat(reminderStr).isNotNull()
        assertThat(reminderStr).isEqualTo(testPoi.name)

        assertThat(selectedPoi).isNotNull()
        assertThat(selectedPoi).isEqualTo(testPoi)

    }

    @Test
    fun givenReminder_whenTitleIsNullOrEmpty_notValidate() {

        var reminderDataItem =
            ReminderDataItem(title = null, "Description", "Location", 30.0, 20.0, "test_id")
        var validate = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validate).isFalse()
        var showSnackBar = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar).isEqualTo(R.string.err_enter_title)

        reminderDataItem =
            ReminderDataItem(title = "", "Description", "Location", 30.0, 20.0, "test_id")
        validate = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validate).isFalse()
        showSnackBar = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar).isEqualTo(R.string.err_enter_title)

    }

    @Test
    fun givenReminder_whenLocationIsNullOrEmpty_notValidate() {
        var reminderDataItem =
            ReminderDataItem(title = "Title", "Description", null, 30.0, 20.0, "test_id")
        var validate = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validate).isFalse()
        var showSnackBar = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar).isEqualTo(R.string.err_select_location)

        reminderDataItem =
            ReminderDataItem(title = "Title", "Description", "", 30.0, 20.0, "test_id")
        validate = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validate).isFalse()
        showSnackBar = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar).isEqualTo(R.string.err_select_location)

    }

    @Test
    fun givenReminder_whenAllFieldsAreValid_validate() {
        val reminderDataItem =
            ReminderDataItem(title = "Title", "Description", "Location", 30.0, 20.0, "test_id")
        val validate = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validate).isTrue()
    }

    @Test
    fun givenReminder_saveReminder_success() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val reminderDataItem =
            ReminderDataItem(title = "Title", "Description", "Location", 30.0, 20.0, "test_id")

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue()).isEqualTo(context.getString(R.string.reminder_saved))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue()).isEqualTo(
            NavigationCommand.Back
        )
    }

    @Test
    fun givenReminder_validateAndSave_success() = runBlocking {
        val reminderDataItem =
            ReminderDataItem(title = "Title", "Description", "Location", 30.0, 20.0, "test_id")

        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        val reminder = saveReminderViewModel.dataSource.getReminder(reminderDataItem.id)

        assertThat(reminder).isNotNull()
        assertThat(reminder).isInstanceOf(Result.Success::class.java)
        reminder as Result.Success
        assertThat(reminder.data.id).isEqualTo(reminderDataItem.id)
    }

    @Test
    fun givenSetLocationInfo_whenClear_resetLiveData() {

        val testLatLng = LatLng(30.0, 20.0)
        val testPoi = PointOfInterest(testLatLng, "test_id", "Test Name")
        saveReminderViewModel.setLocationInfo(testLatLng, testPoi.name, testPoi)

        var latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        var longitude = saveReminderViewModel.longitude.getOrAwaitValue()
        var reminderStr = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        var selectedPoi = saveReminderViewModel.selectedPOI.getOrAwaitValue()

        assertThat(latitude).isNotNull()
        assertThat(latitude).isEqualTo(30.0)

        assertThat(longitude).isNotNull()
        assertThat(longitude).isEqualTo(20.0)

        assertThat(reminderStr).isNotNull()
        assertThat(reminderStr).isEqualTo(testPoi.name)

        assertThat(selectedPoi).isNotNull()
        assertThat(selectedPoi).isEqualTo(testPoi)

        saveReminderViewModel.onClear()

        latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        longitude = saveReminderViewModel.longitude.getOrAwaitValue()
        reminderStr = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        selectedPoi = saveReminderViewModel.selectedPOI.getOrAwaitValue()
        assertThat(latitude).isNull()
        assertThat(longitude).isNull()
        assertThat(reminderStr).isEmpty()
        assertThat(selectedPoi).isNull()

        val description = saveReminderViewModel.reminderDescription.getOrAwaitValue()
        val title = saveReminderViewModel.reminderTitle.getOrAwaitValue()
        assertThat(description).isEmpty()
        assertThat(title).isEmpty()

    }


}