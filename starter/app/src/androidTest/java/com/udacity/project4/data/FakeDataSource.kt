package com.udacity.project4.data

import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result

class FakeAndroidDataSource : ReminderDataSource {

    var remindersFakeDb: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(remindersFakeDb.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersFakeDb[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        remindersFakeDb[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find task")
    }

    override suspend fun deleteAllReminders() {
        remindersFakeDb.clear()
    }

    fun addTasks(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            remindersFakeDb[reminder.id] = reminder
        }
    }


}