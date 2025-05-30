package com.staszek15.myrecipes.authorization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentForgotPasswordBinding
import com.staszek15.myrecipes.validatorRemindPassword


class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.remindPass.setOnClickListener {
            if (validatorRemindPassword(binding.etEmail)) {
                Firebase.auth.sendPasswordResetEmail(binding.etEmail.text.toString())
                    .addOnSuccessListener {
                        val snackbar = Snackbar.make(
                            binding.root,
                            "You have been sent an email to help you reset your password.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") { snackbar.dismiss() }
                            .show()
                        Firebase.analytics.logEvent("remind_password", null)

                        findNavController().navigate(R.id.action_forgotPasswordFragment_to_LogInFragment)
                    }
                    .addOnFailureListener { exception ->
                        FirebaseCrashlytics.getInstance().apply {
                            log("sendPasswordResetEmail failed")
                            setCustomKey("stage", "remind_password")
                            setCustomKey("exception_type", exception::class.java.simpleName)
                            setCustomKey("email", binding.etEmail.text.toString())
                            recordException(exception)
                        }
                        Firebase.analytics.logEvent("remind_password_failure", Bundle().apply {
                            putString("error_type", exception::class.java.simpleName)
                        })
                        val snackbar = Snackbar.make(
                            binding.root,
                            "Remind password failed. Please check your email address and try again.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") { snackbar.dismiss() }
                            .show()
                    }
            }
        }

    }

}