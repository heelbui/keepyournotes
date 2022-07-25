package com.android.keepyournotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        fAuth = Firebase.auth
        onEventHandle()

    }

    private fun onEventHandle() {
        binding.btnSignOut.setOnClickListener {
            fAuth.signOut()
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fAuth = Firebase.auth
        if (fAuth.currentUser == null) {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

}