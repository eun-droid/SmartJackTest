package com.yes.smartjacktest

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yes.smartjacktest.model.ImageItem
import kotlinx.android.synthetic.main.item_image.view.*

class SearchAdapter: RecyclerView.Adapter<SearchAdapter.ImageHolder>() {
    private var items: MutableList<ImageItem> = mutableListOf()
    private val placeholder = ColorDrawable(Color.GRAY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = ImageHolder(parent)

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        items[position].let { imageItem ->
            with(holder.itemView) {
                Glide.with(context)
                    .load(imageItem.thumbnail)
                    .placeholder(placeholder)
                    .into(ivItemImage)

                tvItemImageTitle.text = imageItem.title
            }
        }
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<ImageItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    fun addItems(items: List<ImageItem>) {
        val positionStart = this.itemCount
        this.items.addAll(items.toMutableList())
        notifyItemRangeInserted(positionStart, items.size)

    }

    fun clearItems() {
        this.items.clear()
    }

    class ImageHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false))
}