package udit.programmer.co.easypg.Adapter.PGImage

import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_pg_image_layout.view.*

class PGImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(imageURI: String) {
        Picasso.get().load(imageURI.toUri()).into(itemView.pg_image_item)
    }
}