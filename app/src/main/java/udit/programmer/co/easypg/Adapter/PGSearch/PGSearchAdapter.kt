package udit.programmer.co.easypg.Adapter.PGSearch

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import udit.programmer.co.easypg.Interfaces.OnPGItemClickistener
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R

class PGSearchAdapter(context: Context, var pgs: MutableList<PG>?) :
    RecyclerView.Adapter<PGSearchViewHolder>() {

    lateinit var onPGItemClickistener: OnPGItemClickistener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGSearchViewHolder {
        return PGSearchViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pg_search_layout, parent, false)
        )
    }

    override fun getItemCount(): Int = pgs!!.size

    override fun onBindViewHolder(holder: PGSearchViewHolder, position: Int) {
        holder.bind(pgs!![position])
        holder.itemView.setOnClickListener {
            onPGItemClickistener.onClick(pgs!![position])
        }
    }
}