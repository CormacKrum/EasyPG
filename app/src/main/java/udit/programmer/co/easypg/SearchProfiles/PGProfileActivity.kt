package udit.programmer.co.easypg.SearchProfiles

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_p_g_profile.*
import udit.programmer.co.easypg.Adapter.PGImage.PGImageAdapter
import udit.programmer.co.easypg.ChatActivities.ChatActivity
import udit.programmer.co.easypg.LocationActivities.PGLocateActivity
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R

class PGProfileActivity : AppCompatActivity() {

    private var favTemp = false
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private lateinit var dialog: AlertDialog
    private var imageUriList = mutableListOf<String>()
    private lateinit var imageAdapter: PGImageAdapter

    private var favouroteList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_g_profile)

        databaseReference = FirebaseDatabase.getInstance()

        search_images_recycler_view_pg_profile.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        search_images_recycler_view_pg_profile.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.show()

        //locate , favourite , Call and message work
        val pgid = intent.getStringExtra("Ceased Meteor")

        retrievePGdata(pgid)

        retrieveCustomerData(pgid)

        search_pgFav_btn_pg_profile.setOnClickListener {
            favouriteWork(pgid)
        }
    }

    private fun favouriteWork(pgid: String) {
        if (!favTemp) {
            favTemp = true
            val img = resources.getDrawable(R.drawable.ic_baseline_favorite_24)
            search_pgFav_btn_pg_profile.setImageDrawable(img)
            favouroteList.add(pgid)
            val map = mutableMapOf<String, Any>()
            map["favourites"] = favouroteList
            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(map)
                .addOnCompleteListener {
                    Toast.makeText(this, "Added to favourites", Toast.LENGTH_LONG).show()
                }
        } else {
            favTemp = false
            val img = resources.getDrawable(R.drawable.ic_baseline_favorite_border_24)
            search_pgFav_btn_pg_profile.setImageDrawable(img)
            favouroteList.remove(pgid)
            val map = mutableMapOf<String, Any>()
            map["favourites"] = favouroteList
            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(map)
                .addOnCompleteListener {
                    Toast.makeText(this, "Removed from favourites", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun retrieveCustomerData(pgid: String) {
        FirebaseDatabase.getInstance().reference.child("Customers")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val customer = snapshot.getValue(Customer::class.java)
                    favouroteList = customer!!.favourites!!
                    if (favouroteList.contains(pgid)) {
                        favTemp = true
                        val img = resources.getDrawable(R.drawable.ic_baseline_favorite_24)
                        search_pgFav_btn_pg_profile.setImageDrawable(img)
                    }
                }
            })
    }

    private fun retrievePGdata(pgid: String?) {
        databaseRef =
            FirebaseDatabase.getInstance().reference.child("PGs")
                .child(pgid!!)
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val pg = snapshot.getValue(PG::class.java)
                search_pgName_pg_profile.text = pg!!.pgName
                search_pgState_pg_profile.text = pg.pgState
                search_pgDistrict_pg_profile.text = pg.pgDistrict
                when (pg.pgType) {
                    "Male" -> {
                        search_pgType_pg_profile.text = "Boys"
                    }
                    "Female" -> {
                        search_pgType_pg_profile.text = "Girls"
                    }
                    else -> {
                        search_pgType_pg_profile.text = "Both"
                    }
                }
                if (pg.messAvailable == "Yes") {
                    search_pgMess_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(this@PGProfileActivity, R.color.DodgerBlue)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24)
                    search_pgMess_pg_profile.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        img,
                        null
                    )
                } else {
                    search_pgMess_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(this@PGProfileActivity, R.color.Red)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                    search_pgMess_pg_profile.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        img,
                        null
                    )
                }
                if (pg.liftAvailable == "Yes") {
                    search_pgLift_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(this@PGProfileActivity, R.color.DodgerBlue)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24)
                    search_pgLift_pg_profile.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        img,
                        null
                    )
                } else {
                    search_pgLift_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(this@PGProfileActivity, R.color.Red)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                    search_pgLift_pg_profile.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        img,
                        null
                    )
                }
                search_pgAddress_pg_profile.text = pg.pgAddress

                search_pgCall_btn_pg_profile.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + pg.ownerNumber)))
                }
                search_pgMessage_btn_pg_profile.setOnClickListener {
                    startActivity(Intent(this@PGProfileActivity, ChatActivity::class.java))
                }
                search_pgLocation_btn_pg_profile.setOnClickListener {
                    startActivity(
                        Intent(
                            this@PGProfileActivity,
                            PGLocateActivity::class.java
                        ).putExtra("Ceased Meteor", pgid)
                    )
                }
                imageUriList.addAll(pg.images)
                imageRetrievingWork()

                search_personsPerRoom1_tv.text = pg.personsPerRoom[0]
                search_personsPerRoom2_tv.text = pg.personsPerRoom[1]
                search_personsPerRoom3_tv.text = pg.personsPerRoom[2]
                search_personsPerRoom4_tv.text = pg.personsPerRoom[3]
                search_personsPerRoom5_tv.text = pg.personsPerRoom[4]

                search_noOfRooms1_tv.text = pg.numberOfRooms[0]
                search_noOfRooms2_tv.text = pg.numberOfRooms[1]
                search_noOfRooms3_tv.text = pg.numberOfRooms[2]
                search_noOfRooms4_tv.text = pg.numberOfRooms[3]
                search_noOfRooms5_tv.text = pg.numberOfRooms[4]

                search_noOfACRooms1_tv.text = pg.numberOfACRooms[0]
                search_noOfACRooms2_tv.text = pg.numberOfACRooms[1]
                search_noOfACRooms3_tv.text = pg.numberOfACRooms[2]
                search_noOfACRooms4_tv.text = pg.numberOfACRooms[3]
                search_noOfACRooms5_tv.text = pg.numberOfACRooms[4]

                search_noOfNonACRooms1_tv.text = pg.numberOfNonACRooms[0]
                search_noOfNonACRooms2_tv.text = pg.numberOfNonACRooms[1]
                search_noOfNonACRooms3_tv.text = pg.numberOfNonACRooms[2]
                search_noOfNonACRooms4_tv.text = pg.numberOfNonACRooms[3]
                search_noOfNonACRooms5_tv.text = pg.numberOfNonACRooms[4]

                search_noOfAvailableACRooms1_tv.text = pg.availableNumberOfACRooms[0]
                search_noOfAvailableACRooms2_tv.text = pg.availableNumberOfACRooms[1]
                search_noOfAvailableACRooms3_tv.text = pg.availableNumberOfACRooms[2]
                search_noOfAvailableACRooms4_tv.text = pg.availableNumberOfACRooms[3]
                search_noOfAvailableACRooms5_tv.text = pg.availableNumberOfACRooms[4]

                search_noOfAvailableNonACRooms1_tv.text = pg.avaliableNumberOfNonACRooms[0]
                search_noOfAvailableNonACRooms2_tv.text = pg.avaliableNumberOfNonACRooms[1]
                search_noOfAvailableNonACRooms3_tv.text = pg.avaliableNumberOfNonACRooms[2]
                search_noOfAvailableNonACRooms4_tv.text = pg.avaliableNumberOfNonACRooms[3]
                search_noOfAvailableNonACRooms5_tv.text = pg.avaliableNumberOfNonACRooms[4]

                search_rentOfACRoomsWithMess1_tv.text = pg.rentOfACRoomsWithMess[0]
                search_rentOfACRoomsWithMess2_tv.text = pg.rentOfACRoomsWithMess[1]
                search_rentOfACRoomsWithMess3_tv.text = pg.rentOfACRoomsWithMess[2]
                search_rentOfACRoomsWithMess4_tv.text = pg.rentOfACRoomsWithMess[3]
                search_rentOfACRoomsWithMess5_tv.text = pg.rentOfACRoomsWithMess[4]

                search_rentOfACRoomsWithoutMess1_tv.text = pg.rentOfACRoomsWithoutMess[0]
                search_rentOfACRoomsWithoutMess2_tv.text = pg.rentOfACRoomsWithoutMess[1]
                search_rentOfACRoomsWithoutMess3_tv.text = pg.rentOfACRoomsWithoutMess[2]
                search_rentOfACRoomsWithoutMess4_tv.text = pg.rentOfACRoomsWithoutMess[3]
                search_rentOfACRoomsWithoutMess5_tv.text = pg.rentOfACRoomsWithoutMess[4]

                search_rentOfNonACRoomsWithMess1_tv.text = pg.rentOfNonACRoomsWithMess[0]
                search_rentOfNonACRoomsWithMess2_tv.text = pg.rentOfNonACRoomsWithMess[1]
                search_rentOfNonACRoomsWithMess3_tv.text = pg.rentOfNonACRoomsWithMess[2]
                search_rentOfNonACRoomsWithMess4_tv.text = pg.rentOfNonACRoomsWithMess[3]
                search_rentOfNonACRoomsWithMess5_tv.text = pg.rentOfNonACRoomsWithMess[4]

                search_rentOfNonACRoomsWithoutMess1_tv.text = pg.rentOfNonACRoomsWithoutMess[0]
                search_rentOfNonACRoomsWithoutMess2_tv.text = pg.rentOfNonACRoomsWithoutMess[1]
                search_rentOfNonACRoomsWithoutMess3_tv.text = pg.rentOfNonACRoomsWithoutMess[2]
                search_rentOfNonACRoomsWithoutMess4_tv.text = pg.rentOfNonACRoomsWithoutMess[3]
                search_rentOfNonACRoomsWithoutMess5_tv.text = pg.rentOfNonACRoomsWithoutMess[4]

                search_owner_name_pg_tv.text = pg.ownerName
                search_owner_email_pg_tv.text = pg.ownerEmail
                search_owner_gender_pg_tv.text = pg.ownerGender
                if (pg.ownerDP != "")
                    Picasso.get().load(pg.ownerDP.toUri()).into(search_image_owner_pg_profile_tv)
                search_owner_mobile_number_pg_tv.text = pg.ownerNumber
                search_owner_address_pg_tv.text = pg.ownerAddress
                search_owner_state_pg_tv.text = pg.ownerState
                search_owner_district_pg_tv.text = pg.ownerDistrict
                search_owner_pinCode_pg_tv.text = pg.ownerPincode
                dialog.dismiss()
            }
        })
    }

    private fun imageRetrievingWork() {
        imageAdapter = PGImageAdapter(imageUriList)
        search_images_recycler_view_pg_profile.adapter = imageAdapter
    }

}