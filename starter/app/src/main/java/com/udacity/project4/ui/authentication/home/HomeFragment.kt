package com.udacity.project4.ui.authentication.home

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.data.models.AuthenticationState
import com.udacity.project4.databinding.FragmentHomeBinding


const val LOGIN_TAG = "login_firebase"
class HomeFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentHomeBinding

    private var firebaseAuthenticationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(
                    LOGIN_TAG, "Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                response?.let {
                    it.error?.errorCode
                } ?: run {

                }
                Log.d(LOGIN_TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }


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
            launchSignInFlow()
        }

        viewModel.authenticationState.observe(viewLifecycleOwner) { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    //go to reminder list
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToReminderListFragment())
                }
                else -> Log.e(
                    LOGIN_TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        }


    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        // Create and launch sign-in intent.
        firebaseAuthenticationLauncher.launch(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).setTheme(R.style.FirebaseLoginTheme)
                .build()
        )

    }


}