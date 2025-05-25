package com.staszek15.myrecipes.account

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.authorization.AuthorizationActivity
import com.staszek15.myrecipes.databinding.ActivitySettingsBinding
import androidx.core.net.toUri

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleClickListeners()
        binding.tvEmail.text = Firebase.auth.currentUser?.email.toString()
        binding.tvUid.text = Firebase.auth.currentUser?.uid.toString()
    }

    private fun handleClickListeners() {
        binding.btnSignOut.setOnClickListener {
            showLogOutDialog()
        }
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteDialog()
        }
        binding.btnEmail.setOnClickListener {
            val emailAddress = "mt.stasiak15@gmail.com"
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto: $emailAddress".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Sugar Alarm App email")
            }
            startActivity(emailIntent)
        }
        binding.btnGithub.setOnClickListener {
            val githubLink = "https://github.com/Staszek15"
            val githubIntent = Intent(Intent.ACTION_VIEW, githubLink.toUri())
            startActivity(githubIntent)
        }
    }

    private fun showLogOutDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Do you want to delete this account?")
            .setPositiveButton("Yes") { _, _ ->
                Firebase.auth.signOut()
                startActivity(Intent(this, AuthorizationActivity::class.java))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showDeleteDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Do you want to delete this account?")
            .setPositiveButton("Yes") { _, _ ->
                Firebase.auth.currentUser!!.delete()
                    .addOnSuccessListener {
                        startActivity(Intent(this, AuthorizationActivity::class.java))
                    }
                    .addOnFailureListener {
                        val snackbar = Snackbar.make(
                            binding.root,
                            "Failed to delete your account. Try again later.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar
                            .setAction("OK") {
                                snackbar.dismiss()
                            }
                            .show()
                    }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}