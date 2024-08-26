package com.example.ozan.videoapp22.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ozan.videoapp22.Data.Songs
import com.example.ozan.videoapp22.R

class RecyclerViewImg(private val context: Context) :
    RecyclerView.Adapter<RecyclerViewImg.RecyclerViewItemHolder>() {
    private var recyclerItemValues = emptyList<Songs>()

    fun setData(items: List<Songs>) {
        recyclerItemValues = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView: View = inflater.inflate(R.layout.recyclerview_img, parent, false)
        return RecyclerViewItemHolder(itemView)
    }

    override fun getItemCount(): Int {
        return recyclerItemValues.size
    }

    override fun onBindViewHolder(holder: RecyclerViewItemHolder, position: Int) {
        val item = recyclerItemValues[position]
        holder.bind(item)
    }

    inner class RecyclerViewItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val parentLayout: LinearLayout = itemView.findViewById(R.id.itemLayout)
        private val coverImg: ImageView = itemView.findViewById(R.id.coverimg)

        fun bind(song: Songs) {
            Glide.with(context)
                .load(song.coverimg)
                .into(coverImg)

        }
    }
}
