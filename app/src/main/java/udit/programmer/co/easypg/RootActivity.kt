package udit.programmer.co.easypg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.home_app_bar_main.*
import kotlinx.android.synthetic.main.home_content_main.*
import kotlinx.android.synthetic.main.nav_header_layout.*
import udit.programmer.co.easypg.EditProfileFragments.CustomerEditFragment
import udit.programmer.co.easypg.Models.Customer

class RootActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var sideAppBarConfiguration: AppBarConfiguration

    private var currentCustomerProfileID = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentCustomerReference: DatabaseReference

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setSupportActionBar(home_toolbar)

        navController = findNavController(R.id.nav_host_fragment)

        sideAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.bottom_navigation_home,
                R.id.bottom_navigation_search,
                R.id.bottom_navigation_profile,
                R.id.nav_favourites,
                R.id.nav_web,
                R.id.nav_saved_msg,
                R.id.nav_invite,
                R.id.nav_user_guide,
                R.id.nav_feedback,
                R.id.nav_contact_us,
                R.id.nav_settings
            ), root_layout
        )
        setupActionBarWithNavController(navController, sideAppBarConfiguration)
        home_nav_view.setupWithNavController(navController)
        home_bottom_nav_view.setupWithNavController(navController)

        currentCustomerReference =
            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(currentCustomerProfileID)
        currentCustomerReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val customer = snapshot.getValue(Customer::class.java)
                if(customer!!.image != "")
                    Picasso.get().load(customer.image.toUri()).into(nav_customer_image)
                nav_customer_name.text = customer.name
                nav_customer_username.text = customer.username
            }
        })

    }

    override fun onOptionsItemSelected(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.nav_notification -> {
                Toast.makeText(this, "Notification Clicked", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.nav_share -> {
                Toast.makeText(this, "Share Clicked", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.nav_edit_profile -> {
                Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_LONG).show()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, CustomerEditFragment()).commit()
                return true
            }
            R.id.nav_logout -> {
                Toast.makeText(this, "Logout Clicked", Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.nav_help -> {
                Toast.makeText(this, "Help Clicked", Toast.LENGTH_LONG).show()
                return true
            }
        }
        return super.onOptionsItemSelected(it)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_toolbar_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(sideAppBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }
}
