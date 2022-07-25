package com.android.keepyournotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        googleSignIn()
        fAuth = Firebase.auth
        onEventHandle()

    }

    private companion object {
        private const val RC_SIGN_IN = 9001
    }

    private fun onEventHandle() {

        binding.tvForgotPassword.setOnClickListener {
            Intent(this, ForgotPasswordActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        binding.btnLogin.setOnClickListener {
            if (isValidEmail(binding.etEmail.text.toString()) &&
                isValidPassword(binding.etPassword.text.toString())
            ) {
                fAuth.fetchSignInMethodsForEmail(binding.etEmail.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            if (task.result.signInMethods.isNullOrEmpty()) {
                                binding.tvEmailError.visibility = View.VISIBLE
                                binding.tvEmailError.text =
                                    getText(R.string.this_email_not_registered)
                            } else {
                                binding.tvEmailError.visibility = View.GONE
                                fAuth.signInWithEmailAndPassword(
                                    binding.etEmail.text.toString(),
                                    binding.etPassword.text.toString()
                                ).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            getText(R.string.login_successful),
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        Intent(this, MainActivity::class.java).also { intent ->
                                            startActivity(intent)
                                            finish()
                                        }
                                    } else {
                                        binding.tvPasswordError.visibility = View.VISIBLE
                                        binding.tvPasswordError.text =
                                            getText(R.string.incorrect_password)
                                    }
                                }
                            }
                        } else {
                            Log.d("TAG", "fetchSignInMethodsForEmail: failed")
                        }
                    }
            }
        }

        binding.cardGoogleLogin.setOnClickListener {
            Log.d("TAG", "onCreate: GoogleSignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        binding.tvRegisterLogin.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.e("TAG", "onActivityResult: google Sign in failed ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credentialsApi = GoogleAuthProvider.getCredential(account?.idToken, null)
        fAuth.signInWithCredential(credentialsApi).addOnCompleteListener {
            val firebaseUser = fAuth.currentUser
            //get user info
            val uid = firebaseUser?.uid
            val email = firebaseUser?.email

            Log.d("TAG", "firebaseAuthWithGoogleAccount: $uid")
            Log.d("TAG", "firebaseAuthWithGoogleAccount: $email")

            // navigate to main
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }

        }.addOnFailureListener {
            Log.d("TAG", "firebaseAuthWithGoogleAccount: ${it.message}")
        }
    }

    private fun googleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
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