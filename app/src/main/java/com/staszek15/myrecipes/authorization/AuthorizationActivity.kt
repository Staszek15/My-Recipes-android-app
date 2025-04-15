package com.staszek15.myrecipes.authorization

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAuthorizationBinding

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
            // TODO: uncomment App Check after release to Google Play
            //PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        handleClickListeners()
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser != null) {
            // TODO: uncomment auto log in
            //startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun handleClickListeners() {
        binding.buttonBack.setOnClickListener { findNavController(R.id.nav_host_fragment_container).navigateUp() }
    }
}