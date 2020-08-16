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
import kotlinx.android.synthetic.main.fragment_p_gedit.*
import udit.programmer.co.easypg.Common.CollegeNames
import udit.programmer.co.easypg.Common.DistrictsName
import udit.programmer.co.easypg.Common.StateName
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R
import java.text.SimpleDateFormat
import java.util.*

class PGeditFragment : Fragment() {

    private lateinit var myCalender: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var finalDate = 0L

    private var uri: Uri? = null
    private lateinit var dialog: AlertDialog

    private lateinit var firebaseStorage: StorageReference
    private lateinit var filePath: StorageReference

    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseUser: FirebaseUser

    private var currentPGProfileID = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentPGReference: DatabaseReference

    private val numberList = mutableListOf<String>()

    private var editroomTypeList = mutableListOf<String>()
    private var editpersonsPerRoomList = mutableListOf<String>()
    private var edittotalNoOfRoomsList = mutableListOf<String>()
    private var edittotalNoOfACRoomsList = mutableListOf<String>()
    private var edittotalNoOfNonACRoomsList = mutableListOf<String>()
    private var editavailableNoOfACRoomsList = mutableListOf<String>()
    private var editavailableNoOfNonACRoomsList = mutableListOf<String>()
    private var editrentOfACRoomsWithoutMessList = mutableListOf<String>()
    private var editrentOfNonACRoomsWithoutMessList = mutableListOf<String>()
    private var editrentOfACRoomsWithMessList = mutableListOf<String>()
    private var editrentOfNonACRoomsWithMessList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_p_gedit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        numberList.add("NULL")
        for (i in 1..100)
            numberList.add(i.toString())

        firebaseStorage = FirebaseStorage.getInstance().reference
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("PGs")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(requireContext()).build()

        edit_choose_profile_image_pg_tv.setOnClickListener{
            CropImage.activity().setAspectRatio(1, 1).start(requireContext(), this)
        }

        edit_pg_state_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )

        edit_pg_state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    edit_pg_district_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        edit_owner_state_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            StateName().state
        )

        edit_owner_state_searchable_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    edit_owner_district_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        DistrictsName().districtsName[position]
                    )
                }
            }

        edit_nearest_college_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            CollegeNames().collegeList
        )

        edit_owner_dob_et.setOnClickListener {
            setDateListener()
        }
        edit_owner_password_image_eye.setOnClickListener {
            if (edit_owner_passWord_input.transformationMethod == PasswordTransformationMethod.getInstance())
                edit_owner_passWord_input.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
            else
                edit_owner_passWord_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        roomDescriptionWork()
        retrieveData()

        edit_pg_save_btn.setOnClickListener {
            if (checkEmptyField()) {
                roomDescriptionListGenerator()
                val pgMap = mutableMapOf<String, Any?>()
                pgMap["pgId"] = FirebaseAuth.getInstance().currentUser!!.uid
                pgMap.put("pgName", edit_pg_name_input.text.toString())
                pgMap.put(
                    "pgType", when {
                        edit_malePg_radio_button.isChecked -> {
                            edit_malePg_radio_button.text.toString()
                        }
                        edit_femalePg_radio_button.isChecked -> {
                            edit_femalePg_radio_button.text.toString()
                        }
                        else -> {
                            edit_bothPg_radio_button.text.toString()
                        }
                    }
                )
                pgMap.put(
                    "messAvailable", if (edit_yesMess_radio_button.isChecked) {
                        edit_yesMess_radio_button.text.toString()
                    } else {
                        edit_noMess_radio_button.text.toString()
                    }
                )
                pgMap.put(
                    "liftAvailable", if (edit_yesLift_radio_button.isChecked) {
                        edit_yesLift_radio_button.text.toString()
                    } else {
                        edit_noLift_radio_button.text.toString()
                    }
                )
                pgMap.put("pgState", edit_pg_state_searchable_spinner.selectedItem.toString())
                pgMap.put("pgDistrict", edit_pg_district_searchable_spinner.selectedItem.toString())
                pgMap.put("pgPincode", edit_pg_pinCode_input.text.toString())
                pgMap.put("pgAddress", edit_pg_address_input.text.toString())
                pgMap.put("roomType", editroomTypeList)
                pgMap.put("personsPerRoom", editpersonsPerRoomList)
                pgMap.put("numberOfRooms", edittotalNoOfRoomsList)
                pgMap.put("numberOfACRooms", edittotalNoOfACRoomsList)
                pgMap.put("numberOfNonACRooms", edittotalNoOfNonACRoomsList)
                pgMap.put("availableNumberOfACRooms", editavailableNoOfACRoomsList)
                pgMap.put("avaliableNumberOfNonACRooms", editavailableNoOfNonACRoomsList)
                pgMap.put("rentOfACRoomsWithMess", editrentOfACRoomsWithMessList)
                pgMap.put("rentOfACRoomsWithoutMess", editrentOfACRoomsWithoutMessList)
                pgMap.put("rentOfNonACRoomsWithMess", editrentOfNonACRoomsWithMessList)
                pgMap.put("rentOfNonACRoomsWithoutMess", editrentOfNonACRoomsWithoutMessList)
                pgMap.put("ownerName", edit_owner_name_input.text.toString())
                pgMap.put(
                    "ownerGender", when {
                        edit_owner_male_radio_button.isChecked -> {
                            edit_owner_male_radio_button.text.toString()
                        }
                        edit_owner_female_radio_button.isChecked -> {
                            edit_owner_female_radio_button.text.toString()
                        }
                        else -> {
                            edit_owner_others_radio_button.text.toString()
                        }
                    }
                )
                pgMap.put("ownerDOB", edit_owner_dob_et.text.toString())
                pgMap.put("ownerNumber", edit_owner_mobileNumber_input.text.toString())
                pgMap.put("ownerState", edit_owner_state_searchable_spinner.selectedItem.toString())
                pgMap.put(
                    "ownerDistrict",
                    edit_owner_district_searchable_spinner.selectedItem.toString()
                )
                pgMap.put("ownerPincode", edit_owner_pinCode_input.text.toString())
                pgMap.put("ownerAddress", edit_owner_address_input.text.toString())
                pgMap.put("ownerEmail", edit_owner_email_input.text.toString())
                pgMap.put("ownerPassWord", edit_owner_passWord_input.text.toString())
                pgMap.put(
                    "nearestCollege",
                    edit_nearest_college_searchable_spinner.selectedItem.toString()
                )
                FirebaseDatabase.getInstance().reference.child("PGs")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .updateChildren(pgMap).addOnCompleteListener {
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
            Picasso.get().load(uri).into(edit_pg_profile_image)

            filePath = firebaseStorage.child("PGs")
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

    private fun updateImageValue(){
        val map = mutableMapOf<String, Any>()
        map["ownerDP"] = uri!!.toString()
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

    private fun roomDescriptionListGenerator() {

        editroomTypeList.add("1")
        editroomTypeList.add("2")
        editroomTypeList.add("3")
        editroomTypeList.add("4")
        editroomTypeList.add("5")

        editpersonsPerRoomList.add(edit_noOfPersonsPerRoom1_searchable_spinner.selectedItem.toString())
        editpersonsPerRoomList.add(edit_noOfPersonsPerRoom2_searchable_spinner.selectedItem.toString())
        editpersonsPerRoomList.add(edit_noOfPersonsPerRoom3_searchable_spinner.selectedItem.toString())
        editpersonsPerRoomList.add(edit_noOfPersonsPerRoom4_searchable_spinner.selectedItem.toString())
        editpersonsPerRoomList.add(edit_noOfPersonsPerRoom5_searchable_spinner.selectedItem.toString())

        edittotalNoOfRoomsList.add(edit_noOfRooms1_searchable_spinner.selectedItem.toString())
        edittotalNoOfRoomsList.add(edit_noOfRooms2_searchable_spinner.selectedItem.toString())
        edittotalNoOfRoomsList.add(edit_noOfRooms3_searchable_spinner.selectedItem.toString())
        edittotalNoOfRoomsList.add(edit_noOfRooms4_searchable_spinner.selectedItem.toString())
        edittotalNoOfRoomsList.add(edit_noOfRooms5_searchable_spinner.selectedItem.toString())

        edittotalNoOfACRoomsList.add(edit_noOfACRooms1_searchable_spinner.selectedItem.toString())
        edittotalNoOfACRoomsList.add(edit_noOfACRooms2_searchable_spinner.selectedItem.toString())
        edittotalNoOfACRoomsList.add(edit_noOfACRooms3_searchable_spinner.selectedItem.toString())
        edittotalNoOfACRoomsList.add(edit_noOfACRooms4_searchable_spinner.selectedItem.toString())
        edittotalNoOfACRoomsList.add(edit_noOfACRooms5_searchable_spinner.selectedItem.toString())

        edittotalNoOfNonACRoomsList.add(edit_noOfNonACRooms1_searchable_spinner.selectedItem.toString())
        edittotalNoOfNonACRoomsList.add(edit_noOfNonACRooms2_searchable_spinner.selectedItem.toString())
        edittotalNoOfNonACRoomsList.add(edit_noOfNonACRooms3_searchable_spinner.selectedItem.toString())
        edittotalNoOfNonACRoomsList.add(edit_noOfNonACRooms4_searchable_spinner.selectedItem.toString())
        edittotalNoOfNonACRoomsList.add(edit_noOfNonACRooms5_searchable_spinner.selectedItem.toString())

        editavailableNoOfACRoomsList.add(edit_noOfAvailableACRooms1_searchable_spinner.selectedItem.toString())
        editavailableNoOfACRoomsList.add(edit_noOfAvailableACRooms2_searchable_spinner.selectedItem.toString())
        editavailableNoOfACRoomsList.add(edit_noOfAvailableACRooms3_searchable_spinner.selectedItem.toString())
        editavailableNoOfACRoomsList.add(edit_noOfAvailableACRooms4_searchable_spinner.selectedItem.toString())
        editavailableNoOfACRoomsList.add(edit_noOfAvailableACRooms5_searchable_spinner.selectedItem.toString())

        editavailableNoOfNonACRoomsList.add(edit_noOfAvailableNonACRooms1_searchable_spinner.selectedItem.toString())
        editavailableNoOfNonACRoomsList.add(edit_noOfAvailableNonACRooms2_searchable_spinner.selectedItem.toString())
        editavailableNoOfNonACRoomsList.add(edit_noOfAvailableNonACRooms3_searchable_spinner.selectedItem.toString())
        editavailableNoOfNonACRoomsList.add(edit_noOfAvailableNonACRooms4_searchable_spinner.selectedItem.toString())
        editavailableNoOfNonACRoomsList.add(edit_noOfAvailableNonACRooms5_searchable_spinner.selectedItem.toString())

        editrentOfACRoomsWithMessList.add(edit_rentOfACRoomsWithMess1_input.text.toString())
        editrentOfACRoomsWithMessList.add(edit_rentOfACRoomsWithMess2_input.text.toString())
        editrentOfACRoomsWithMessList.add(edit_rentOfACRoomsWithMess3_input.text.toString())
        editrentOfACRoomsWithMessList.add(edit_rentOfACRoomsWithMess4_input.text.toString())
        editrentOfACRoomsWithMessList.add(edit_rentOfACRoomsWithMess5_input.text.toString())

        editrentOfACRoomsWithoutMessList.add(edit_rentOfACRoomsWithoutMess1_input.text.toString())
        editrentOfACRoomsWithoutMessList.add(edit_rentOfACRoomsWithoutMess2_input.text.toString())
        editrentOfACRoomsWithoutMessList.add(edit_rentOfACRoomsWithoutMess3_input.text.toString())
        editrentOfACRoomsWithoutMessList.add(edit_rentOfACRoomsWithoutMess4_input.text.toString())
        editrentOfACRoomsWithoutMessList.add(edit_rentOfACRoomsWithoutMess5_input.text.toString())

        editrentOfNonACRoomsWithMessList.add(edit_rentOfNonACRoomsWithMess1_input.text.toString())
        editrentOfNonACRoomsWithMessList.add(edit_rentOfNonACRoomsWithMess2_input.text.toString())
        editrentOfNonACRoomsWithMessList.add(edit_rentOfNonACRoomsWithMess3_input.text.toString())
        editrentOfNonACRoomsWithMessList.add(edit_rentOfNonACRoomsWithMess4_input.text.toString())
        editrentOfNonACRoomsWithMessList.add(edit_rentOfNonACRoomsWithMess5_input.text.toString())

        editrentOfNonACRoomsWithoutMessList.add(edit_rentOfNonACRoomsWithoutMess1_input.text.toString())
        editrentOfNonACRoomsWithoutMessList.add(edit_rentOfNonACRoomsWithoutMess2_input.text.toString())
        editrentOfNonACRoomsWithoutMessList.add(edit_rentOfNonACRoomsWithoutMess3_input.text.toString())
        editrentOfNonACRoomsWithoutMessList.add(edit_rentOfNonACRoomsWithoutMess4_input.text.toString())
        editrentOfNonACRoomsWithoutMessList.add(edit_rentOfNonACRoomsWithoutMess5_input.text.toString())

    }

    private fun checkEmptyField(): Boolean {
        if (edit_pg_name_input.text.isNullOrBlank()) {
            edit_pg_name_input.error = "This field is Empty"
            return false
        }
        if (!edit_malePg_radio_button.isChecked && !edit_femalePg_radio_button.isChecked && !edit_bothPg_radio_button.isChecked) {
            Toast.makeText(requireContext(), "Please Select PG Type", Toast.LENGTH_LONG).show()
            return false
        }
        if (!edit_yesMess_radio_button.isChecked && !edit_noMess_radio_button.isChecked) {
            Toast.makeText(requireContext(), "Please Select Mess Availability", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (!edit_yesLift_radio_button.isChecked && !edit_noLift_radio_button.isChecked) {
            Toast.makeText(requireContext(), "Please Select Lift Availability", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_pg_state_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select State of PG", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_pg_district_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select District of PG", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_pg_pinCode_input.text.isNullOrBlank()) {
            edit_pg_pinCode_input.error = "This field is Empty"
            return false
        }
        if (edit_pg_address_input.text.isNullOrBlank()) {
            edit_pg_address_input.error = "This field is Empty"
            return false
        }
        if (!roomDescriptionCheck()) {
            return false
        }
        if (edit_owner_name_input.text.isNullOrBlank()) {
            edit_owner_name_input.error = "This field is Empty"
            return false
        }
        if (!edit_owner_male_radio_button.isChecked && !edit_owner_female_radio_button.isChecked && !edit_owner_others_radio_button.isChecked) {
            Toast.makeText(requireContext(), "Please Select Owner Gender", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_owner_dob_et.text.isNullOrBlank()) {
            edit_owner_dob_et.error = "This field is Empty"
            return false
        }
        if (edit_owner_mobileNumber_input.text.isNullOrBlank()) {
            edit_owner_mobileNumber_input.error = "This field is Empty"
            return false
        }
        if (edit_owner_mobileNumber_input.text.toString().length != 10) {
            edit_owner_mobileNumber_input.error = "Mobile Number is Invalid"
            return false
        }
        if (edit_owner_state_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select State of Owner", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_owner_district_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select District of Owner", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_owner_pinCode_input.text.isNullOrBlank()) {
            edit_owner_pinCode_input.error = "This field is Empty"
            return false
        }
        if (edit_owner_address_input.text.isNullOrBlank()) {
            edit_owner_address_input.error = "This field is Empty"
            return false
        }
        if (edit_owner_email_input.text.isNullOrBlank()) {
            edit_owner_email_input.error = "This field is Empty"
            return false
        }
        if (edit_owner_passWord_input.text.isNullOrBlank()) {
            edit_owner_passWord_input.error = "This field is Empty"
            return false
        }
        if (edit_owner_passWord_input.text.toString().length < 7) {
            edit_owner_passWord_input.error = "Password length should be greater than 6"
            Toast.makeText(requireContext(), "Password id too Short", Toast.LENGTH_LONG).show()
            return false
        }
        if (edit_nearest_college_searchable_spinner.selectedItem == "") {
            Toast.makeText(requireContext(), "Please Select Nearest College", Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun roomDescriptionCheck(): Boolean {
        //1.
        if (edit_noOfPersonsPerRoom1_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Persons per Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfRooms1_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfACRooms1_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfNonACRooms1_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Non AC Room Field",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (edit_noOfAvailableACRooms1_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_noOfAvailableNonACRooms1_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available Non AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_rentOfACRoomsWithMess1_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithMess1_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithMess1_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithMess1_input.error = "This field is Empty"
        }
        if (edit_rentOfACRoomsWithoutMess1_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithoutMess1_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithoutMess1_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithoutMess1_input.error = "This field is Empty"
        }
        //2.
        if (edit_noOfPersonsPerRoom2_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Persons per Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfRooms2_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfACRooms2_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfNonACRooms2_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Non AC Room Field",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (edit_noOfAvailableACRooms2_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_noOfAvailableNonACRooms2_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available Non AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_rentOfACRoomsWithMess2_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithMess2_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithMess2_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithMess2_input.error = "This field is Empty"
        }
        if (edit_rentOfACRoomsWithoutMess2_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithoutMess2_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithoutMess2_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithoutMess2_input.error = "This field is Empty"
        }
        //3.
        if (edit_noOfPersonsPerRoom3_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Persons per Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfRooms3_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfACRooms3_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfNonACRooms3_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Non AC Room Field",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (edit_noOfAvailableACRooms3_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_noOfAvailableNonACRooms3_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available Non AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_rentOfACRoomsWithMess3_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithMess3_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithMess3_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithMess3_input.error = "This field is Empty"
        }
        if (edit_rentOfACRoomsWithoutMess3_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithoutMess3_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithoutMess3_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithoutMess3_input.error = "This field is Empty"
        }
        //4.
        if (edit_noOfPersonsPerRoom4_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Persons per Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfRooms4_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfACRooms4_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfNonACRooms4_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Non AC Room Field",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (edit_noOfAvailableACRooms4_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_noOfAvailableNonACRooms4_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available Non AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_rentOfACRoomsWithMess4_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithMess4_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithMess4_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithMess4_input.error = "This field is Empty"
        }
        if (edit_rentOfACRoomsWithoutMess4_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithoutMess4_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithoutMess4_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithoutMess4_input.error = "This field is Empty"
        }
        //5.
        if (edit_noOfPersonsPerRoom5_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Persons per Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfRooms5_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfACRooms5_searchable_spinner.selectedItem != "") {
            Toast.makeText(requireContext(), "Select Number of AC Room Field", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (edit_noOfNonACRooms5_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Non AC Room Field",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (edit_noOfAvailableACRooms5_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_noOfAvailableNonACRooms5_searchable_spinner.selectedItem != "") {
            Toast.makeText(
                requireContext(),
                "Select Number of Available Non AC Room Field",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        if (edit_rentOfACRoomsWithMess5_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithMess5_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithMess5_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithMess5_input.error = "This field is Empty"
        }
        if (edit_rentOfACRoomsWithoutMess5_input.text.isNullOrEmpty()) {
            edit_rentOfACRoomsWithoutMess5_input.error = "This field is Empty"
        }
        if (edit_rentOfNonACRoomsWithoutMess5_input.text.isNullOrEmpty()) {
            edit_rentOfNonACRoomsWithoutMess5_input.error = "This field is Empty"
        }
        return true
    }

    private fun retrieveData() {
        currentPGReference =
            FirebaseDatabase.getInstance().reference.child("PGs")
                .child(currentPGProfileID)
        currentPGReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val pg = snapshot.getValue(PG::class.java)
                if (pg!!.ownerDP != "")
                    Picasso.get().load(pg.ownerDP.toUri()).into(edit_pg_profile_image)
                edit_pg_name_input.setText(pg.pgName)
                when (pg.pgType) {
                    "Male" -> edit_pgType_radio_group.check(R.id.edit_malePg_radio_button)
                    "Female" -> edit_pgType_radio_group.check(R.id.edit_femalePg_radio_button)
                    else -> edit_pgType_radio_group.check(R.id.edit_bothPg_radio_button)
                }
                when (pg.messAvailable) {
                    "Yes" -> edit_messAvailable_radio_group.check(R.id.edit_yesMess_radio_button)
                    else -> edit_messAvailable_radio_group.check(R.id.edit_noMess_radio_button)
                }
                when (pg.liftAvailable) {
                    "Yes" -> edit_liftAvailable_radio_group.check(R.id.edit_yesLift_radio_button)
                    else -> edit_liftAvailable_radio_group.check(R.id.edit_noLift_radio_button)
                }
                edit_pg_pinCode_input.setText(pg.pgPincode)
                edit_pg_address_input.setText(pg.pgAddress)

                edit_rentOfACRoomsWithMess1_input.setText(pg.rentOfACRoomsWithMess[0])
                edit_rentOfACRoomsWithoutMess1_input.setText(pg.rentOfACRoomsWithoutMess[0])
                edit_rentOfNonACRoomsWithMess1_input.setText(pg.rentOfNonACRoomsWithMess[0])
                edit_rentOfNonACRoomsWithoutMess1_input.setText(pg.rentOfNonACRoomsWithoutMess[0])

                edit_rentOfACRoomsWithMess2_input.setText(pg.rentOfACRoomsWithMess[1])
                edit_rentOfACRoomsWithoutMess2_input.setText(pg.rentOfACRoomsWithoutMess[1])
                edit_rentOfNonACRoomsWithMess2_input.setText(pg.rentOfNonACRoomsWithMess[1])
                edit_rentOfNonACRoomsWithoutMess2_input.setText(pg.rentOfNonACRoomsWithoutMess[1])

                edit_rentOfACRoomsWithMess3_input.setText(pg.rentOfACRoomsWithMess[2])
                edit_rentOfACRoomsWithoutMess3_input.setText(pg.rentOfACRoomsWithoutMess[2])
                edit_rentOfNonACRoomsWithMess3_input.setText(pg.rentOfNonACRoomsWithMess[2])
                edit_rentOfNonACRoomsWithoutMess3_input.setText(pg.rentOfNonACRoomsWithoutMess[2])

                edit_rentOfACRoomsWithMess4_input.setText(pg.rentOfACRoomsWithMess[3])
                edit_rentOfACRoomsWithoutMess4_input.setText(pg.rentOfACRoomsWithoutMess[3])
                edit_rentOfNonACRoomsWithMess4_input.setText(pg.rentOfNonACRoomsWithMess[3])
                edit_rentOfNonACRoomsWithoutMess4_input.setText(pg.rentOfNonACRoomsWithoutMess[3])

                edit_rentOfACRoomsWithMess5_input.setText(pg.rentOfACRoomsWithMess[4])
                edit_rentOfACRoomsWithoutMess5_input.setText(pg.rentOfACRoomsWithoutMess[4])
                edit_rentOfNonACRoomsWithMess5_input.setText(pg.rentOfNonACRoomsWithMess[4])
                edit_rentOfNonACRoomsWithoutMess5_input.setText(pg.rentOfNonACRoomsWithoutMess[4])

                edit_owner_name_input.setText(pg.ownerName)
                when (pg.ownerGender) {
                    "Male" -> edit_owner_gender_radio_group.check(R.id.edit_owner_male_radio_button)
                    "Female" -> edit_owner_gender_radio_group.check(R.id.edit_owner_female_radio_button)
                    else -> edit_owner_gender_radio_group.check(R.id.edit_owner_others_radio_button)
                }
                edit_owner_dob_et.setText(pg.ownerDOB)
                edit_owner_mobileNumber_input.setText(pg.ownerNumber)
                edit_owner_pinCode_input.setText(pg.ownerPincode)
                edit_owner_address_input.setText(pg.ownerAddress)
                edit_owner_email_input.setText(pg.ownerEmail)
                edit_owner_passWord_input.setText(pg.ownerPassWord)
            }
        })
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
        edit_owner_dob_et.setText(sdf.format(myCalender.time))
    }

    private fun roomDescriptionWork() {

        //1.
        edit_noOfPersonsPerRoom1_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms1_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms1_searchable_spinner.onItemSelectedListener =
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
                    edit_noOfACRooms1_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    edit_noOfACRooms1_searchable_spinner.onItemSelectedListener =
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
                                edit_noOfNonACRooms1_searchable_spinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                edit_noOfAvailableACRooms1_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list02
                                    )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                edit_noOfAvailableNonACRooms1_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list03
                                    )
                            }
                        }
                }
            }

        //2.
        edit_noOfPersonsPerRoom2_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms2_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms2_searchable_spinner.onItemSelectedListener =
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
                    edit_noOfACRooms2_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    edit_noOfACRooms2_searchable_spinner.onItemSelectedListener =
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
                                edit_noOfNonACRooms2_searchable_spinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                edit_noOfAvailableACRooms2_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list02
                                    )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                edit_noOfAvailableNonACRooms2_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list03
                                    )
                            }
                        }
                }
            }

        //3.
        edit_noOfPersonsPerRoom3_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms3_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms3_searchable_spinner.onItemSelectedListener =
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
                    edit_noOfACRooms3_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    edit_noOfACRooms3_searchable_spinner.onItemSelectedListener =
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
                                edit_noOfNonACRooms3_searchable_spinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                edit_noOfAvailableACRooms3_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list02
                                    )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                edit_noOfAvailableNonACRooms3_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list03
                                    )
                            }
                        }
                }
            }

        //4.
        edit_noOfPersonsPerRoom4_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms4_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms4_searchable_spinner.onItemSelectedListener =
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
                    edit_noOfACRooms4_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    edit_noOfACRooms4_searchable_spinner.onItemSelectedListener =
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
                                edit_noOfNonACRooms4_searchable_spinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                edit_noOfAvailableACRooms4_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list02
                                    )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                edit_noOfAvailableNonACRooms4_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list03
                                    )
                            }
                        }
                }
            }

        //5.
        edit_noOfPersonsPerRoom5_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms5_searchable_spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_1,
            numberList
        )
        edit_noOfRooms5_searchable_spinner.onItemSelectedListener =
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
                    edit_noOfACRooms5_searchable_spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_expandable_list_item_1,
                        list
                    )
                    edit_noOfACRooms5_searchable_spinner.onItemSelectedListener =
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
                                edit_noOfNonACRooms5_searchable_spinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_expandable_list_item_1,
                                    list01
                                )
                                val list02 = mutableListOf<String>()
                                list02.add("NULL")
                                for (i in 1..(position + 1))
                                    list02.add(i.toString())
                                edit_noOfAvailableACRooms5_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list02
                                    )
                                val list03 = mutableListOf<String>()
                                list03.add("NULL")
                                for (i in 1..(noOfRooms - position))
                                    list03.add(i.toString())
                                edit_noOfAvailableNonACRooms5_searchable_spinner.adapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_expandable_list_item_1,
                                        list03
                                    )
                            }
                        }
                }
            }

    }

}