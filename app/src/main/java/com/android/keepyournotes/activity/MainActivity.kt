package com.android.keepyournotes.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var db: DatabaseReference

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        init()
        onEventHandle()

    }

    private companion object {
        const val EMAIL_ADDRESS = "huuhieu992001@gmail.com"
    }

    private fun init() {
        fAuth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://keepyournotes-d3dc6-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
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
                R.id.item_refresh -> Toast.makeText(
                    this,
                    getText(R.string.refreshing),
                    Toast.LENGTH_LONG
                ).show()
                R.id.item_sign_out -> signOut()
                R.id.item_change_pass -> changePassword()
                R.id.item_delete_account -> deleteAccount()
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
        val user = fAuth.currentUser
        user?.let {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var check = false
                    for (data: DataSnapshot in dataSnapshot.children) {
                        val post = data.getValue<User>()
                        if (post?.email == user.email) {
                            name.text = post?.name
                            email.text = post?.email
                            check = true
                        }
                    }
                    if (!check) {
                        createUserProfile(user.displayName.toString(), user.email.toString())
                        name.text = user.displayName
                        email.text = user.email
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                }
            }
            db.child("users").addValueEventListener(postListener)
        }
    }

    private fun createUserProfile(name: String, email: String) {
        val key = db.child("users").push().key.toString()
        val user = RegisterActivity.User(name.trim(), email.trim())
        db.child("users").child(key).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "createUserProfile: success")
            } else {
                Log.e("TAG", "createUserProfile: ${it.exception}")
            }
        }
    }

    data class User(val name: String? = null, val email: String? = null)

    private fun deleteAccount() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getText(R.string.are_you_sure))
        builder.setMessage(getText(R.string.your_account_will_be_remove))
        builder.setPositiveButton(getText(R.string.delete)) { dialog, _ ->
            dialog.dismiss()
            val user = Firebase.auth.currentUser!!
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("TAG", "deleteAccount: deleted.")
                        fAuth.signOut()
                        Intent(this, LoginActivity::class.java).also {
                            startActivity(it)
                            finish()
                        }
                    } else {
                        Log.e("TAG", "deleteAccount: ${task.exception}")
                    }
                }
        }.setNegativeButton(getText(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    @SuppressLint("InflateParams")
    private fun changePassword() {
        val sheetView = layoutInflater.inflate(R.layout.layout_bottomsheet_changepassword, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.setCancelable(false)
        dialog.show()

        sheetView.findViewById<ImageView>(R.id.ivCloseCP).setOnClickListener {
            dialog.dismiss()
        }

        val currentPass = sheetView.findViewById<TextInputEditText>(R.id.etCurrentPassCP)
        val layoutCP = sheetView.findViewById<TextInputLayout>(R.id.tfCurrentPassCP)
        val newPass = sheetView.findViewById<TextInputEditText>(R.id.etNewPassCP)
        val layoutNP = sheetView.findViewById<TextInputLayout>(R.id.tfNewPassCP)
        val confirmPass = sheetView.findViewById<TextInputEditText>(R.id.etConfirmPassCP)
        val layoutCNP = sheetView.findViewById<TextInputLayout>(R.id.tfConfirmPassCP)
        val btnChange = sheetView.findViewById<Button>(R.id.btnChangeCP)

        currentPass.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (currentPass.text.toString().isEmpty()) {
                    layoutCP.isHelperTextEnabled = true
                    layoutCP.helperText = getText(R.string.please_enter_your_password)
                } else {
                    fAuth.signInWithEmailAndPassword(
                        fAuth.currentUser?.email.toString(),
                        currentPass.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            layoutCP.isHelperTextEnabled = false
                        } else {
                            layoutCP.isHelperTextEnabled = true
                            layoutCP.helperText = getText(R.string.wrong_password)
                        }
                    }
                }
            } else {
                layoutCP.isHelperTextEnabled = false
            }
        }

        newPass.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (newPass.text.toString().isEmpty()) {
                    layoutNP.isHelperTextEnabled = true
                    layoutNP.helperText = getText(R.string.please_enter_your_password)
                } else {
                    if (newPass.text.toString().length < 8) {
                        layoutNP.isHelperTextEnabled = true
                        layoutNP.helperText = getText(R.string.password_must_contain)
                    } else {
                        layoutNP.isHelperTextEnabled = false
                    }
                }
            } else {
                layoutNP.isHelperTextEnabled = false
            }
        }

        confirmPass.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (confirmPass.text.toString().isEmpty()) {
                    layoutCNP.isHelperTextEnabled = true
                    layoutCNP.helperText = getText(R.string.please_enter_your_password)
                } else {
                    if (confirmPass.text.toString() == newPass.text.toString()) {
                        layoutCNP.isHelperTextEnabled = true
                        layoutCNP.helperText = getText(R.string.password_not_match)
                    } else {
                        layoutCNP.isHelperTextEnabled = false
                    }
                }
            } else {
                layoutCNP.isHelperTextEnabled = false
            }
        }

        btnChange.setOnClickListener {
            btnChange.isEnabled = false
            if (currentPass.text.toString().isNotEmpty() &&
                newPass.text.toString().isNotEmpty() &&
                confirmPass.text.toString().isNotEmpty() &&
                newPass.text.toString().length >= 8 &&
                confirmPass.text.toString() == newPass.text.toString()
            ) {
                val user = Firebase.auth.currentUser
                val newPassword = newPass.text.toString()

                user!!.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "updatePassword: updated.")
                            Toast.makeText(
                                this,
                                getText(R.string.your_password_updated),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            dialog.dismiss()
                            fAuth.signOut()
                            Intent(this, LoginActivity::class.java).also {
                                startActivity(it)
                                finish()
                            }
                        } else {
                            Log.e("TAG", "updatePassword: ${task.exception}")
                            Toast.makeText(
                                this,
                                getText(R.string.something_wrong_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                            btnChange.isEnabled = true
                        }
                    }
            }
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