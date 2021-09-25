package com.udacity.project4.ui.authentication.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.udacity.project4.data.models.AuthenticationState
import com.udacity.project4.databinding.FragmentHomeBinding
import com.udacity.project4.ui.authentication.login.LoginFragment

class HomeFragment: Fragment() {

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {

        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.loginButton.setOnClickListener {
            //go to login screen
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
        }

        binding.registerButton.setOnClickListener {
            //go to register screen
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRegisterFragment())
        }

        viewModel.authenticationState.observe(viewLifecycleOwner) { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    //go to reminder list
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToReminderListFragment())
                }
                else -> Log.e(
                    LoginFragment.TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        }


    }


}