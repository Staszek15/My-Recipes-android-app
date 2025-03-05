package com.staszek15.myrecipes.authorization

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.FragmentLogInBinding
import com.staszek15.myrecipes.home.MainActivity


class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLogInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogInBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.logIn.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }
        binding.remindPass.setOnClickListener { findNavController().navigate(R.id.action_LogInFragment_to_forgotPasswordFragment) }
    }

}