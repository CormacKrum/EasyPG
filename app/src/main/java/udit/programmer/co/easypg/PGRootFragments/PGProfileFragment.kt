package udit.programmer.co.easypg.PGRootFragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_p_g_add.*
import kotlinx.android.synthetic.main.fragment_p_g_profile.*
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfACRooms1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfACRooms2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfACRooms3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfACRooms4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfACRooms5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableACRooms1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableACRooms2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableACRooms3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableACRooms4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableACRooms5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableNonACRooms1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableNonACRooms2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableNonACRooms3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableNonACRooms4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfAvailableNonACRooms5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfRooms1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfRooms2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfRooms3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfRooms4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.noOfRooms5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithMess1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithMess2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithMess3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithMess4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithMess5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithoutMess1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithoutMess2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithoutMess3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithoutMess4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfACRoomsWithoutMess5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithMess1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithMess2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithMess3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithMess4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithMess5_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithoutMess1_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithoutMess2_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithoutMess3_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithoutMess4_tv
import kotlinx.android.synthetic.main.fragment_p_g_profile.rentOfNonACRoomsWithoutMess5_tv
import kotlinx.android.synthetic.main.fragment_profile.*
import udit.programmer.co.easypg.Adapter.PGImage.PGImageAdapter
import udit.programmer.co.easypg.LocationActivities.LocationActivity
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R
import udit.programmer.co.easypg.SignUpActivities.PG.PGImageActivity

class PGProfileFragment : Fragment() {

    private var currentPGProfileID = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentPGReference: DatabaseReference
    private lateinit var dialog: AlertDialog

    private var imageUriList = mutableListOf<String>()

    private lateinit var imageAdapter: PGImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_p_g_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        images_recycler_view_pg_profile.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        images_recycler_view_pg_profile.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(requireContext()).build()
        dialog.show()
        retrieveData()
        pgSet_images_btn_pg_profile.setOnClickListener {
            startActivity(Intent(requireContext(), PGImageActivity::class.java))
        }
        pgSet_location_btn_pg_profile.setOnClickListener {
            startActivity(Intent(requireContext(), LocationActivity::class.java))
        }
    }

    private fun retrieveData() {
        currentPGReference =
            FirebaseDatabase.getInstance().reference.child("PGs")
                .child(currentPGProfileID)
        currentPGReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val pg = snapshot.getValue(PG::class.java)
                pgName_pg_profile.text = pg!!.pgName
                pgState_pg_profile.text = pg.pgState
                pgDistrict_pg_profile.text = pg.pgDistrict
                when (pg.pgType) {
                    "Male" -> {
                        pgType_pg_profile.text = "Boys"
                    }
                    "Female" -> {
                        pgType_pg_profile.text = "Girls"
                    }
                    else -> {
                        pgType_pg_profile.text = "Both"
                    }
                }
                if (pg.messAvailable == "Yes") {
                    pgMess_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.DodgerBlue)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24)
                    pgMess_pg_profile.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                } else {
                    pgMess_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.Red)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                    pgMess_pg_profile.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                }
                if (pg.liftAvailable == "Yes") {
                    pgLift_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.DodgerBlue)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24)
                    pgLift_pg_profile.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                } else {
                    pgLift_pg_profile.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.Red)
                    )
                    var img =
                        resources.getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                    pgLift_pg_profile.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                }
                pgAddress_pg_profile.text = pg.pgAddress

                imageUriList.addAll(pg.images)
                imageRetrievingWork()

                personsPerRoom1_tv.text = pg.personsPerRoom[0]
                personsPerRoom2_tv.text = pg.personsPerRoom[1]
                personsPerRoom3_tv.text = pg.personsPerRoom[2]
                personsPerRoom4_tv.text = pg.personsPerRoom[3]
                personsPerRoom5_tv.text = pg.personsPerRoom[4]

                noOfRooms1_tv.text = pg.numberOfRooms[0]
                noOfRooms2_tv.text = pg.numberOfRooms[1]
                noOfRooms3_tv.text = pg.numberOfRooms[2]
                noOfRooms4_tv.text = pg.numberOfRooms[3]
                noOfRooms5_tv.text = pg.numberOfRooms[4]

                noOfACRooms1_tv.text = pg.numberOfACRooms[0]
                noOfACRooms2_tv.text = pg.numberOfACRooms[1]
                noOfACRooms3_tv.text = pg.numberOfACRooms[2]
                noOfACRooms4_tv.text = pg.numberOfACRooms[3]
                noOfACRooms5_tv.text = pg.numberOfACRooms[4]

                noOfNonACRooms1_tv.text = pg.numberOfNonACRooms[0]
                noOfNonACRooms2_tv.text = pg.numberOfNonACRooms[1]
                noOfNonACRooms3_tv.text = pg.numberOfNonACRooms[2]
                noOfNonACRooms4_tv.text = pg.numberOfNonACRooms[3]
                noOfNonACRooms5_tv.text = pg.numberOfNonACRooms[4]

                noOfAvailableACRooms1_tv.text = pg.availableNumberOfACRooms[0]
                noOfAvailableACRooms2_tv.text = pg.availableNumberOfACRooms[1]
                noOfAvailableACRooms3_tv.text = pg.availableNumberOfACRooms[2]
                noOfAvailableACRooms4_tv.text = pg.availableNumberOfACRooms[3]
                noOfAvailableACRooms5_tv.text = pg.availableNumberOfACRooms[4]

                noOfAvailableNonACRooms1_tv.text = pg.avaliableNumberOfNonACRooms[0]
                noOfAvailableNonACRooms2_tv.text = pg.avaliableNumberOfNonACRooms[1]
                noOfAvailableNonACRooms3_tv.text = pg.avaliableNumberOfNonACRooms[2]
                noOfAvailableNonACRooms4_tv.text = pg.avaliableNumberOfNonACRooms[3]
                noOfAvailableNonACRooms5_tv.text = pg.avaliableNumberOfNonACRooms[4]

                rentOfACRoomsWithMess1_tv.text = pg.rentOfACRoomsWithMess[0]
                rentOfACRoomsWithMess2_tv.text = pg.rentOfACRoomsWithMess[1]
                rentOfACRoomsWithMess3_tv.text = pg.rentOfACRoomsWithMess[2]
                rentOfACRoomsWithMess4_tv.text = pg.rentOfACRoomsWithMess[3]
                rentOfACRoomsWithMess5_tv.text = pg.rentOfACRoomsWithMess[4]

                rentOfACRoomsWithoutMess1_tv.text = pg.rentOfACRoomsWithoutMess[0]
                rentOfACRoomsWithoutMess2_tv.text = pg.rentOfACRoomsWithoutMess[1]
                rentOfACRoomsWithoutMess3_tv.text = pg.rentOfACRoomsWithoutMess[2]
                rentOfACRoomsWithoutMess4_tv.text = pg.rentOfACRoomsWithoutMess[3]
                rentOfACRoomsWithoutMess5_tv.text = pg.rentOfACRoomsWithoutMess[4]

                rentOfNonACRoomsWithMess1_tv.text = pg.rentOfNonACRoomsWithMess[0]
                rentOfNonACRoomsWithMess2_tv.text = pg.rentOfNonACRoomsWithMess[1]
                rentOfNonACRoomsWithMess3_tv.text = pg.rentOfNonACRoomsWithMess[2]
                rentOfNonACRoomsWithMess4_tv.text = pg.rentOfNonACRoomsWithMess[3]
                rentOfNonACRoomsWithMess5_tv.text = pg.rentOfNonACRoomsWithMess[4]

                rentOfNonACRoomsWithoutMess1_tv.text = pg.rentOfNonACRoomsWithoutMess[0]
                rentOfNonACRoomsWithoutMess2_tv.text = pg.rentOfNonACRoomsWithoutMess[1]
                rentOfNonACRoomsWithoutMess3_tv.text = pg.rentOfNonACRoomsWithoutMess[2]
                rentOfNonACRoomsWithoutMess4_tv.text = pg.rentOfNonACRoomsWithoutMess[3]
                rentOfNonACRoomsWithoutMess5_tv.text = pg.rentOfNonACRoomsWithoutMess[4]

                owner_name_pg_tv.text = pg.ownerName
                owner_email_pg_tv.text = pg.ownerEmail
                owner_gender_pg_tv.text = pg.ownerGender
                if (pg.ownerDP != "")
                    Picasso.get().load(pg.ownerDP.toUri()).into(image_owner_pg_profile_tv)
                owner_mobile_number_pg_tv.text = pg.ownerNumber
                owner_address_pg_tv.text = pg.ownerAddress
                owner_state_pg_tv.text = pg.ownerState
                owner_district_pg_tv.text = pg.ownerDistrict
                owner_pinCode_pg_tv.text = pg.ownerPincode
                dialog.dismiss()
            }
        })
    }

    private fun imageRetrievingWork() {
        imageAdapter = PGImageAdapter(imageUriList)
        images_recycler_view_pg_profile.adapter = imageAdapter
    }

}