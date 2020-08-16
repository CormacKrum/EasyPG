package udit.programmer.co.easypg.PGRootFragments

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
import kotlinx.android.synthetic.main.fragment_p_g_search.*
import udit.programmer.co.easypg.Interfaces.OnCustomerItemClickListener
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R
import udit.programmer.co.easypg.SearchProfiles.CustomerProfileActivity
import udit.programmer.co.payingguest.Adapter.CustomerSearch.CustomerSearchAdapter

class PGSearchFragment : Fragment() {

    private lateinit var customerAdapter: CustomerSearchAdapter
    private var customerList = mutableListOf<Customer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_p_g_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerSearchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchCustomers(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.equals("")) customerList!!.clear()
                newText?.let { searchCustomers(it) }
                return true
            }
        })

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        customer_search_recycler_view.layoutManager = layoutManager
        customer_search_recycler_view.setHasFixedSize(true)

        customerAdapter = CustomerSearchAdapter(requireContext(), customerList)
        customerAdapter.onCustomerItemClickListener = object : OnCustomerItemClickListener {
            override fun onClick(customer: Customer) {
                startActivity(
                    Intent(
                        requireContext(), CustomerProfileActivity::class.java
                    ).putExtra("Ceased Meteor", customer.customerId)
                )
            }
        }
        customer_search_recycler_view.adapter = customerAdapter

    }

    private fun searchCustomers(it: String): Any {
        val customersReference = FirebaseDatabase.getInstance().reference.child("Customers")

        val query = FirebaseDatabase.getInstance().reference
            .child("Customers")
            .orderByChild("name")
            .startAt(it)
            .endAt(it + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                customerList.clear()
                for (snapShot in dataSnapshot.children) {
                    val customer = snapShot.getValue(Customer::class.java)
                    if (customer != null)
                        customerList.add(customer)
                }
            }
        })
        return customersReference
    }
}