package udit.programmer.co.easypg.LoginActivities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_p_g_sign_in.*
import udit.programmer.co.easypg.PGRootActivity
import udit.programmer.co.easypg.R

class PGSignInActivity : AppCompatActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    lateinit var email: String
    lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_p_g_sign_in)

        val sharedPreferences = getSharedPreferences("999", Context.MODE_PRIVATE)
        pg_email_et_signin.setText(sharedPreferences.getString("owneremail", ""))
        pg_password_et_signin.setText(sharedPreferences.getString("password", ""))

        pg_login_btn.setOnClickListener {
            if (pg_email_et_signin.text.toString().isNotEmpty()) {
                email = pg_email_et_signin.text.toString()
            }
            if (pg_password_et_signin.text.toString()
                    .isNotEmpty() || pg_password_et_signin.text.toString().length < 6
            ) {
                password = pg_password_et_signin.text.toString()
            }

            sharedPreferences.edit { putString("owneremail", email) }
            sharedPreferences.edit { putString("password", password) }

            pg_login_btn.isEnabled = false

            auth.signInWithEmailAndPassword(
                pg_email_et_signin.text.toString(),
                pg_password_et_signin.text.toString()
            ).addOnSuccessListener {
                startActivity(Intent(this@PGSignInActivity, PGRootActivity::class.java))
                finish()
            }.addOnFailureListener {
                Snackbar.make(
                    pg_signin_activity_layout,
                    "FAILED : $it",
                    Snackbar.LENGTH_LONG
                ).show()
                pg_login_btn.isEnabled = true
            }
        }

    }
}