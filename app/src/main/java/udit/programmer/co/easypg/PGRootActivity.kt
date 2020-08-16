package udit.programmer.co.easypg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_p_g_root.*
import kotlinx.android.synthetic.main.pg_home_app_bar_main.*
import kotlinx.android.synthetic.main.pg_home_content_main.*
import udit.programmer.co.easypg.EditProfileFragments.PGeditFragment

class PGRootActivity : AppCompatActivity() {
    private lateinit var pgBottomAppBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_g_root)

        setSupportActionBar(pg_home_toolbar)

        val pgNavController = findNavController(R.id.pg_nav_host_fragment)

        pgBottomAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.pg_bottom_navigation_home,
                R.id.pg_bottom_navigation_search,
                R.id.pg_bottom_navigation_profile
            ), pg_root_layout
        )
        //setupActionBarWithNavController(pgNavController, pgBottomAppBarConfiguration)
        pg_home_bottom_nav_view.setupWithNavController(pgNavController)

    }

    override fun onOptionsItemSelected(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.pg_nav_notification -> {
                Toast.makeText(this, "Notification Clicked", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.pg_nav_share -> {
                Toast.makeText(this, "Share Clicked", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.pg_nav_edit_profile -> {
                Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_LONG).show()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.pg_nav_host_fragment, PGeditFragment()).commit()
                return true
            }
            R.id.pg_nav_logout -> {
                Toast.makeText(this, "Logout Clicked", Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.pg_nav_help -> {
                Toast.makeText(this, "Help Clicked", Toast.LENGTH_LONG).show()
                return true
            }
        }
        return super.onOptionsItemSelected(it)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pg_home_toolbar_menu, menu)
        return true
    }
}