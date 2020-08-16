package udit.programmer.co.easypg.Adapter.CustomerSearch

import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_customer_search_layput.view.*
import udit.programmer.co.easypg.Models.Customer

class CustomerSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(customer: Customer) {
        itemView.item_name_customer.text = customer.name
        itemView.item_username_customer.text = customer.username
        Picasso.get().load(customer.image.toUri()).into(itemView.item_image_customer)
    }
}