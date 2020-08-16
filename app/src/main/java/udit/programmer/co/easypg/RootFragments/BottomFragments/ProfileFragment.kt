package udit.programmer.co.easypg.RootFragments.BottomFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R

class ProfileFragment : Fragment() {

    private var currentCustomerProfileID = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentCustomerReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentCustomerReference =
            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(currentCustomerProfileID)
        currentCustomerReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val customer = snapshot.getValue(Customer::class.java)
                userName_profile_tv.text = customer!!.username
                if (customer.image != "")
                    Picasso.get().load(customer.image.toUri()).into(image_customer_profile_tv)
                name_profile_tv.text = customer.name
                email_profile_tv.text = customer.email
                gender_profile_tv.text = customer.gender
                mobile_number_customer_profile_tv.text = customer.mobileNumber
                address_customer_profile_tv.text = customer.address
                state_customer_profile_tv.text = customer.state
                district_customer_profile_tv.text = customer.district
                pincode_customer_profile_tv.text = customer.pinCode
                college_customer_profile_tv.text = customer.college
                father_name_customer_profile_tv.text = customer.fatherName
                father_mobile_no_customer_profile_tv.text = customer.fatherMobileNo
                mother_name_customer_profile_tv.text = customer.motherName
                dob_customer_profile_tv.text = customer.dob
            }
        })

    }

}