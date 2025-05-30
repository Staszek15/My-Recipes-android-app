package com.staszek15.myrecipes.authorization

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentLogInBinding
import com.staszek15.myrecipes.home.MainActivity
import com.staszek15.myrecipes.loadingDialog
import com.staszek15.myrecipes.validatorLogIn
import kotlinx.coroutines.launch


class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLogInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<MaterialButton>(R.id.button_back).visibility = View.VISIBLE
        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.logIn.setOnClickListener {
            if (validatorLogIn(binding.etEmail, binding.etPassword)) {

                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val loadingDialog = loadingDialog(requireActivity())

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        loadingDialog?.dismiss()

                        Firebase.analytics.logEvent("login", null)

                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { exception ->
                        loadingDialog?.dismiss()

                        // Default message
                        var message = "Login failed. Please try again later."

                        // Handle known cases
                        if (exception is FirebaseAuthInvalidUserException || exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Incorrect email or password."
                        } else {
                            FirebaseCrashlytics.getInstance().apply {
                                log("Unexpected login failure")
                                setCustomKey("email", email)
                                setCustomKey("exception_type", exception::class.java.simpleName)
                                setCustomKey(
                                    "message",
                                    exception.localizedMessage ?: "Unknown error"
                                )
                                recordException(exception)
                            }
                        }

                        val bundle = Bundle().apply {
                            putString("error_type", exception::class.java.simpleName)
                            putString("error_message", exception.localizedMessage)
                        }
                        Firebase.analytics.logEvent("login_failure", bundle)

                        val snackbar = Snackbar.make(
                            binding.root,
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction("OK") { snackbar.dismiss() }.show()
                    }
            }
        }


        binding.remindPass.setOnClickListener { findNavController().navigate(R.id.action_LogInFragment_to_forgotPasswordFragment) }

        binding.googleLogIn.setOnClickListener {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true) // Only existing signed-up accounts
                .setServerClientId(getString(R.string.WEB_CLIENT_ID))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(requireContext())

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(requireContext(), request)
                    handleSignIn(result)

                    Toast.makeText(
                        requireContext(),
                        "Welcome! You logged in successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                    Firebase.analytics.logEvent("google_login", null)

                } catch (e: GetCredentialException) {
                    FirebaseCrashlytics.getInstance().apply {
                        log("Google login failed")
                        setCustomKey("exception_type", e::class.java.simpleName)
                        setCustomKey("error_message", e.localizedMessage ?: "Unknown")
                        recordException(e)
                    }

                    val bundle = Bundle().apply {
                        putString("error_type", e::class.java.simpleName)
                        putString("stage", "google_login")
                    }
                    Firebase.analytics.logEvent("google_login_failure", bundle)

                    Toast.makeText(
                        requireContext(),
                        "Google sign-in failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)

                    } catch (e: GoogleIdTokenParsingException) {
                        FirebaseCrashlytics.getInstance().apply {
                            log("GoogleIdToken parsing failed in handleSignIn")
                            setCustomKey("stage", "token_parsing")
                            setCustomKey("exception_type", e::class.java.simpleName)
                            setCustomKey("message", e.localizedMessage ?: "Unknown error")
                            recordException(e)
                        }
                        Firebase.analytics.logEvent("google_login_failure", Bundle().apply {
                            putString("error_type", e::class.java.simpleName)
                            putString("stage", "token_parsing")
                        })
                    }
                } else {
                    FirebaseCrashlytics.getInstance().apply {
                        log("Unexpected custom credential type in handleSignIn")
                        setCustomKey("stage", "unexpected_credential_type")
                        setCustomKey("credential_type", credential.type ?: "null")
                    }
                    Firebase.analytics.logEvent("google_login_failure", Bundle().apply {
                        putString("stage", "unexpected_credential_type")
                    })
                }
            }

            else -> {
                FirebaseCrashlytics.getInstance().log("Unrecognized credential in handleSignIn")
                Firebase.analytics.logEvent("google_login_failure", Bundle().apply {
                    putString("stage", "unknown_credential")
                })
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        Firebase.auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Firebase.analytics.logEvent("google_auth", null)
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val exception = task.exception
                    FirebaseCrashlytics.getInstance().apply {
                        log("Firebase signInWithCredential failed")
                        setCustomKey("stage", "firebase_auth_with_google")
                        setCustomKey("exception_type", exception?.javaClass?.simpleName ?: "null")
                        setCustomKey("message", exception?.localizedMessage ?: "Unknown error")
                        recordException(exception ?: Exception("Unknown Firebase auth failure"))
                    }
                    Firebase.analytics.logEvent("google_auth_failure", Bundle().apply {
                        putString("stage", "firebase_auth_with_google")
                        putString("error_type", exception?.javaClass?.simpleName ?: "Unknown")
                    })
                    Toast.makeText(requireContext(), "Google login failed.", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }


}