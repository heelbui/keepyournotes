package com.android.keepyournotes.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fAuth: FirebaseAuth

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        fAuth = Firebase.auth
        onEventHandle()

    }

    private companion object {
        const val EMAIL_ADDRESS = "huuhieu992001@gmail.com"
    }

    private fun onEventHandle() {
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_refresh -> Toast.makeText(this, getText(R.string.refreshing), Toast.LENGTH_LONG).show()
                R.id.item_language -> Toast.makeText(this, getText(R.string.language), Toast.LENGTH_LONG).show()
                R.id.item_sign_out -> signOut()
                R.id.item_change_pass -> Toast.makeText(this, getText(R.string.change_password), Toast.LENGTH_LONG).show()
                R.id.item_delete_account -> Toast.makeText(this, getText(R.string.delete_account), Toast.LENGTH_LONG).show()
                R.id.item_feedback -> sendFeedback()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val headerView = binding.navigationView.getHeaderView(0)
        val avatar = headerView.findViewById<ImageView>(R.id.drawer_avatar)
        val name = headerView.findViewById<TextView>(R.id.drawer_your_name)
        val email = headerView.findViewById<TextView>(R.id.drawer_email)
        avatar.setImageResource(R.drawable.logo_keep_your_notes_full)
        fAuth.currentUser?.let {
            name.text = it.displayName
            email.text = it.email
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendFeedback() {
        val subject = "Feedback related to KeepYourNotes"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$EMAIL_ADDRESS") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, EMAIL_ADDRESS)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun signOut() {
        fAuth.signOut()
        Toast.makeText(this, getText(R.string.signed_out), Toast.LENGTH_SHORT).show()
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else super.onBackPressed()
    }

}