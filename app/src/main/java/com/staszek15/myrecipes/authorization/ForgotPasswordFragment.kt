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
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.EmailTextRule
import com.staszek15.myrecipes.EmptyTextRule
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentForgotPasswordBinding
import com.staszek15.myrecipes.validateRule
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
                        Firebase.analytics.logEvent("remind_password_event_success", null)
                        val snackbar = Snackbar.make(
                            binding.root,
                            "We have sent an email to help you reset your password.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") { snackbar.dismiss() }
                            .show()
                        findNavController().navigate(R.id.action_forgotPasswordFragment_to_LogInFragment)
                    }
                    .addOnFailureListener {
                        Firebase.analytics.logEvent("remind_password_event_failure", null)
                    }
            }
        }
    }

}