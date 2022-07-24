package com.android.keepyournotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
        onEventHandle()

    }

    private fun init() {
        fAuth = Firebase.auth
    }

    private fun onEventHandle() {

        binding.btnLogin.setOnClickListener {
            if (isValidEmail(binding.etEmail.text.toString()) &&
                isValidPassword(binding.etPassword.text.toString())
            ) {
                fAuth.signInWithEmailAndPassword(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, getText(R.string.login_successful), Toast.LENGTH_SHORT)
                            .show()
                        Intent(this, MainActivity::class.java).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getText(R.string.incorrect_email_or_password).toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Firebase", it.exception.toString())
                    }
                }
            }
        }

        binding.tvRegisterLogin.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = getText(R.string.please_enter_your_email)
            false
        } else {
            binding.tvEmailError.visibility = View.GONE
            true
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return if (password.isEmpty()) {
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.tvPasswordError.text = getText(R.string.please_enter_your_password)
            false
        } else {
            binding.tvPasswordError.visibility = View.GONE
            true
        }
    }

}