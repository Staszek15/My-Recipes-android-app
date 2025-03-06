package com.staszek15.myrecipes.authorization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.staszek15.myrecipes.EmailTextRule
import com.staszek15.myrecipes.EmptyTextRule
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
            if (validatorRemindPassword(binding.etEmail)) {}
        }
    }

    private fun validator(): Boolean {
        val isEmailValid = binding.etEmail.validateRule(
            rules = listOf(
                EmptyTextRule(),
                EmailTextRule()
            )
        )
        return isEmailValid
    }
}