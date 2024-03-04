package com.example.stadt_land_fluss

//import com.github.nkzawa.socketio.client.IO
//import com.github.nkzawa.socketio.client.Socket
import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {
    private lateinit var socket: Socket

    @Synchronized
    fun setSocket(){
        try{
            socket = IO.socket("http://10.0.2.2:3000")
            socket.connect()
        } catch(e : java.lang.Exception)
        {
            kotlin.io.println("connection not possible")
        }
    }

    @Synchronized
    fun disconnect()
    {
        socket.disconnect()
        socket.off()
    }

    @Synchronized
    fun getSocket() : Socket
    {
        return socket
    }

}