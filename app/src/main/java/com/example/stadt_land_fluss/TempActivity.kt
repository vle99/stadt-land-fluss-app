package com.example.stadt_land_fluss

import androidx.recyclerview.widget.RecyclerView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stadt_land_fluss.Adapters.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.toList


class TempActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val parentList = ArrayList<ParentItem>()
    val mSocket = SocketManager.getSocket()
    private val allcategories : ArrayList<String> = ArrayList<String>()
    private lateinit var submit : Button
    private lateinit var jsonString : JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)

        submit = findViewById(R.id.btnSubmitRating)

        val roomObject = JSONObject().put("room_name", "Raum1")
        mSocket.emit("fetch-all-categories", roomObject)
        mSocket.emit("get-answers", "Raum1")

        recyclerView = findViewById(R.id.parentRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        submit.setOnClickListener {

        }


        mSocket.on("all-info") { args ->
            if (args[0] != null) {
                val categoriesandanswersarraylist: ArrayList<ParentItem> = ArrayList<ParentItem>()
                val jsonarray = args[0] as JSONArray

                for (i in 0 until jsonarray.length()) {
                    val jsonObject = jsonarray.getJSONObject(i)
                    val allcategories = jsonObject.keys()
                    val childitemsarraylist: ArrayList<ChildItem> = ArrayList()

                    for (category in allcategories) {
                        val jsonArray_user = jsonObject.getJSONObject(category).getJSONArray("user")
                        val jsonArray_antwort = jsonObject.getJSONObject(category).getJSONArray("antwort")
                        val jsonArray_answer_score = jsonObject.getJSONObject(category).getJSONArray("answer_score")

                        for (j in 0 until jsonArray_user.length()) {
                            val score = jsonArray_answer_score.getBoolean(j)
                            childitemsarraylist.add(
                                ChildItem(
                                    jsonArray_antwort.get(j) as String,
                                    jsonArray_user.get(j) as String,
                                    score
                                )
                            )
                        }
                        val parentitem : ParentItem = ParentItem(category, childitemsarraylist as List<ChildItem>)
                        categoriesandanswersarraylist.add(parentitem)
                    }
                }
            }
        }
    }
}
