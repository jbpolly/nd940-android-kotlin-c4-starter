package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var localRemindersRepository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        localRemindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun saveRemindersAndGetReminders() = runBlocking {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        localRemindersRepository.saveReminder(reminder1)
        localRemindersRepository.saveReminder(reminder2)
        localRemindersRepository.saveReminder(reminder3)

        val result = localRemindersRepository.getReminders()
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).hasSize(3)

    }

    @Test
    fun saveRemindersAndGetReminderById() = runBlocking {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        localRemindersRepository.saveReminder(reminder1)
        localRemindersRepository.saveReminder(reminder2)
        localRemindersRepository.saveReminder(reminder3)

        val result = localRemindersRepository.getReminder(reminder2.id)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.id).isEqualTo(reminder2.id)
        assertThat(result.data.title).isEqualTo(reminder2.title)
        assertThat(result.data.description).isEqualTo(reminder2.description)
        assertThat(result.data.location).isEqualTo(reminder2.location)
        assertThat(result.data.latitude).isEqualTo(reminder2.latitude)
        assertThat(result.data.longitude).isEqualTo(reminder2.longitude)

    }

    @Test
    fun saveRemindersAndDeleteReminders() = runBlocking {

        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        localRemindersRepository.saveReminder(reminder1)
        localRemindersRepository.saveReminder(reminder2)
        localRemindersRepository.saveReminder(reminder3)

        var result = localRemindersRepository.getReminders()
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).hasSize(3)

        localRemindersRepository.deleteAllReminders()

        result = localRemindersRepository.getReminders()
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEmpty()

    }

    @Test
    fun saveReminders_getReminderByWrongId_returnsError() = runBlocking {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        localRemindersRepository.saveReminder(reminder1)
        localRemindersRepository.saveReminder(reminder2)
        localRemindersRepository.saveReminder(reminder3)

        val result = localRemindersRepository.getReminder("error_id")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.message).isEqualTo("Reminder not found!")

    }


}