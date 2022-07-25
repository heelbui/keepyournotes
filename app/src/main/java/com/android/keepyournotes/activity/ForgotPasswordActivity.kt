package com.android.keepyournotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        onEventHandle()

    }

    private fun onEventHandle() {
        binding.ivClose.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
        binding.editTxtEmail.doOnTextChanged { text, start, before, count ->
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                auth.fetchSignInMethodsForEmail(text.toString()).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        if (it.result.signInMethods.isNullOrEmpty()) {
                            binding.txtFieldEmail.isHelperTextEnabled = true
                            binding.txtFieldEmail.helperText =
                                getText(R.string.this_email_not_registered)
                            binding.txtFieldEmail.hintTextColor =
                                ContextCompat.getColorStateList(this, R.color.red)
                            binding.txtFieldEmail.boxStrokeColor =
                                ContextCompat.getColor(this, R.color.red)
                        } else {
                            binding.txtFieldEmail.isHelperTextEnabled = false
                            binding.txtFieldEmail.boxStrokeColor =
                                ContextCompat.getColor(this, R.color.green)
                            binding.txtFieldEmail.hintTextColor =
                                ContextCompat.getColorStateList(this, R.color.green)
                        }
                    } else {
                        Log.e("TAG", "fetchSignInMethodsForEmail: failed")
                    }
                }
            } else {
                binding.txtFieldEmail.isHelperTextEnabled = true
                binding.txtFieldEmail.boxStrokeColor = ContextCompat.getColor(this, R.color.red)
                binding.txtFieldEmail.hintTextColor =
                    ContextCompat.getColorStateList(this, R.color.red)
                binding.txtFieldEmail.helperText = getText(R.string.wrong_email_format)
            }
        }
        binding.btnContinue.setOnClickListener {
            if (binding.editTxtEmail.text?.isNotEmpty() == true) {
                binding.txtFieldEmail.isHelperTextEnabled = false
                binding.progressBtnContinue.visibility = View.VISIBLE
                binding.btnContinue.visibility = View.GONE
                sendConfirmationLink(binding.editTxtEmail.text.toString())
            } else {
                binding.txtFieldEmail.isHelperTextEnabled = true
                binding.txtFieldEmail.helperText = getText(R.string.please_enter_your_email)
                binding.txtFieldEmail.boxStrokeColor = ContextCompat.getColor(this, R.color.red)
                binding.txtFieldEmail.hintTextColor =
                    ContextCompat.getColorStateList(this, R.color.red)
            }
        }
    }

    private fun sendConfirmationLink(email: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, getText(R.string.email_sent), Toast.LENGTH_SHORT)
                            .show()
                        Log.d("TAG", email)
                        Intent(this, LoginActivity::class.java).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getText(R.string.something_wrong_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("TAG", "sendConfirmationLink: ${it.exception}")
                    }
                }
        }, 500)
    }

}