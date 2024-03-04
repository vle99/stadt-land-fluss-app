package com.example.stadt_land_fluss

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stadt_land_fluss.Adapters.*
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var categoriesForGame : RecyclerView
    private lateinit var submitButton: Button
    private lateinit var resetButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var countDownTimerTextView: TextView
    private var timer: CountDownTimer? = null
    private lateinit var arraylist : ArrayList<EditModel>

    val mSocket = SocketManager.getSocket()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        categoriesForGame = findViewById(R.id.categoriesForGame)

        submitButton = findViewById(R.id.submitButton)
        resetButton = findViewById(R.id.resetButton)
        resultTextView = findViewById(R.id.resultTextView)
        countDownTimerTextView = findViewById(R.id.countDownTimerTextView)

        arraylist = ArrayList<EditModel>()

        val roomObject = JSONObject().put("room_name", UserSession.roomid)
        mSocket.emit("fetch-all-categories", roomObject)

        submitButton.setOnClickListener {
            checkAnswers()
            val intent = Intent(this, TempActivity::class.java)
            startActivity(intent)
        }

        resetButton.setOnClickListener {
            deleteAllFields()
        }


        mSocket.on("emit-all-categories") {
                args -> if(args[0] != null)
            {

                val data  = args[0] as JSONArray

                if(data != null)
                {
                    for(i in 0 until data.length())
                    {
                        val obj = data[i]
                        val tmp = obj.toString()
                        val editModel = EditModel()
                        editModel.setEditTextValue(tmp)
                        editModel.category = tmp
                        arraylist.add(editModel)
                    }
                }
                }
                runOnUiThread {
                    val adapter = EditAdapter(this, arraylist)
                    val linearlayoutmanager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    categoriesForGame.layoutManager = linearlayoutmanager
                    categoriesForGame.adapter = adapter
                }
            }
        }




    private fun deleteAllFields(){
        for(i in 0 until arraylist.size)
        {
            arraylist[i].setEditTextValue("")

        }
    }

    private fun checkAnswers() {

        val answers : JSONArray = JSONArray()
        for(i in 0 until arraylist.size)
        {
            val answer : JSONObject = JSONObject()
            answer.put("userid", UserSession.userid).put("answer", arraylist[i].getEditTextValue())
            mSocket.emit("set-answer-in-db", answer)
            //Antworten sollen im User gespeichert werden, der im Raum ist
        }
    }

}