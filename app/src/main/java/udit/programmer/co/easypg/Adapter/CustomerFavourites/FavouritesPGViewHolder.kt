package udit.programmer.co.easypg.Adapter.CustomerFavourites

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_pg_search_layout.view.*
import udit.programmer.co.easypg.Models.PG

class FavouritesPGViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
    fun bind(pg : PG){
        itemView.item_name_pg.text = pg.pgName
    }
}