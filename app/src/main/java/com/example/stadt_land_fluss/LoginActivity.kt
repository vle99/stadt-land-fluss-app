package com.example.stadt_land_fluss

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.Adapters.EntryFieldAdapter
import org.json.JSONArray
import org.json.JSONObject
import io.socket.client.IO
import io.socket.client.Socket

class LoginActivity : AppCompatActivity() {

    private  lateinit var mSocket : Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        SocketManager.setSocket()
        mSocket = SocketManager.getSocket()

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val roomEditText = findViewById<EditText>(R.id.roomEditText)
        val joinButton = findViewById<Button>(R.id.joinButton)
        //val saveButton = findViewById<Button>
        val rvexistingrooms = findViewById<RecyclerView>(R.id.rvexistingrooms)
        val tvWarning = findViewById<TextView>(R.id.tvWarning)

        val arraylist : ArrayList<String> = ArrayList<String>()

        //fetch existing rooms
        mSocket.emit("fetch-existing-roms")

        joinButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val room = roomEditText.text.toString()
            if(username == "" || room == "")
            {
                tvWarning.text = "username and room aren't defined"
            } else
            {
                val userObj = JSONObject().put("username", username).put("room", room)
                mSocket.emit("join-or-create-room", userObj)
                tvWarning.text = ""
            }

        }

        mSocket.on("all-existing-rooms")
        {
                args -> if(args[0] != null)
        {
            val data  = args[0] as JSONArray

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
                val adapter = EntryFieldAdapter(arraylist)
                rvexistingrooms.adapter = adapter
                val linearlayoutmanager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                rvexistingrooms.layoutManager = linearlayoutmanager
            }
        }
        }

        mSocket.on("username-taken"){
            tvWarning.text = "Benutzername bereits vergeben"
            usernameEditText.text.clear()
        }

        mSocket.on("room-full"){
            tvWarning.text = "Raum ist voll"
            roomEditText.text.clear()
        }

        mSocket.on("new-username")
        {
            tvWarning.text = ""
        }

        mSocket.on("join-admin-panel"){
            args -> if(args[0] != null)
        {
            val user = args[0] as JSONObject
            UserSession.userid = user.getString("username")
            UserSession.roomid = user.getString("room")
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("room", user.getString("room"))
            startActivity(intent)
        }
        }

        mSocket.on("join-waiting-room"){
            args -> if(args[0] != 0)
        {
            val user = args[0] as JSONObject
            UserSession.userid = user.getString("username")
            UserSession.roomid = user.getString("room")
            
            val intent = Intent(this, WaitingRoomActivity::class.java)
            startActivity(intent)
        }
        }
    }
}