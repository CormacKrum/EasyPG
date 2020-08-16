package udit.programmer.co.easypg.SearchProfiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_customer_profile.*
import udit.programmer.co.easypg.ChatActivities.ChatActivity
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R

class CustomerProfileActivity : AppCompatActivity() {

    private lateinit var currentCustomerReference: DatabaseReference
    private lateinit var dialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        dialog.show()

        val id = intent.getStringExtra("Ceased Meteor")
        retriveData(id)

        //Chat Work Pending
        search_mobile_chat_btn_profile_tv.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun retriveData(id: String) {
        currentCustomerReference =
            FirebaseDatabase.getInstance().reference.child("Customers").child(id)
        currentCustomerReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val customer = snapshot.getValue(Customer::class.java)
                search_userName_profile_tv.text = customer!!.username
                if (customer.image != "")
                    Picasso.get().load(customer.image.toUri())
                        .into(search_image_customer_profile_tv)
                search_name_profile_tv.text = customer.name
                search_email_profile_tv.text = customer.email
                search_gender_profile_tv.text = customer.gender
                search_mobile_number_customer_profile_tv.text = customer.mobileNumber
                search_address_customer_profile_tv.text = customer.address
                search_state_customer_profile_tv.text = customer.state
                search_district_customer_profile_tv.text = customer.district
                search_pincode_customer_profile_tv.text = customer.pinCode
                search_college_customer_profile_tv.text = customer.college
                search_father_name_customer_profile_tv.text = customer.fatherName
                search_father_mobile_no_customer_profile_tv.text = customer.fatherMobileNo
                search_mother_name_customer_profile_tv.text = customer.motherName
                search_dob_customer_profile_tv.text = customer.dob
                dialog.dismiss()
            }
        })


    }
}