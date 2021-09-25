package com.udacity.project4.ui.authentication.register

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.udacity.project4.databinding.FragmentLoginBinding
import com.udacity.project4.databinding.FragmentRegisterBinding
import com.udacity.project4.ui.authentication.login.LoginViewModel
import com.udacity.project4.BR
import com.udacity.project4.R
import com.udacity.project4.core.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.ui.authentication.login.LoginFragment
import com.udacity.project4.ui.authentication.login.LoginFragmentDirections

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    private var firebaseAuthenticationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.d(
                    LoginFragment.TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                //authentication state will change?
            } else {
                Log.d(LoginFragment.TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.problem_register),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.setVariable(BR.viewModel, viewModel)
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            registerUserWithEmail()
        }

        binding.registerSocialMediaButton.setOnClickListener {
            registerUserWithSocialMedia()
        }

    }

    private fun registerUserWithEmail() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailField.text.toString().trim(), binding.passwordField.text.toString().trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToReminderListFragment())
                } else {
                    Toast.makeText(requireContext(), getString(R.string.problem_register), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUserWithSocialMedia() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        // Create and launch sign-in intent.
        firebaseAuthenticationLauncher.launch(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build()
        )
    }

}