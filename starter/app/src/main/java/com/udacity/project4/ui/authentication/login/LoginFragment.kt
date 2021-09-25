package com.udacity.project4.ui.authentication.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.udacity.project4.BR
import com.udacity.project4.core.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.data.models.AuthenticationState
import com.udacity.project4.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    companion object {
        const val TAG = "Login"
    }

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    private var firebaseAuthenticationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.d(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                //authentication state will change?
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.d(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                //should go to register?
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.setVariable(BR.viewModel, viewModel)
        setDisplayHomeAsUpEnabled(true)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginSocialMediaButton.setOnClickListener {
            launchGoogleSignInFlow()
        }

        binding.enterButton.setOnClickListener {
            //go to register screen
            tryToLogUserIn()
        }

        viewModel.authenticationState.observe(viewLifecycleOwner) { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    //go to reminder list
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToReminderListFragment())
                }
                else -> Log.d(
                    TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        }

    }

    private fun tryToLogUserIn() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.emailField.text.toString().trim(), binding.passwordField.text.toString().trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //will it change the authentication state?
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            //user does not exist
                            Log.d(TAG, "user does not exist")
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Log.d(TAG, "password is incorrect")
                            Toast.makeText(requireContext(), "Invalid credentials. Check your information and try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

    }

    private fun launchGoogleSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        // Create and launch sign-in intent.
        firebaseAuthenticationLauncher.launch(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build()
        )

    }

}