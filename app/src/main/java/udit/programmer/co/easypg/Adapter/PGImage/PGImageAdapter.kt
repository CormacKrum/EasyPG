package udit.programmer.co.easypg.Adapter.PGImage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import udit.programmer.co.easypg.R

class PGImageAdapter(var imageUriList: List<String>) : RecyclerView.Adapter<PGImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGImageViewHolder {
        return PGImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pg_image_layout, parent, false)
        )
    }

    override fun getItemCount(): Int = 6

    override fun onBindViewHolder(holder: PGImageViewHolder, position: Int) {
        holder.bind(imageUriList[position])
    }
}