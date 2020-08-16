package udit.programmer.co.easypg.RootFragments.BottomFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import udit.programmer.co.easypg.Adapter.PGSearch.PGSearchAdapter
import udit.programmer.co.easypg.Interfaces.OnPGItemClickistener
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R
import udit.programmer.co.easypg.SearchProfiles.PGProfileActivity

class SearchFragment : Fragment() {

    private lateinit var pgAdapter: PGSearchAdapter
    private var pgList = mutableListOf<PG>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pgSearchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchPGs(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.equals("")) pgList.clear()
                newText?.let { searchPGs(it) }
                return true
            }
        })

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        pg_search_recycler_view.layoutManager = layoutManager
        pg_search_recycler_view.setHasFixedSize(true)

        pgAdapter = PGSearchAdapter(requireContext(), pgList)
        pgAdapter.onPGItemClickistener = object : OnPGItemClickistener {
            override fun onClick(pg: PG) {
                startActivity(
                    Intent(
                        requireContext(),
                        PGProfileActivity::class.java
                    ).putExtra("Ceased Meteor", pg.pgId)
                )
            }
        }
        pg_search_recycler_view.adapter = pgAdapter

    }

    private fun searchPGs(it: String): Any {
        val pgsReference = FirebaseDatabase.getInstance().reference.child("PGs")

        val query = FirebaseDatabase.getInstance().reference
            .child("PGs")
            .orderByChild("pgName")
            .startAt(it)
            .endAt(it + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                pgList.clear()
                for (snapShot in dataSnapshot.children) {
                    val pg = snapShot.getValue(PG::class.java)
                    if (pg != null)
                        pgList.add(pg)
                }
            }
        })
        return pgsReference
    }

}