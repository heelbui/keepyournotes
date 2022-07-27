package com.android.keepyournotes.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.keepyournotes.R
import com.android.keepyournotes.adapter.NoteRecyclerAdapter
import com.android.keepyournotes.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var db: DatabaseReference

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: NoteRecyclerAdapter

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
        db =
            FirebaseDatabase.getInstance("https://keepyournotes-d3dc6-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfNotes: ArrayList<WriteNoteActivity.Note> = arrayListOf()
                var check = false
                for (data: DataSnapshot in dataSnapshot.children) {
                    val post = data.getValue<WriteNoteActivity.Note>()
                    if (post?.creator == fAuth.currentUser?.uid) {
                        post?.let { listOfNotes.add(it) }
                        check = true
                    }
                }
                if (!check) {
                    binding.coverMain.visibility = View.VISIBLE
                    binding.recyclerviewNote.visibility = View.GONE
                } else {
                    binding.coverMain.visibility = View.GONE
                    binding.recyclerviewNote.visibility = View.VISIBLE

                    linearLayoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = NoteRecyclerAdapter(listOfNotes)
                    binding.recyclerviewNote.layoutManager = linearLayoutManager
                    binding.recyclerviewNote.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        db.child("notes").addValueEventListener(postListener)

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
                R.id.item_change_name -> changeName()
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

        binding.floatingBtn.setOnClickListener {
            createNote()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun createNote() {
        val sdf = SimpleDateFormat("EEE, d MMM yyyy")
        val currentDate = sdf.format(Date())

        val title = ""
        val content = ""
        val date = currentDate.toString()
        val creator = fAuth.currentUser?.uid.toString()
        val key = db.child("notes").push().key.toString()
        val note = WriteNoteActivity.Note(key, title, content, date, creator)

        db.child("notes").child(key).setValue(note).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "createNote: success")
                Intent(this, WriteNoteActivity::class.java).also { intent ->
                    intent.putExtra("key", key)
                    startActivity(intent)
                }
            } else {
                Log.e("TAG", "createNote: ${it.exception}")
            }
        }
    }

    private fun changeName() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getText(R.string.change_name))
        val input = EditText(this)
        input.hint = getText(R.string.enter_your_new_name)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton(getText(R.string.ok)) { dialog, _ ->
            if (input.text.toString().isNotEmpty()) {
                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (data: DataSnapshot in dataSnapshot.children) {
                            val post = data.getValue<User>()
                            if (post?.email == fAuth.currentUser?.email) {
                                updateUser(
                                    data.key.toString(),
                                    input.text.toString(),
                                    fAuth.currentUser?.email.toString()
                                )
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                    }
                }
                db.child("users").addValueEventListener(postListener)

            } else dialog.dismiss()
        }
        builder.setNegativeButton(getText(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateUser(key: String, name: String, email: String) {
        val userInfo = mapOf<String, Any?>(
            "name" to name,
            "email" to email
        )
        db.child("users").child(key).updateChildren(userInfo).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "updateUser: success")
            } else {
                Log.e("TAG", "updateUser: ${it.exception}")
            }
        }
    }

    private fun createUserProfile(name: String, email: String) {
        val key = db.child("users").push().key.toString()
        val user = RegisterActivity.User(name.trim(), email.trim())
        db.child("users").child(key).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "createUserProfile: success")
                Toast.makeText(this, getText(R.string.updated), Toast.LENGTH_SHORT).show()
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