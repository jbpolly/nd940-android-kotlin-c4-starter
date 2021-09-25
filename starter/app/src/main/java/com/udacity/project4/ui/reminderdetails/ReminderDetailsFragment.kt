package com.udacity.project4.ui.reminderdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.udacity.project4.core.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.databinding.FragmentReminderDetailsBinding
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataItem

class ReminderDetailsFragment: Fragment() {

    private lateinit var binding: FragmentReminderDetailsBinding
    private var reminderItem: ReminderDataItem? = null
    private val detailsArgs by navArgs<ReminderDetailsFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentReminderDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderItem = detailsArgs.reminder
        reminderItem?.let {
            binding.reminderTitle.text = it.title
            binding.reminderDescription.text = it.description
            binding.selectLocation.text = it.location
        }

    }

}