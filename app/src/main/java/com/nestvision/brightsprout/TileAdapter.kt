package com.nestvision.brightsprout

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TileAdapter<T>(
    private var items: List<T>,
    private val itemToString: (T) -> String,
    private val onItemClick: (T) -> Unit
) : RecyclerView.Adapter<TileAdapter.TileViewHolder>() {

    private var initialFocusRequested = false

    class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tile_title)
        val cardView: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tile_item, parent, false)
        return TileViewHolder(view)
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = itemToString(item)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true

        holder.itemView.post {
            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.08f else 1.0f)
                    .scaleY(if (hasFocus) 1.08f else 1.0f)
                    .setDuration(150)
                    .start()

                // Highlight and revert background color
                if (hasFocus) {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#00BCD4"))
                    holder.titleText.setTextColor(Color.BLACK)
                } else {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#444444")) // your default grey
                    holder.titleText.setTextColor(Color.WHITE)
                }
            }

            // Auto-focus first tile only once after update
            if (!initialFocusRequested && position == 0 && holder.itemView.isShown) {
                holder.itemView.requestFocus()
                initialFocusRequested = true
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<T>) {
        items = newItems
        initialFocusRequested = false // reset focus flag
        notifyDataSetChanged()
    }
}
