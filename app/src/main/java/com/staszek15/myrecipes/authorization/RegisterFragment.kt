package com.staszek15.myrecipes.authorization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentRegisterBinding
import com.staszek15.myrecipes.validatorRegister

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
                findNavController().navigate(R.id.action_registerFragment_to_LogInFragment)
            }
        }
    }


}