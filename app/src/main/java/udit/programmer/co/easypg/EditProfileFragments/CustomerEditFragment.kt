package udit.programmer.co.easypg.EditProfileFragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_customer_edit.*
import udit.programmer.co.easypg.Common.CollegeNames
import udit.programmer.co.easypg.Common.DistrictsName
import udit.programmer.co.easypg.Common.StateName
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R
import java.text.SimpleDateFormat
import java.util.*

class CustomerEditFragment : Fragment() {

    private lateinit var myCalender: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var finalDate = 0L

    private var currentCustomerProfileID = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentCustomerReference: DatabaseReference

    private var uri: Uri? = null
    private lateinit var dialog: AlertDialog

    private lateinit var firebaseStorage: StorageReference
    private lateinit var filePath: StorageReference

    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseStorage = FirebaseStorage.getInstance().reference
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Customers")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(requireContext()).build()

        edit_dob_et.setOnClickListener {
            setDateListener()
        }

        edit_password_image_eye.setOnClickListener {
            if (edit_passWord_input.transformationMethod == PasswordTransformationMethod.getInstance())
                edit_passWord_input.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
            else
                edit_passWord_input.transformationMethod =
                    PasswordTransformationMethod.getInstance();
        }

        edit_state_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )

        edit_college_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            CollegeNames().collegeList
        )

        edit_state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    edit_district_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        edit_change_profile_image_tv.setOnClickListener {
            CropImage.activity().setAspectRatio(1, 1).start(requireContext(), this)
        }

        currentCustomerReference =
            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(currentCustomerProfileID)
        currentCustomerReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val customer = snapshot.getValue(Customer::class.java)
                edit_name_input.setText(customer!!.name)
                if (customer.image != "")
                    Picasso.get().load(customer.image.toUri()).into(edit_customer_profile_image)
                when (customer.gender) {
                    "Male" -> edit_gender_radio_group.check(R.id.edit_male_radio_button)
                    "Female" -> edit_gender_radio_group.check(R.id.edit_female_radio_button)
                    else -> edit_gender_radio_group.check(R.id.edit_others_radio_button)
                }
                edit_fatherName_input.setText(customer.fatherName)
                edit_motherName_input.setText(customer.motherName)
                edit_fatherMobileNumber_input.setText(customer.fatherMobileNo)
                edit_dob_et.setText(customer.dob)
                edit_mobileNumber_input.setText(customer.mobileNumber)
                edit_pinCode_input.setText(customer.pinCode)
                edit_address_input.setText(customer.address)
                edit_email_input.setText(customer.email)
                edit_userName_input.setText(customer.username)
                edit_passWord_input.setText(customer.password)
            }
        })

        edit_save_btn.setOnClickListener {
            if (checkEmptyField()) {
                val customerMap = mutableMapOf<String, Any?>()
                customerMap["customerId"] = FirebaseAuth.getInstance().currentUser!!.uid
                customerMap["name"] = edit_name_input.text.toString()
                customerMap["gender"] = when {
                    edit_male_radio_button.isChecked -> {
                        edit_male_radio_button.text.toString()
                    }
                    edit_female_radio_button.isChecked -> {
                        edit_female_radio_button.text.toString()
                    }
                    edit_others_radio_button.isChecked -> {
                        edit_others_radio_button.text.toString()
                    }
                    else -> {
                        "Not to Show"
                    }
                }
                customerMap["fatherName"] = edit_fatherMobileNumber_input.text.toString()
                customerMap["motherName"] = edit_mobileNumber_input.text.toString()
                customerMap["fatherMobileNo"] = edit_fatherMobileNumber_input.text.toString()
                customerMap["dob"] = edit_dob_et.text.toString()
                customerMap["mobileNumber"] = edit_mobileNumber_input.text.toString()
                customerMap["state"] = edit_state_searchable_spinner.selectedItem.toString()
                customerMap["district"] = edit_district_searchable_spinner.selectedItem.toString()
                customerMap["pinCode"] = edit_pinCode_input.text.toString()
                customerMap["address"] = edit_address_input.text.toString()
                customerMap["email"] = edit_email_input.text.toString()
                customerMap["username"] = edit_userName_input.text.toString()
                customerMap["password"] = edit_passWord_input.text.toString()
                customerMap["college"] = edit_college_searchable_spinner.selectedItem.toString()
                FirebaseDatabase.getInstance().reference.child("Customers")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .updateChildren(customerMap).addOnCompleteListener {
                        Toast.makeText(
                            requireContext(),
                            "Information Updated Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            dialog.show()
            uri = CropImage.getActivityResult(data).uri
            Picasso.get().load(uri).into(edit_customer_profile_image)

            filePath = firebaseStorage.child("Customers")
                .child(firebaseUser.uid)
            filePath.putFile(CropImage.getActivityResult(data).uri).addOnCompleteListener {
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Uploading Failed $it :(", Toast.LENGTH_LONG)
                    .show()
            }.continueWithTask {
                filePath.downloadUrl
            }.addOnCompleteListener {
                if (it.isComplete) {
                    uri = it.result
                    updateImageValue()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Uri Failed $it :(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateImageValue() {
        val map = mutableMapOf<String, Any>()
        map["image"] = uri!!.toString()
        firebaseDatabase.child(firebaseUser.uid).updateChildren(map)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Uploaded Successfully :)", Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Uploading Failed :(", Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun checkEmptyField(): Boolean {
        if (edit_name_input.text.isNullOrBlank()) {
            edit_name_input.error = "This field is Empty"
            return false
        }
        if (!edit_male_radio_button.isChecked && !edit_female_radio_button.isChecked && !edit_others_radio_button.isChecked) {
            Toast.makeText(requireContext(), "Please Select Gender", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_fatherName_input.text.isNullOrBlank()) {
            edit_fatherName_input.error = "This field is Empty"
            return false
        }
        if (edit_motherName_input.text.isNullOrBlank()) {
            edit_motherName_input.error = "This field is Empty"
            return false
        }
        if (edit_fatherMobileNumber_input.text.isNullOrBlank()) {
            edit_fatherMobileNumber_input.error = "This field is Empty"
            return false
        }
        if (edit_dob_et.text.isNullOrBlank()) {
            edit_dob_et.error = "This field is Empty"
            return false
        }
        if (edit_mobileNumber_input.text.isNullOrBlank()) {
            edit_mobileNumber_input.error = "This field is Empty"
            return false
        }
        if (edit_mobileNumber_input.text.toString().length != 10) {
            edit_mobileNumber_input.error = "Mobile Number is Invalid"
            return false
        }
        if (edit_state_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select State", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_district_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select District", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_pinCode_input.text.isNullOrBlank()) {
            edit_pinCode_input.error = "This field is Empty"
            return false
        }
        if (edit_address_input.text.isNullOrBlank()) {
            edit_address_input.error = "This field is Empty"
            return false
        }
        if (edit_email_input.text.isNullOrBlank()) {
            edit_email_input.error = "This field is Empty"
            return false
        }
        if (edit_userName_input.text.isNullOrBlank()) {
            edit_userName_input.error = "This field is Empty"
            return false
        }
        if (edit_passWord_input.text.isNullOrBlank()) {
            edit_passWord_input.error = "This field is Empty"
            return false
        }
        if (edit_passWord_input.text.toString().length < 7) {
            edit_passWord_input.error = "Password length should be greater than 6"
            Toast.makeText(requireContext(), "Password id too Short", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_college_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select College", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun setDateListener() {
        myCalender = Calendar.getInstance()
        dateSetListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                myCalender.set(Calendar.YEAR, year)
                myCalender.set(Calendar.MONTH, month)
                myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDate()
            }
        val datePickerDialog = DatePickerDialog(
            requireContext(), dateSetListener, myCalender.get(Calendar.YEAR),
            myCalender.get(Calendar.MONTH), myCalender.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateDate() {
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat)
        finalDate = myCalender.time.time
        edit_dob_et.setText(sdf.format(myCalender.time))
    }

}