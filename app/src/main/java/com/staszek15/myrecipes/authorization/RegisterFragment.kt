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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentRegisterBinding
import com.staszek15.myrecipes.home.MainActivity
import com.staszek15.myrecipes.validatorRegister
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<MaterialButton>(R.id.button_back).visibility = View.VISIBLE
        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.createAcc.setOnClickListener {
            if (validatorRegister(binding.etEmail, binding.etPassword, binding.etConfirmPassword)) {
                Firebase.auth.createUserWithEmailAndPassword(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                )
                    .addOnSuccessListener {
                        Firebase.analytics.logEvent("sign_up", null)
                        val snackbar = Snackbar.make(
                            binding.root,
                            "Your account has been created. Please log in with your credentials.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") { snackbar.dismiss() }
                            .show()
                        findNavController().navigate(R.id.action_registerFragment_to_LogInFragment)
                    }
                    .addOnFailureListener {exception ->
                        FirebaseCrashlytics.getInstance().apply {
                            log("User registration failed")
                            setCustomKey("email", binding.etEmail.text.toString())
                            setCustomKey(
                                "errorMessage",
                                exception.localizedMessage ?: "Unknown error"
                            )
                            recordException(exception)
                        }
                        val bundle = Bundle().apply {
                            putString("error_type", exception::class.java.simpleName)
                            putString("error_message", exception.localizedMessage)
                        }
                        Firebase.analytics.logEvent("sign_up_failure", bundle)

                        val snackbar = Snackbar.make(
                            binding.root,
                            "Register failed. $exception",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") { snackbar.dismiss() }
                            .show()
                    }
            }
        }

        binding.googleRegister.setOnClickListener {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Query all google accounts on the device
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
                    Firebase.analytics.logEvent("google_sign_up", null)

                } catch (e: GetCredentialException) {
                    FirebaseCrashlytics.getInstance().apply {
                        log("Google sign-in failed")
                        setCustomKey("exception_type", e::class.java.simpleName)
                        setCustomKey("exception_message", e.localizedMessage ?: "Unknown error")
                        recordException(e)
                    }
                    val bundle = Bundle().apply {
                        putString("error_type", e::class.java.simpleName)
                        putString("message", e.localizedMessage)
                    }
                    Firebase.analytics.logEvent("google_sign_up_failure", bundle)
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Register failed. $e",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar
                        .setAction("OK") { snackbar.dismiss() }
                        .show()
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

                        // Sign in to Firebase with token
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)

                    } catch (e: GoogleIdTokenParsingException) {
                        FirebaseCrashlytics.getInstance().apply {
                            log("GoogleIdToken parsing failed in handleSignIn")
                            setCustomKey("exception_type", e::class.java.simpleName)
                            setCustomKey("message", e.localizedMessage ?: "Unknown error")
                            recordException(e)
                        }

                        val bundle = Bundle().apply {
                            putString("error_type", e::class.java.simpleName)
                            putString("stage", "google_id_token_parsing")
                        }
                        Firebase.analytics.logEvent("google_sign_up_failure", bundle)
                    }
                } else {
                    // Unexpected custom credential type
                    FirebaseCrashlytics.getInstance().apply {
                        log("Unexpected credential type in handleSignIn")
                        setCustomKey("credential_type", credential.type ?: "null")
                    }
                    Firebase.analytics.logEvent("google_sign_up_failure", Bundle().apply {
                        putString("stage", "unexpected_credential_type")
                    })
                }
            }

            else -> {
                // Completely unrecognized credential
                FirebaseCrashlytics.getInstance().log("Unknown credential in handleSignIn")
                Firebase.analytics.logEvent("google_sign_up_failure", Bundle().apply {
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
                    Firebase.analytics.logEvent("google_sign_up", null)
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val exception = task.exception
                    FirebaseCrashlytics.getInstance().apply {
                        log("Google register with Firebase failed")
                        setCustomKey("stage", "google_sign_up_firebase_auth")
                        setCustomKey("exception_type", exception?.javaClass?.simpleName ?: "null")
                        setCustomKey("message", exception?.localizedMessage ?: "Unknown error")
                        recordException(exception ?: Exception("Unknown register failure"))
                    }
                    Firebase.analytics.logEvent("google_sign_up_failure", Bundle().apply {
                        putString("stage", "firebase_auth_with_google")
                        putString("error_type", exception?.javaClass?.simpleName ?: "Unknown")
                    })
                    Toast.makeText(requireContext(), "Google sign-up failed.", Toast.LENGTH_LONG).show()
                }
            }
    }



}