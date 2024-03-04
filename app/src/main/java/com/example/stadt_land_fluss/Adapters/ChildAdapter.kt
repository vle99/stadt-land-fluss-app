package com.example.stadt_land_fluss.Adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.R

class ChildAdapter(private val childList: List<ChildItem>) :
    RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ratingUser: TextView = itemView.findViewById(R.id.rating_user)
        val ratingAnswer: TextView = itemView.findViewById(R.id.rating_answer)
        val ratingChecked: CheckBox = itemView.findViewById(R.id.rating_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.child_item, parent, false)
        return ChildViewHolder(view)
    }


    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.ratingUser.text = childList[position].ratingUser
        holder.ratingAnswer.text = childList[position].ratingAnswer
        holder.ratingChecked.isChecked = childList[position].ratingChecked
    }

    override fun getItemCount(): Int {
        return childList.size
    }

}