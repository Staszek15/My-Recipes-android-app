package com.staszek15.serveit.authorization

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.staszek15.serveit.R
import com.staszek15.serveit.databinding.FragmentRegisterBinding
import com.staszek15.serveit.home.MainActivity
import com.staszek15.serveit.loadingDialog
import com.staszek15.serveit.validatorRegister
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var credentialManager: CredentialManager

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

        credentialManager = CredentialManager.create(requireContext())
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
                    .addOnFailureListener { exception ->
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
            doGoogleSignIn()
        }
    }


    private fun getSignInWithGoogleOption(): GetSignInWithGoogleOption {
        return GetSignInWithGoogleOption.Builder(getString(R.string.WEB_CLIENT_ID))
            .build()
    }

    private fun doGoogleSignIn() {
        val googleSignRequest = GetCredentialRequest.Builder()
            .addCredentialOption(getSignInWithGoogleOption())
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = googleSignRequest,
                    context = requireContext())
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment", "Credential request failed", e)
            }
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        val loadingDialog = loadingDialog(requireContext())

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val googleIdToken = googleIdTokenCredential.idToken
                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                        val user = Firebase.auth.signInWithCredential(authCredential).await().user
                        user?.let {
                            loadingDialog?.dismiss()
                            Log.d("LoginFragment", "Signed in: ${it.email}")
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        loadingDialog?.dismiss()
                        Log.e("LoginFragment", "Invalid ID token", e)
                    } catch (e: Exception) {
                        loadingDialog?.dismiss()
                        Log.e("LoginFragment", "Firebase sign-in failed", e)
                    }
                }
            }
            else -> {
                loadingDialog?.dismiss()
                Log.e("LoginFragment", "Unexpected credential type")
            }
        }
    }


}