package udit.programmer.co.easypg.SignUpActivities.Customer

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_customer_sign_up.*
import udit.programmer.co.easypg.Common.CollegeNames
import udit.programmer.co.easypg.Common.DistrictsName
import udit.programmer.co.easypg.Common.StateName
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CustomerSignUp : AppCompatActivity(), View.OnClickListener {

    private lateinit var myCalender: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var finalDate = 0L

    private lateinit var verificationCodeBySystem: String

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    private val customers by lazy {
        db.getReference("Customers")
    }
    private val mPhoneAuthCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationCodeBySystem = p0
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                if (p0.smsCode != null)
                    otpAuthWork(p0.smsCode.toString())
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@CustomerSignUp, "FAILED : " + e.message, Toast.LENGTH_LONG)
                    .show()
                Log.d("Ceased Meteor", "FAILED : " + e.message)
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_sign_up)

        dob_et.setOnClickListener(this)

        password_image_eye.setOnClickListener {
            if (passWord_input.transformationMethod == PasswordTransformationMethod.getInstance())
                passWord_input.transformationMethod = HideReturnsTransformationMethod.getInstance();
            else
                passWord_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        state_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )
        state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    district_searchable_spinner.adapter = ArrayAdapter(
                        this@CustomerSignUp,
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        college_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            CollegeNames().collegeList
        )

        submit_btn.setOnClickListener {
            if (checkEmptyField()) {
                if (!isAlreadyRegistered()) {
                    otpWork()
                    register_btn.setOnClickListener {
                        if (manualOtpWork()) {
                            auth.createUserWithEmailAndPassword(
                                email_input.text.toString(),
                                passWord_input.text.toString()
                            ).addOnSuccessListener {
                                val customer = Customer(
                                    customerId = FirebaseAuth.getInstance().currentUser!!.uid.toString(),
                                    name = name_input.text.toString(),
                                    image = "",
                                    gender = when {
                                        male_radio_button.isChecked -> {
                                            male_radio_button.text.toString()
                                        }
                                        female_radio_button.isChecked -> {
                                            female_radio_button.text.toString()
                                        }
                                        others_radio_button.isChecked -> {
                                            others_radio_button.text.toString()
                                        }
                                        else -> {
                                            "Not to Show"
                                        }
                                    },
                                    fatherName = fatherName_input.text.toString(),
                                    motherName = motherName_input.text.toString(),
                                    fatherMobileNo = fatherMobileNumber_input.text.toString(),
                                    dob = dob_et.text.toString(),
                                    mobileNumber = mobileNumber_input.text.toString(),
                                    state = state_searchable_spinner.selectedItem.toString(),
                                    district = district_searchable_spinner.selectedItem.toString(),
                                    pinCode = pinCode_input.text.toString(),
                                    address = address_input.text.toString(),
                                    email = email_input.text.toString(),
                                    username = userName_input.text.toString(),
                                    password = passWord_input.text.toString(),
                                    college = college_searchable_spinner.selectedItem.toString()
                                )
                                customers.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(customer)
                                    .addOnSuccessListener {
                                        Snackbar.make(
                                            customer_sign_up_layout,
                                            "Registered Successfully",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        Thread.sleep(2000)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Snackbar.make(
                                            customer_sign_up_layout,
                                            "FAILED : $it",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                            }.addOnFailureListener {
                                Snackbar.make(
                                    customer_sign_up_layout,
                                    "FAILED : $it",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun manualOtpWork(): Boolean {
        var code = otp_input.text.toString()
        if (code.isNullOrEmpty() || code.length < 6) {
            otp_input.error = "Wrong OTP"
            Toast.makeText(this, "Wrong OTP", Toast.LENGTH_LONG).show()
            return false
        }
        otpAuthWork(code)
        return true
    }

    private fun otpAuthWork(code: String) {
        PhoneAuthProvider.getCredential(verificationCodeBySystem, code)
        Toast.makeText(this, "OTP Verified", Toast.LENGTH_LONG).show()
    }

    private fun otpWork() {

        otp_tv.visibility = View.VISIBLE
        otp_input.visibility = View.VISIBLE
        otp_input_layout.visibility = View.VISIBLE
        register_btn.visibility = View.VISIBLE

        Toast.makeText(this, "OTP Sent to given Mobile Number", Toast.LENGTH_LONG).show()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91" + mobileNumber_input.text.toString(),
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mPhoneAuthCallback
        )

    }

    private fun isAlreadyRegistered(): Boolean {
        val query = FirebaseDatabase.getInstance().reference
            .child("Customers")

        val customersList = mutableListOf<Customer>()
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapShot in dataSnapshot.children) {
                    val customer = snapShot.getValue(Customer::class.java)
                    if (customer != null)
                        customersList.add(customer)
                }
            }
        })
        for (customer in customersList) {
            if (customer.email == email_input.text.toString()) {
                Toast.makeText(this, "Email is already Registered", Toast.LENGTH_LONG).show()
                return true
            }
            if (customer.mobileNumber == mobileNumber_input.text.toString()) {
                Toast.makeText(this, "Mobile Number is already Registered", Toast.LENGTH_LONG)
                    .show()
                return true
            }
        }
        return false
    }

    private fun checkEmptyField(): Boolean {
        if (name_input.text.isNullOrBlank()) {
            name_input.error = "This field is Empty"
            return false
        }
        if (!male_radio_button.isChecked && !female_radio_button.isChecked && !others_radio_button.isChecked) {
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_LONG).show()
            return false
        }
        if (fatherName_input.text.isNullOrBlank()) {
            fatherName_input.error = "This field is Empty"
            return false
        }
        if (motherName_input.text.isNullOrBlank()) {
            motherName_input.error = "This field is Empty"
            return false
        }
        if (fatherMobileNumber_input.text.isNullOrBlank()) {
            fatherMobileNumber_input.error = "This field is Empty"
            return false
        }
        if (dob_et.text.isNullOrBlank()) {
            dob_et.error = "This field is Empty"
            return false
        }
        if (mobileNumber_input.text.isNullOrBlank()) {
            mobileNumber_input.error = "This field is Empty"
            return false
        }
        if (mobileNumber_input.text.toString().length != 10) {
            mobileNumber_input.error = "Mobile Number is Invalid"
            return false
        }
        if (state_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select State", Toast.LENGTH_LONG).show()
            return false
        }
        if (district_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select District", Toast.LENGTH_LONG).show()
            return false
        }
        if (pinCode_input.text.isNullOrBlank()) {
            pinCode_input.error = "This field is Empty"
            return false
        }
        if (address_input.text.isNullOrBlank()) {
            address_input.error = "This field is Empty"
            return false
        }
        if (email_input.text.isNullOrBlank()) {
            email_input.error = "This field is Empty"
            return false
        }
        if (userName_input.text.isNullOrBlank()) {
            userName_input.error = "This field is Empty"
            return false
        }
        if (passWord_input.text.isNullOrBlank()) {
            passWord_input.error = "This field is Empty"
            return false
        }
        if (passWord_input.text.toString().length < 7) {
            passWord_input.error = "Password length should be greater than 6"
            Toast.makeText(this, "Password id too Short", Toast.LENGTH_LONG).show()
            return false
        }
        if (college_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select College", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.dob_et -> {
                setDateListener()
            }
        }
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
            this, dateSetListener, myCalender.get(Calendar.YEAR),
            myCalender.get(Calendar.MONTH), myCalender.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateDate() {
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat)
        finalDate = myCalender.time.time
        dob_et.setText(sdf.format(myCalender.time))
    }

}