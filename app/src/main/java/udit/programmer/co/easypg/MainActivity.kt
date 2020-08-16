package udit.programmer.co.easypg

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*
import udit.programmer.co.easypg.LoginActivities.PGSignInActivity
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.SignUpActivities.Customer.CustomerSignUp
import udit.programmer.co.easypg.SignUpActivities.PG.PGAddActivity

class MainActivity : AppCompatActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db by lazy {
        FirebaseDatabase.getInstance().reference
    }

    lateinit var email: String
    lateinit var password: String

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        pg_sign_in.setOnClickListener {
            startActivity(Intent(this, PGSignInActivity::class.java))
        }
        create_account.setOnClickListener {
            startActivity(Intent(this, CustomerSignUp::class.java))
        }
        add_pg.setOnClickListener {
            startActivity(Intent(this, PGAddActivity::class.java))
        }

        val sharedPreferences = getSharedPreferences("1000", Context.MODE_PRIVATE)
        email_input.setText(sharedPreferences.getString("email", ""))
        password_input.setText(sharedPreferences.getString("customerpassword", ""))

        login_btn.setOnClickListener {
            if (checkEmptyField()) {
                email = email_input.text.toString()
                password = password_input.text.toString()

                sharedPreferences.edit { putString("email", email) }
                sharedPreferences.edit { putString("customerpassword", password) }

                login_btn.isEnabled = false

                auth.signInWithEmailAndPassword(
                    email_input.text.toString(),
                    password_input.text.toString()
                ).addOnSuccessListener {
                    startActivity(Intent(this@MainActivity, RootActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Snackbar.make(
                        main_layout,
                        "FAILED : $it",
                        Snackbar.LENGTH_LONG
                    ).show()
                    login_btn.isEnabled = true
                }
            }
        }
    }

    private fun checkEmptyField(): Boolean {
        if (email_input.text.isNullOrBlank()) {
            email_input.error = "This field is Empty"
            return false
        }
        if (password_input.text.isNullOrBlank()) {
            password_input.error = "This field is Empty"
            return false
        }
        if (password_input.text.toString().length < 7) {
            password_input.error = "PassWord is too Short"
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            dialog = SpotsDialog.Builder().setContext(this).build()
            dialog.show()
            check()
        }
    }

    private fun check() {
        db.child("Customers").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ss in snapshot.children) {
                    var data = ss.getValue(Customer::class.java)
                    if (data!!.email == auth.currentUser!!.email) {
                        dialog.dismiss()
                        startActivity(Intent(this@MainActivity, RootActivity::class.java))
                        finish()
                    }
                }
            }
        })
        db.child("PGs").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ss in snapshot.children) {
                    var data = ss.getValue(PG::class.java)
                    if (data!!.ownerEmail == auth.currentUser!!.email) {
                        dialog.dismiss()
                        startActivity(Intent(this@MainActivity, PGRootActivity::class.java))
                        finish()
                    }
                }
            }
        })
    }

}