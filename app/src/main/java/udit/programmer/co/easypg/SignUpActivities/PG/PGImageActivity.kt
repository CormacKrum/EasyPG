package udit.programmer.co.easypg.SignUpActivities.PG

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_p_g_image.*
import udit.programmer.co.easypg.R

class PGImageActivity : AppCompatActivity() {

    private var uri: Uri? = null
    private lateinit var dialog: AlertDialog

    private lateinit var firebaseStorage: StorageReference
    private lateinit var filePath: StorageReference

    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseUser: FirebaseUser

    private var images = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_p_g_image)

        firebaseStorage = FirebaseStorage.getInstance().reference
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("PGs")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

        upload_btn.isClickable = false
        done_btn.isClickable = false

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE),
                2222
            )
        } else {
            selectionWork()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2222 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectionWork()
            }
        }
    }

    private fun selectionWork() {
        select_btn.setOnClickListener {
            CropImage.activity().setAspectRatio(1, 1).start(this)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            when (activity_tv.text) {
                "Select Image" -> {
                    activity_tv.text = "First Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_01)
                    uploadingWork(1)
                }
                "First Image Uploaded :)" -> {
                    activity_tv.text = "Second Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_02)
                    uploadingWork(2)
                }
                "Second Image Uploaded :)" -> {
                    activity_tv.text = "Third Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_03)
                    uploadingWork(3)
                }
                "Third Image Uploaded :)" -> {
                    activity_tv.text = "Fourth Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_04)
                    uploadingWork(4)
                }
                "Fourth Image Uploaded :)" -> {
                    activity_tv.text = "Fifth Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_05)
                    uploadingWork(5)
                }
                "Fifth Image Uploaded :)" -> {
                    activity_tv.text = "Sixth Image Selected :)"
                    select_btn.isClickable = false
                    upload_btn.isClickable = true
                    uri = CropImage.getActivityResult(data).uri
                    Picasso.get().load(uri).into(image_06)
                    uploadingWork(6)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun uploadingWork(i: Int) {
        upload_btn.setOnClickListener {
            dialog.show()
            filePath =
                firebaseStorage.child("PGs").child("Images").child(firebaseUser.uid + i)
            filePath.putFile(uri!!).addOnCompleteListener {
                Toast.makeText(this, "Uploaded Successfully :)", Toast.LENGTH_LONG).show()
                when (i) {
                    1 -> activity_tv.text = "First Image Uploaded :)"
                    2 -> activity_tv.text = "Second Image Uploaded :)"
                    3 -> activity_tv.text = "Third Image Uploaded :)"
                    4 -> activity_tv.text = "Fourth Image Uploaded :)"
                    5 -> activity_tv.text = "Fifth Image Uploaded :)"
                    6 -> activity_tv.text = "Sixth Image Uploaded :)"
                }
                upload_btn.isClickable = false
                if (i != 6)
                    select_btn.isClickable = true
            }.addOnFailureListener {
                Toast.makeText(this, "Uploading Failed $it :(", Toast.LENGTH_LONG).show()
            }.continueWithTask {
                filePath.downloadUrl
            }.addOnCompleteListener {
                if (it.isComplete) {
                    uri = it.result
                    images.add(uri!!.toString())
                    dialog.dismiss()
                    if (i == 6) doneWork()
                }
            }.addOnFailureListener {
            }
        }
    }

    private fun doneWork() {
        done_btn.isClickable = true
        done_btn.setOnClickListener {
            dialog.show()
            databaseWork()
            onBackPressed()
        }
    }

    private fun databaseWork() {
        var map = mutableMapOf<String, Any>()
        map["images"] = images
        firebaseDatabase.child(firebaseUser.uid).updateChildren(map).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(this, "Uploaded Successfully :)", Toast.LENGTH_LONG).show()
        }
    }

}