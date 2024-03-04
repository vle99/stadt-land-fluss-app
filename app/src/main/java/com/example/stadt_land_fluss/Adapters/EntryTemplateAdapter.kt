package com.example.stadt_land_fluss.Adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.R
import kotlin.coroutines.coroutineContext

//https://demonuts.com/kotlin-recyclerview-with-edittext/

class EditAdapter(ctx: Context, editModelArrayLists: ArrayList<EditModel>) :
    RecyclerView.Adapter<EditAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(ctx)
        editModelArrayList = editModelArrayLists
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditAdapter.MyViewHolder {
        val view = inflater.inflate(R.layout.edittext_template, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditAdapter.MyViewHolder, position: Int) {
        holder.editText.setText(editModelArrayList[position].getEditTextValue())
        holder.category.text = editModelArrayList[position].category
    }

    override fun getItemCount(): Int {
        return editModelArrayList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var editText: EditText
        var category : TextView = itemView.findViewById(R.id.templateCategory)

        init {
            editText = itemView.findViewById(R.id.templateEditText) as EditText
            editText.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                    editModelArrayList[adapterPosition].setEditTextValue(editText.text.toString())
                }

                override fun afterTextChanged(editable: Editable) {

                }
            })
        }
    }

    companion object {
        lateinit var editModelArrayList: ArrayList<EditModel>
    }
}

class EditModel {

    private var editTextValue: String? = null

    fun getEditTextValue(): String? {
        return editTextValue
    }

    fun setEditTextValue(editTextValue: String) {
        this.editTextValue = editTextValue
    }

    var category : String = ""

}
