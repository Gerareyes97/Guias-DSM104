package com.example.login_dsm

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val GOOGLE_SIGN_IN = 100



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val provider = OAuthProvider.newBuilder("github.com")

        auth = FirebaseAuth.getInstance()



        val signInEmail :EditText = findViewById(R.id.signInEmail)
        val signInPassword :EditText = findViewById(R.id.signInPassword)
        val signInPasswordLayout : TextInputLayout = findViewById(R.id.signInPasswordLayout)
        val signInBtn : Button = findViewById(R.id.SignInBtn)
        val signInProgressBar : ProgressBar = findViewById(R.id.signInProgressBar)
        val btnGoogle : Button = findViewById<Button>(R.id.SignInGoogleBtn)
        val btnGithub : Button = findViewById<Button>(R.id.SignInGithubBtn)



        val signUpText : TextView = findViewById(R.id.SignUpText)

        signUpText.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
        signInBtn.setOnClickListener {
            signInProgressBar.visibility = View.VISIBLE
            signInPasswordLayout.isPasswordVisibilityToggleEnabled = true

            val email = signInEmail.text.toString()
            val password = signInPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()){
                if (email.isEmpty()){
                    signInEmail.error = "Ingrese su correo electronico"
                }
                if (password.isEmpty()){
                    signInPassword.error = "Ingrese su contraseña"
                    signInPasswordLayout.isPasswordVisibilityToggleEnabled = false
                }

                signInProgressBar.visibility = View.GONE
                Toast.makeText(this,"Ingrese datos validos",Toast.LENGTH_SHORT).show()

            }else if (!email.matches(emailPattern.toRegex())){
                signInProgressBar.visibility = View.GONE
                signInEmail.error = "Ingrese un correo electronico valido"
                Toast.makeText(this,"Ingrese un correo electronico valido",Toast.LENGTH_SHORT).show()
            }else if (password.length < 6 ){
                signInPasswordLayout.isPasswordVisibilityToggleEnabled = false
                signInProgressBar.visibility = View.GONE
                signInPassword.error = "Ingrese una contraseña con mas de 6 caracteres"
                Toast.makeText(this,"Ingrese una contraseña con mas de 6 caracteres",Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this,"Algo ocurrio mal, intentalo de nuevo",Toast.LENGTH_SHORT).show()
                        signInProgressBar.visibility = View.GONE
                    }
                }
            }
        }

        //para google
        btnGoogle.setOnClickListener {
            btGoogle()
        }

        //Para Github
        btnGithub.setOnClickListener {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    // El usuario ha iniciado sesión correctamente
                    val user = auth.currentUser
                    Toast.makeText(this, "Iniciaste sesión como ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    // El inicio de sesión falló
                    Toast.makeText(this, "Error al iniciar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }



    }

    private fun btGoogle(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this,googleConf)

        googleClient.signOut().addOnCompleteListener {

            val signInIntent = googleClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                if (account!=null){
                    Log.d("Tag","googleId ${account.id}")
                    firebaseAuthWithGoogle(account.idToken!!)
                }else{
                    Toast.makeText(this,"El correo no existe",Toast.LENGTH_SHORT).show()
                }
            }catch (e:ApiException){
                Log.w("Tag","Google failed $e")
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d("Tag","sucess")
                    val user = auth.currentUser?.email.toString()
                    login(user)
                }else{
                    Log.w("Tag","failed",task.exception)
                    Toast.makeText(this,"No se pudo ingresar",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun login(email : String){
        val intent = Intent(this,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }


}