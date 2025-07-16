package com.staszek15.serveit.authorization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.staszek15.serveit.R
import com.staszek15.serveit.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<MaterialButton>(R.id.button_back).visibility = View.INVISIBLE
        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.logIn.setOnClickListener { findNavController().navigate(R.id.action_welcomeFragment_to_LogInFragment) }
        binding.createAcc.setOnClickListener { findNavController().navigate(R.id.registerFragment) }
    }


}