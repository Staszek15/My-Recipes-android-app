package com.staszek15.myrecipes.authorization

import android.content.ContentValues.TAG
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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
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
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            findNavController().navigate(R.id.action_registerFragment_to_LogInFragment)
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        }
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
                } catch (e: GetCredentialException) {
                    Log.e("MainActivity", "GetCredentialException", e)
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)

                        // TODO: Send [googleIdTokenCredential.idToken] to your backend
                        // my added part
                        // Sign in to Firebase with using the token
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("MainActivity", "handleSignIn:", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e("MainActivity", "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e("MainActivity", "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        // this line is different from sign in
        Firebase.auth.signInWithCredential(firebaseCredential)
            // there was addOnCompleteListener(this) but idk why this as it was not used later on
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signUpWithGoogleCredential:success")
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signUpWithGoogleCredential:failure", task.exception)
                }
            }
    }


}