package udit.programmer.co.easypg.SignUpActivities.PG

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_p_g_add.*
import udit.programmer.co.easypg.Common.CollegeNames
import udit.programmer.co.easypg.Common.DistrictsName
import udit.programmer.co.easypg.Common.StateName
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PGAddActivity : AppCompatActivity(), View.OnClickListener {

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
    private val pgs by lazy {
        db.getReference("PGs")
    }

    private val numberList = mutableListOf<String>()

    private var roomTypeList = mutableListOf<String>()
    private var personsPerRoomList = mutableListOf<String>()
    private var totalNoOfRoomsList = mutableListOf<String>()
    private var totalNoOfACRoomsList = mutableListOf<String>()
    private var totalNoOfNonACRoomsList = mutableListOf<String>()
    private var availableNoOfACRoomsList = mutableListOf<String>()
    private var availableNoOfNonACRoomsList = mutableListOf<String>()
    private var rentOfACRoomsWithoutMessList = mutableListOf<String>()
    private var rentOfNonACRoomsWithoutMessList = mutableListOf<String>()
    private var rentOfACRoomsWithMessList = mutableListOf<String>()
    private var rentOfNonACRoomsWithMessList = mutableListOf<String>()

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
                Toast.makeText(this@PGAddActivity, "FAILED : " + e.message, Toast.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_g_add)

        numberList.add("NULL")
        for (i in 1..100)
            numberList.add(i.toString())

        roomDescriptionWork()

        pg_state_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )
        pg_state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pg_district_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        owner_dob_et.setOnClickListener(this)

        owner_state_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )

        owner_state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    owner_district_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        owner_password_image_eye.setOnClickListener {
            if (owner_passWord_input.transformationMethod == PasswordTransformationMethod.getInstance())
                owner_passWord_input.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
            else
                owner_passWord_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        nearest_college_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            CollegeNames().collegeList
        )

        pg_register_btn.setOnClickListener {
            if (checkEmptyField()) {
                if (!isAlreadyRegistered()) {
                    otpWork()
                    pg_submit_btn.setOnClickListener {
                        if (manualOtpWork()) {
                            roomDescriptionListGenerator()
                            auth.createUserWithEmailAndPassword(
                                owner_email_input.text.toString(),
                                owner_passWord_input.text.toString()
                            ).addOnSuccessListener {
                                val pg = PG(
                                    pgId = FirebaseAuth.getInstance().currentUser!!.uid.toString(),
                                    pgName = pg_name_input.text.toString(),
                                    pgType = when {
                                        malePg_radio_button.isChecked -> {
                                            malePg_radio_button.text.toString()
                                        }
                                        femalePg_radio_button.isChecked -> {
                                            femalePg_radio_button.text.toString()
                                        }
                                        else -> {
                                            bothPg_radio_button.text.toString()
                                        }
                                    },
                                    messAvailable = if (yesMess_radio_button.isChecked) {
                                        yesMess_radio_button.text.toString()
                                    } else {
                                        noMess_radio_button.text.toString()
                                    },
                                    liftAvailable = if (yesLift_radio_button.isChecked) {
                                        yesLift_radio_button.text.toString()
                                    } else {
                                        noLift_radio_button.text.toString()
                                    },
                                    pgState = pg_state_searchable_spinner.selectedItem.toString(),
                                    pgDistrict = pg_district_searchable_spinner.selectedItem.toString(),
                                    pgPincode = pg_pinCode_input.text.toString(),
                                    pgAddress = pg_address_input.text.toString(),
                                    roomType = roomTypeList,
                                    personsPerRoom = personsPerRoomList,
                                    numberOfRooms = totalNoOfRoomsList,
                                    numberOfACRooms = totalNoOfACRoomsList,
                                    numberOfNonACRooms = totalNoOfNonACRoomsList,
                                    availableNumberOfACRooms = availableNoOfACRoomsList,
                                    avaliableNumberOfNonACRooms = availableNoOfNonACRoomsList,
                                    rentOfACRoomsWithMess = rentOfACRoomsWithMessList,
                                    rentOfACRoomsWithoutMess = rentOfACRoomsWithoutMessList,
                                    rentOfNonACRoomsWithMess = rentOfNonACRoomsWithMessList,
                                    rentOfNonACRoomsWithoutMess = rentOfNonACRoomsWithoutMessList,
                                    ownerName = owner_name_input.text.toString(),
                                    ownerGender = when {
                                        owner_male_radio_button.isChecked -> {
                                            owner_male_radio_button.text.toString()
                                        }
                                        owner_female_radio_button.isChecked -> {
                                            owner_female_radio_button.text.toString()
                                        }
                                        else -> {
                                            owner_others_radio_button.text.toString()
                                        }
                                    },
                                    ownerDP = "",
                                    ownerDOB = owner_dob_et.text.toString(),
                                    ownerNumber = owner_mobileNumber_input.text.toString(),
                                    ownerState = owner_state_searchable_spinner.selectedItem.toString(),
                                    ownerDistrict = owner_district_searchable_spinner.selectedItem.toString(),
                                    ownerPincode = owner_pinCode_input.text.toString(),
                                    ownerAddress = owner_address_input.text.toString(),
                                    ownerEmail = owner_email_input.text.toString(),
                                    ownerPassWord = owner_passWord_input.text.toString(),
                                    nearestCollege = nearest_college_searchable_spinner.selectedItem.toString()
                                )
                                pgs.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(pg)
                                    .addOnSuccessListener {
                                        Snackbar.make(
                                            pg_add_layout,
                                            "Registered Successfully",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        Thread.sleep(2000)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Snackbar.make(
                                            pg_add_layout,
                                            "FAILED : $it",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                            }.addOnFailureListener {
                                Snackbar.make(
                                    pg_add_layout,
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
        var code = pg_otp_input.text.toString()
        if (code.isNullOrEmpty() || code.length < 6) {
            pg_otp_input.error = "Wrong OTP"
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

        pg_otp_tv.visibility = View.VISIBLE
        pg_otp_input.visibility = View.VISIBLE
        pg_otp_input_layout.visibility = View.VISIBLE
        pg_submit_btn.visibility = View.VISIBLE

        Toast.makeText(this, "OTP Sent to given Mobile Number", Toast.LENGTH_LONG).show()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91" + owner_mobileNumber_input.text.toString(),
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mPhoneAuthCallback
        )

    }

    private fun isAlreadyRegistered(): Boolean {

        val query = FirebaseDatabase.getInstance().reference
            .child("PGs")

        val pgsList = mutableListOf<PG>()
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapShot in dataSnapshot.children) {
                    val pg = snapShot.getValue(PG::class.java)
                    if (pg != null)
                        pgsList.add(pg)
                }
            }
        })
        for (pg in pgsList) {
            if (pg.ownerEmail == owner_email_input.text.toString()) {
                Toast.makeText(this, "Email is already Registered", Toast.LENGTH_LONG).show()
                return true
            }
            if (pg.ownerNumber == owner_mobileNumber_input.text.toString()) {
                Toast.makeText(this, "Mobile Number is already Registered", Toast.LENGTH_LONG)
                    .show()
                return true
            }
        }
        return false
    }

    private fun roomDescriptionWork() {

        //1.
        noOfPersonsPerRoom1_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms1_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms1_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val noOfRooms = position
                    val list = mutableListOf<String>()
                    list.add("NULL")
                    for (i in 1..(position + 1))
                        list.add(i.toString())
                    noOfACRooms1_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    noOfACRooms1_searchable_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val list01 = mutableListOf<String>()
                                list01.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list01.add(i.toString())
                                noOfNonACRooms1_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                noOfAvailableACRooms1_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list02
                                )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                noOfAvailableNonACRooms1_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list03
                                )
                            }
                        }
                }
            }

        //2.
        noOfPersonsPerRoom2_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms2_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms2_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val noOfRooms = position
                    val list = mutableListOf<String>()
                    list.add("NULL")
                    for (i in 1..(position + 1))
                        list.add(i.toString())
                    noOfACRooms2_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    noOfACRooms2_searchable_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val list01 = mutableListOf<String>()
                                list01.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list01.add(i.toString())
                                noOfNonACRooms2_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                noOfAvailableACRooms2_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list02
                                )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                noOfAvailableNonACRooms2_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list03
                                )
                            }
                        }
                }
            }

        //3.
        noOfPersonsPerRoom3_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms3_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms3_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val noOfRooms = position
                    val list = mutableListOf<String>()
                    list.add("NULL")
                    for (i in 1..(position + 1))
                        list.add(i.toString())
                    noOfACRooms3_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    noOfACRooms3_searchable_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val list01 = mutableListOf<String>()
                                list01.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list01.add(i.toString())
                                noOfNonACRooms3_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                noOfAvailableACRooms3_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list02
                                )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                noOfAvailableNonACRooms3_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list03
                                )
                            }
                        }
                }
            }

        //4.
        noOfPersonsPerRoom4_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms4_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms4_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val noOfRooms = position
                    val list = mutableListOf<String>()
                    list.add("NULL")
                    for (i in 1..(position + 1))
                        list.add(i.toString())
                    noOfACRooms4_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    noOfACRooms4_searchable_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val list01 = mutableListOf<String>()
                                list01.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list01.add(i.toString())
                                noOfNonACRooms4_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                noOfAvailableACRooms4_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list02
                                )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                noOfAvailableNonACRooms4_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list03
                                )
                            }
                        }
                }
            }

        //5.
        noOfPersonsPerRoom5_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms5_searchable_spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        noOfRooms5_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val noOfRooms = position
                    val list = mutableListOf<String>()
                    list.add("NULL")
                    for (i in 1..(position + 1))
                        list.add(i.toString())
                    noOfACRooms5_searchable_spinner.adapter = ArrayAdapter(
                        this@PGAddActivity,
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    noOfACRooms5_searchable_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val list01 = mutableListOf<String>()
                                list01.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list01.add(i.toString())
                                noOfNonACRooms5_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                noOfAvailableACRooms5_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list02
                                )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                noOfAvailableNonACRooms5_searchable_spinner.adapter = ArrayAdapter(
                                    this@PGAddActivity,
                                    android.R.layout.simple_expandable_list_item_1,
                                    list03
                                )
                            }
                        }
                }
            }

    }

    private fun roomDescriptionListGenerator() {

        roomTypeList.add("1")
        roomTypeList.add("2")
        roomTypeList.add("3")
        roomTypeList.add("4")
        roomTypeList.add("5")

        personsPerRoomList.add(noOfPersonsPerRoom1_searchable_spinner.selectedItem.toString())
        personsPerRoomList.add(noOfPersonsPerRoom2_searchable_spinner.selectedItem.toString())
        personsPerRoomList.add(noOfPersonsPerRoom3_searchable_spinner.selectedItem.toString())
        personsPerRoomList.add(noOfPersonsPerRoom4_searchable_spinner.selectedItem.toString())
        personsPerRoomList.add(noOfPersonsPerRoom5_searchable_spinner.selectedItem.toString())

        totalNoOfRoomsList.add(noOfRooms1_searchable_spinner.selectedItem.toString())
        totalNoOfRoomsList.add(noOfRooms2_searchable_spinner.selectedItem.toString())
        totalNoOfRoomsList.add(noOfRooms3_searchable_spinner.selectedItem.toString())
        totalNoOfRoomsList.add(noOfRooms4_searchable_spinner.selectedItem.toString())
        totalNoOfRoomsList.add(noOfRooms5_searchable_spinner.selectedItem.toString())

        totalNoOfACRoomsList.add(noOfACRooms1_searchable_spinner.selectedItem.toString())
        totalNoOfACRoomsList.add(noOfACRooms2_searchable_spinner.selectedItem.toString())
        totalNoOfACRoomsList.add(noOfACRooms3_searchable_spinner.selectedItem.toString())
        totalNoOfACRoomsList.add(noOfACRooms4_searchable_spinner.selectedItem.toString())
        totalNoOfACRoomsList.add(noOfACRooms5_searchable_spinner.selectedItem.toString())

        totalNoOfNonACRoomsList.add(noOfNonACRooms1_searchable_spinner.selectedItem.toString())
        totalNoOfNonACRoomsList.add(noOfNonACRooms2_searchable_spinner.selectedItem.toString())
        totalNoOfNonACRoomsList.add(noOfNonACRooms3_searchable_spinner.selectedItem.toString())
        totalNoOfNonACRoomsList.add(noOfNonACRooms4_searchable_spinner.selectedItem.toString())
        totalNoOfNonACRoomsList.add(noOfNonACRooms5_searchable_spinner.selectedItem.toString())

        availableNoOfACRoomsList.add(noOfAvailableACRooms1_searchable_spinner.selectedItem.toString())
        availableNoOfACRoomsList.add(noOfAvailableACRooms2_searchable_spinner.selectedItem.toString())
        availableNoOfACRoomsList.add(noOfAvailableACRooms3_searchable_spinner.selectedItem.toString())
        availableNoOfACRoomsList.add(noOfAvailableACRooms4_searchable_spinner.selectedItem.toString())
        availableNoOfACRoomsList.add(noOfAvailableACRooms5_searchable_spinner.selectedItem.toString())

        availableNoOfNonACRoomsList.add(noOfAvailableNonACRooms1_searchable_spinner.selectedItem.toString())
        availableNoOfNonACRoomsList.add(noOfAvailableNonACRooms2_searchable_spinner.selectedItem.toString())
        availableNoOfNonACRoomsList.add(noOfAvailableNonACRooms3_searchable_spinner.selectedItem.toString())
        availableNoOfNonACRoomsList.add(noOfAvailableNonACRooms4_searchable_spinner.selectedItem.toString())
        availableNoOfNonACRoomsList.add(noOfAvailableNonACRooms5_searchable_spinner.selectedItem.toString())

        rentOfACRoomsWithMessList.add(rentOfACRoomsWithMess1_input.text.toString())
        rentOfACRoomsWithMessList.add(rentOfACRoomsWithMess2_input.text.toString())
        rentOfACRoomsWithMessList.add(rentOfACRoomsWithMess3_input.text.toString())
        rentOfACRoomsWithMessList.add(rentOfACRoomsWithMess4_input.text.toString())
        rentOfACRoomsWithMessList.add(rentOfACRoomsWithMess5_input.text.toString())

        rentOfACRoomsWithoutMessList.add(rentOfACRoomsWithoutMess1_input.text.toString())
        rentOfACRoomsWithoutMessList.add(rentOfACRoomsWithoutMess2_input.text.toString())
        rentOfACRoomsWithoutMessList.add(rentOfACRoomsWithoutMess3_input.text.toString())
        rentOfACRoomsWithoutMessList.add(rentOfACRoomsWithoutMess4_input.text.toString())
        rentOfACRoomsWithoutMessList.add(rentOfACRoomsWithoutMess5_input.text.toString())

        rentOfNonACRoomsWithMessList.add(rentOfNonACRoomsWithMess1_input.text.toString())
        rentOfNonACRoomsWithMessList.add(rentOfNonACRoomsWithMess2_input.text.toString())
        rentOfNonACRoomsWithMessList.add(rentOfNonACRoomsWithMess3_input.text.toString())
        rentOfNonACRoomsWithMessList.add(rentOfNonACRoomsWithMess4_input.text.toString())
        rentOfNonACRoomsWithMessList.add(rentOfNonACRoomsWithMess5_input.text.toString())

        rentOfNonACRoomsWithoutMessList.add(rentOfNonACRoomsWithoutMess1_input.text.toString())
        rentOfNonACRoomsWithoutMessList.add(rentOfNonACRoomsWithoutMess2_input.text.toString())
        rentOfNonACRoomsWithoutMessList.add(rentOfNonACRoomsWithoutMess3_input.text.toString())
        rentOfNonACRoomsWithoutMessList.add(rentOfNonACRoomsWithoutMess4_input.text.toString())
        rentOfNonACRoomsWithoutMessList.add(rentOfNonACRoomsWithoutMess5_input.text.toString())

    }

    private fun checkEmptyField(): Boolean {
        if (pg_name_input.text.isNullOrBlank()) {
            pg_name_input.error = "This field is Empty"
            return false
        }
        if (!malePg_radio_button.isChecked && !femalePg_radio_button.isChecked && !bothPg_radio_button.isChecked) {
            Toast.makeText(this, "Please Select PG Type", Toast.LENGTH_LONG).show()
            return false
        }
        if (!yesMess_radio_button.isChecked && !noMess_radio_button.isChecked) {
            Toast.makeText(this, "Please Select Mess Availability", Toast.LENGTH_LONG).show()
            return false
        }
        if (!yesLift_radio_button.isChecked && !noLift_radio_button.isChecked) {
            Toast.makeText(this, "Please Select Lift Availability", Toast.LENGTH_LONG).show()
            return false
        }
        if (pg_state_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select State of PG", Toast.LENGTH_LONG).show()
            return false
        }
        if (pg_district_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select District of PG", Toast.LENGTH_LONG).show()
            return false
        }
        if (pg_pinCode_input.text.isNullOrBlank()) {
            pg_pinCode_input.error = "This field is Empty"
            return false
        }
        if (pg_address_input.text.isNullOrBlank()) {
            pg_address_input.error = "This field is Empty"
            return false
        }
        if (!roomDescriptionCheck()) {
            return false
        }
        if (owner_name_input.text.isNullOrBlank()) {
            owner_name_input.error = "This field is Empty"
            return false
        }
        if (!owner_male_radio_button.isChecked && !owner_female_radio_button.isChecked && !owner_others_radio_button.isChecked) {
            Toast.makeText(this, "Please Select Owner Gender", Toast.LENGTH_LONG).show()
            return false
        }
        if (owner_dob_et.text.isNullOrBlank()) {
            owner_dob_et.error = "This field is Empty"
            return false
        }
        if (owner_mobileNumber_input.text.isNullOrBlank()) {
            owner_mobileNumber_input.error = "This field is Empty"
            return false
        }
        if (owner_mobileNumber_input.text.toString().length != 10) {
            owner_mobileNumber_input.error = "Mobile Number is Invalid"
            return false
        }
        if (owner_state_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select State of Owner", Toast.LENGTH_LONG).show()
            return false
        }
        if (owner_district_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select District of Owner", Toast.LENGTH_LONG).show()
            return false
        }
        if (owner_pinCode_input.text.isNullOrBlank()) {
            owner_pinCode_input.error = "This field is Empty"
            return false
        }
        if (owner_address_input.text.isNullOrBlank()) {
            owner_address_input.error = "This field is Empty"
            return false
        }
        if (owner_email_input.text.isNullOrBlank()) {
            owner_email_input.error = "This field is Empty"
            return false
        }
        if (owner_passWord_input.text.isNullOrBlank()) {
            owner_passWord_input.setError("This field is Empty")
            return false
        }
        if (owner_passWord_input.text.toString().length < 7) {
            owner_passWord_input.setError("Password length should be greater than 6")
            Toast.makeText(this, "Password id too Short", Toast.LENGTH_LONG).show()
            return false
        }
        if (nearest_college_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Please Select Nearest College", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun roomDescriptionCheck(): Boolean {
        //1.
        if (noOfPersonsPerRoom1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Persons per Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfRooms1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfACRooms1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfNonACRooms1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Non AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfAvailableACRooms1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (noOfAvailableNonACRooms1_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available Non AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (rentOfACRoomsWithMess1_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithMess1_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithMess1_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithMess1_input.error = "This field is Empty"
            return false
        }
        if (rentOfACRoomsWithoutMess1_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithoutMess1_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithoutMess1_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithoutMess1_input.error = "This field is Empty"
            return false
        }
        //2.
        if (noOfPersonsPerRoom2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Persons per Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfRooms2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfACRooms2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfNonACRooms2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Non AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfAvailableACRooms2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (noOfAvailableNonACRooms2_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available Non AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (rentOfACRoomsWithMess2_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithMess2_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithMess2_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithMess2_input.error = "This field is Empty"
            return false
        }
        if (rentOfACRoomsWithoutMess2_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithoutMess2_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithoutMess2_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithoutMess2_input.error = "This field is Empty"
            return false
        }
        //3.
        if (noOfPersonsPerRoom3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Persons per Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfRooms3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfACRooms3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfNonACRooms3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Non AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfAvailableACRooms3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (noOfAvailableNonACRooms3_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available Non AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (rentOfACRoomsWithMess3_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithMess3_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithMess3_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithMess3_input.error = "This field is Empty"
            return false
        }
        if (rentOfACRoomsWithoutMess3_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithoutMess3_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithoutMess3_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithoutMess3_input.error = "This field is Empty"
            return false
        }
        //4.
        if (noOfPersonsPerRoom4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Persons per Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfRooms4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfACRooms4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfNonACRooms4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Non AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfAvailableACRooms4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (noOfAvailableNonACRooms4_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available Non AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (rentOfACRoomsWithMess4_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithMess4_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithMess4_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithMess4_input.error = "This field is Empty"
            return false
        }
        if (rentOfACRoomsWithoutMess4_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithoutMess4_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithoutMess4_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithoutMess4_input.error = "This field is Empty"
            return false
        }
        //5.
        if (noOfPersonsPerRoom5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Persons per Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfRooms5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfACRooms5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfNonACRooms5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Non AC Room Field", Toast.LENGTH_LONG).show()
            return false
        }
        if (noOfAvailableACRooms5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (noOfAvailableNonACRooms5_searchable_spinner.selectedItem == "") {
            Toast.makeText(this, "Select Number of Available Non AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (rentOfACRoomsWithMess5_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithMess5_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithMess5_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithMess5_input.error = "This field is Empty"
            return false
        }
        if (rentOfACRoomsWithoutMess5_input.text.isNullOrEmpty()) {
            rentOfACRoomsWithoutMess5_input.error = "This field is Empty"
            return false
        }
        if (rentOfNonACRoomsWithoutMess5_input.text.isNullOrEmpty()) {
            rentOfNonACRoomsWithoutMess5_input.error = "This field is Empty"
            return false
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.owner_dob_et -> {
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
        owner_dob_et.setText(sdf.format(myCalender.time))
    }

}