package udit.programmer.co.easypg.RootFragments.SideFragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_favourites.*
import udit.programmer.co.easypg.Adapter.CustomerFavourites.FavouritesPGAdapter
import udit.programmer.co.easypg.Interfaces.OnFavouritesIPGClickListener
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R
import udit.programmer.co.easypg.SearchProfiles.PGProfileActivity

class FavouritesFragment : Fragment() {

    private lateinit var pgAdapter: FavouritesPGAdapter
    private var favourites = mutableListOf<String>()
    private var allPgList = mutableMapOf<String, PG>()
    private var favouritePGs = mutableListOf<PG>()
    private lateinit var dialog : AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        dialog.show()

        retrieveAllPGs()
        retrieveCustomersFavouriteList()

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        fav_recycler_view.layoutManager = layoutManager
        fav_recycler_view.setHasFixedSize(true)

        pgAdapter = FavouritesPGAdapter(favouritePGs)
        pgAdapter.onFavouritesIPGClickListener = object : OnFavouritesIPGClickListener {
            override fun onClick(pg: PG) {
                startActivity(
                    Intent(
                        requireContext(),
                        PGProfileActivity::class.java
                    ).putExtra("Ceased Meteor", pg.pgId)
                )
            }
        }
        fav_recycler_view.adapter = pgAdapter
        pgAdapter.notifyDataSetChanged()
    }

    private fun retrieveCustomersFavouriteList() {
        FirebaseDatabase.getInstance().getReference("Customers")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val customer = snapshot.getValue(Customer::class.java)
                    favourites = customer!!.favourites!!
                    for (pgid in favourites)
                        if (allPgList.containsKey(pgid))
                            favouritePGs.add(allPgList.get(pgid)!!)
                    dialog.dismiss()
                }
            })
    }

    private fun retrieveAllPGs() {
        FirebaseDatabase.getInstance().getReference("PGs")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pg = snapshot.getValue(PG::class.java)
                    allPgList[pg!!.pgId] = pg
                }
            })
    }

}