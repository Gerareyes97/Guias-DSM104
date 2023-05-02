package com.example.login_dsm

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signOut :ImageView = findViewById(R.id.SignOutImageView)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }

        signOut.setOnClickListener {
            signOut()

        }

    }

    private fun signOut(){
        auth.signOut()
        val intent = Intent(this,SignInActivity::class.java)
        this.startActivity(intent)
    }

}