package com.android.keepyournotes.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityWriteNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class WriteNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteNoteBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onEventHandle()

    }

    private fun init() {
        auth = Firebase.auth
        db =
            FirebaseDatabase.getInstance("https://keepyournotes-d3dc6-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data: DataSnapshot in dataSnapshot.children) {
                    val post = data.getValue<Note>()
                    val key = intent.getStringExtra("key")
                    if (post?.key == key) {
                        binding.etTitle.setText(post?.title.toString())
                        binding.etContent.setText(post?.content.toString())
                        binding.tvDate.text = post?.date.toString()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        db.child("notes").addValueEventListener(postListener)
    }

    @SuppressLint("SimpleDateFormat")
    private fun onEventHandle() {

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivSave.setOnClickListener {
            val sdf = SimpleDateFormat("EEE, d MMM yyyy")
            val currentDate = sdf.format(Date())

            val key = intent.getStringExtra("key")
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()
            val date = currentDate.toString()
            val creator = auth.currentUser?.uid.toString()

            if (key != null) {
                updateNote(key, title, content, date, creator)
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            }
        }

        binding.ivDelete.setOnClickListener {
            val key = intent.getStringExtra("key")
            if (key != null) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getText(R.string.are_you_sure))
                builder.setMessage(getText(R.string.your_note_will_be_delete))
                builder.setPositiveButton(getText(R.string.delete)) { dialog, _ ->
                    dialog.dismiss()
                    deleteNote(key)
                }.setNegativeButton(getText(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
            }
        }

    }

    private fun deleteNote(key: String) {
        db.child("notes").child(key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "deleteNote: success")
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java).also { intent ->
                    startActivity(intent)
                    finish()
                }
            } else {
                Log.e("TAG", "deleteNote: ${it.exception}")
            }
        }
    }

    private fun updateNote(
        key: String,
        title: String,
        content: String,
        date: String,
        creator: String
    ) {
        val noteInfo = mapOf<String, Any?>(
            "key" to key,
            "title" to title,
            "content" to content,
            "date" to date,
            "creator" to creator
        )
        db.child("notes").child(key).updateChildren(noteInfo).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "updateNote: success")
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            } else {
                Log.e("TAG", "updateNote: ${it.exception}")
            }
        }
    }

    data class Note(
        val key: String? = null,
        val title: String? = null,
        val content: String? = null,
        val date: String? = null,
        val creator: String? = null
    )

}