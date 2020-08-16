package udit.programmer.co.payingguest.Adapter.CustomerSearch

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import udit.programmer.co.easypg.Adapter.CustomerSearch.CustomerSearchViewHolder
import udit.programmer.co.easypg.Interfaces.OnCustomerItemClickListener
import udit.programmer.co.easypg.Models.Customer
import udit.programmer.co.easypg.R

class CustomerSearchAdapter(context: Context, var customers: MutableList<Customer>?) :
    RecyclerView.Adapter<CustomerSearchViewHolder>() {

    lateinit var onCustomerItemClickListener: OnCustomerItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerSearchViewHolder {
        return CustomerSearchViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_customer_search_layput, parent, false)
        )
    }

    override fun getItemCount(): Int = customers!!.size

    override fun onBindViewHolder(holder: CustomerSearchViewHolder, position: Int) {
        holder.bind(customers!![position])
        holder.itemView.setOnClickListener {
            onCustomerItemClickListener.onClick(customers!![position])
        }
    }

}