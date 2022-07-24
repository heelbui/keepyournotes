package com.android.keepyournotes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.keepyournotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fAuth = Firebase.auth
        fAuth.signOut()
    }
}