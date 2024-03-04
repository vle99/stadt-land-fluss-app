package com.example.stadt_land_fluss.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.R

class EntryFieldAdapter(private val mList: ArrayList<String>) : RecyclerView.Adapter<EntryFieldAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_template, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val vm = mList[position]
        holder.category.text = vm

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        val vmlist = mList
        return vmlist.size
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val category : TextView = itemView.findViewById(R.id.categoryText)
    }
}