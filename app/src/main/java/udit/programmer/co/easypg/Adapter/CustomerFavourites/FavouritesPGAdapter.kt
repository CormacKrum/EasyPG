package udit.programmer.co.easypg.Adapter.CustomerFavourites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import udit.programmer.co.easypg.Interfaces.OnFavouritesIPGClickListener
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R

class FavouritesPGAdapter(var list: MutableList<PG>) :
    RecyclerView.Adapter<FavouritesPGViewHolder>() {

    var onFavouritesIPGClickListener: OnFavouritesIPGClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesPGViewHolder {
        return FavouritesPGViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pg_search_layout, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: FavouritesPGViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            onFavouritesIPGClickListener!!.onClick(list[position])
        }
    }
}