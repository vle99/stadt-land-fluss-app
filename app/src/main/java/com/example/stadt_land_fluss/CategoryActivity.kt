package com.example.stadt_land_fluss

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.Adapters.EditAdapter
import com.example.stadt_land_fluss.Adapters.EditModel
import com.example.stadt_land_fluss.Adapters.EntryFieldAdapter
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.typeOf


class CategoryActivity : AppCompatActivity() {
    lateinit var btnAddCategory : Button
    lateinit var btnSave : Button
    lateinit var etMaxUsers : EditText
    lateinit var etAddCategory : EditText
    lateinit var rv4game : RecyclerView
    lateinit var tvWarningCategory : TextView

    lateinit var adapter : EntryFieldAdapter
    lateinit var arrayList: ArrayList<String>

    val mSocket = SocketManager.getSocket()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        btnAddCategory = findViewById(R.id.btnAddCategory)
        btnSave = findViewById(R.id.btnSave)
        etAddCategory = findViewById(R.id.etAddCategory)
        etMaxUsers = findViewById(R.id.etMaxUsers)
        rv4game = findViewById(R.id.rvforgame)
        tvWarningCategory = findViewById(R.id.tvWarningCategory)


        val roomObject = JSONObject().put("room_name", UserSession.roomid)
        mSocket.emit("fetch-all-categories", roomObject)

        val arraylist : ArrayList<String> = ArrayList()


        btnAddCategory.setOnClickListener {

            val customcategory : String = etAddCategory.text.toString()
            val categoryObj = JSONObject().put("category", customcategory).put("current_room", UserSession.roomid)
            mSocket.emit("add-category", categoryObj) //Füge Kategorie zum Backend hinzu
        }


        //val linearlayoutmanager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //rv4game.layoutManager = linearlayoutmanager


        mSocket.on("field-is-ok")
        {

            mSocket.emit("fetch-all-categories", roomObject)
            //https://www.geeksforgeeks.org/android-pull-to-refresh-with-recyclerview-in-kotlin/
        }

        mSocket.on("emit-all-categories") //Frage das Backend ab und refreshe die Recyclerview
        {
            args -> if(args[0] != null)
        {
            val data  = args[0] as JSONArray
            arraylist.clear() //Reset der Arraylist, ist ein bisschen umständlich programmiert

            if(data != null)
            {
                for(i in 0 until data.length())
                {
                    val obj = data[i]
                    val tmp = obj.toString()
                    arraylist.add(tmp)
                }
            }
            runOnUiThread{
                adapter = EntryFieldAdapter(arraylist)
                rv4game.adapter = adapter
                val linearlayoutmanager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                rv4game.layoutManager = linearlayoutmanager
            }
        }
        }


        btnSave.setOnClickListener {

            val room = intent.getStringExtra("room")
            val maxUsers = etMaxUsers.text.toString()
            val configObject = JSONObject().put("room_name", room).put("max_user", maxUsers)
            mSocket.emit("update-max-users", configObject)
            val intent = Intent(this, WaitingRoomActivity::class.java)
            startActivity(intent)
        }
    }
}