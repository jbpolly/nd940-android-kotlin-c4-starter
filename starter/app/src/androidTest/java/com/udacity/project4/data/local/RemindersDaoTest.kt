package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //use in memory builder
    //always close the database

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb(){
        database.close()
    }


    @Test
    fun saveRemindersAndGetReminders() = runBlockingTest {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val reminderList = database.reminderDao().getReminders()
        assertThat(reminderList).hasSize(3)
        assertThat(reminderList).contains(reminder1)
    }

    @Test
    fun saveRemindersAndGetReminderById() = runBlockingTest {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val reminder = database.reminderDao().getReminderById(reminder1.id)
        assertThat(reminder).isNotNull()
        assertThat(reminder?.id).isEqualTo(reminder1.id)
    }

    @Test
    fun saveReminders_getReminderByWrongId_returnsError() = runBlockingTest {
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val reminder = database.reminderDao().getReminderById("error_id")
        assertThat(reminder).isNull()
    }

    @Test
    fun saveRemindersAndDeleteReminders() = runBlockingTest{
        val reminder1 = ReminderDTO(title = "Title1", description = "Description1", location = "Location1", latitude = 50.0, longitude = 30.0)
        val reminder2 = ReminderDTO(title = "Title2", description = "Description2", location = "Location2", latitude = 20.0, longitude = 20.0)
        val reminder3 = ReminderDTO(title = "Title3", description = "Description3", location = "Location3", latitude = 70.5, longitude = 10.3)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        var reminderList = database.reminderDao().getReminders()
        assertThat(reminderList).hasSize(3)

        database.reminderDao().deleteAllReminders()
        reminderList = database.reminderDao().getReminders()
        assertThat(reminderList).isEmpty()
    }

    @Test
    fun getReminders_noRemindersSaved_returnsEmpty() = runBlockingTest {
        val reminderList = database.reminderDao().getReminders()
        assertThat(reminderList).isNotNull()
        assertThat(reminderList).isEmpty()
    }

}