package com.example.stadt_land_fluss

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import org.w3c.dom.Text

class WaitingRoomActivity : AppCompatActivity() {

    private lateinit var tvWait : TextView
    private lateinit var btnStartGame : Button
    private lateinit var progressbar : ProgressBar

    private lateinit var tvwaitinguser : TextView
    private lateinit var tvwaitingroom : TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        tvWait = findViewById(R.id.tvwaitingprogress)
        btnStartGame = findViewById(R.id.btnStartGame)
        progressbar = findViewById(R.id.progressBar)
        tvwaitingroom = findViewById(R.id.tvwaitingroom)
        tvwaitinguser = findViewById(R.id.tvwaitinguser)
        progressbar.progress = 50

        tvwaitinguser.text = "User: " + "${UserSession.userid}"
        tvwaitingroom.text = "Room: " + "${UserSession.roomid}"

        btnStartGame.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}