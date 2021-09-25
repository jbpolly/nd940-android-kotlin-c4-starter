package com.udacity.project4.ui.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemindersBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()?.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
