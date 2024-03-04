package com.example.stadt_land_fluss.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.R

class ParentAdapter(private val parentList: List<ParentItem>) :
    RecyclerView.Adapter<ParentAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryForRating: TextView = itemView.findViewById(R.id.categoryForRating)
        val rvRatingUser: RecyclerView = itemView.findViewById(R.id.rvRatingUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parent_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = parentList[position]
        holder.categoryForRating.text = parentItem.categoryForRating
        holder.rvRatingUser.setHasFixedSize(true)

        holder.rvRatingUser.layoutManager = LinearLayoutManager(holder.itemView.context)
        val adapter = ChildAdapter(parentItem.rvRatingUser)
        holder.rvRatingUser.adapter = adapter
    }

    override fun getItemCount(): Int {
        return parentList.size
    }
}