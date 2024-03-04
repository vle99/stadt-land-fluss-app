// Importing required packages
const express = require('express');
const mongoose = require('mongoose');
const http = require('http');
const socketio = require('socket.io');
const { Room, User } = require('./schema');
const app = express();

const server = http.createServer(app);

const io = socketio(server);

mongoose
  .connect('mongodb://127.0.0.1:27017/slf', { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => {
    console.log('Connected to MongoDB');
  })
  .catch((error) => {
    console.log('Error connecting to MongoDB:', error.message);
  });


io.on("connection", (socket) => {
  console.log("A user connected");

  // Event for joining room
  socket.on("join-or-create-room", async (data) => {
      const { username, room } = data;
      try {
        const existingUser = await User.findOne({ username });
        if (existingUser) {
          socket.emit("username-taken");
          return;
        } else
        {
          socket.emit("new-username");
        }

        // creating user
        const user = new User({
          username: username,
          room: room
        });
        await user.save();

        // creating room
        let existingRoom = await Room.findOne({ room_name: room });
        if (!existingRoom) {
          user.is_admin = true
          await user.save();
          existingRoom = new Room({
            room_name: room,
            users: [user]
          });
          existingRoom.max_user--;
          await existingRoom.save();
          socket.emit("join-admin-panel", user);
        }

        else {
          if (existingRoom.users.length >= existingRoom.max_user) {
            await User.deleteOne({ username: username })
            socket.emit("room-full");
            return;
          }
          existingRoom.max_user--;
          existingRoom.users.push(user);
          await existingRoom.save();
          socket.emit("join-waiting-room", user);
        }

        socket.join(existingRoom.room_name);
        socket.to(existingRoom.room_name).emit("user-joined", user);

      } catch (error) {
        console.error(error);
        socket.emit("join-error", "Error joining room");
      }
    });


    socket.on("update-max-users", async (data) => {
      const { room_name, max_user } = data;
      try {
        const existingRoom = await Room.findOne({ room_name });
        existingRoom.max_user = max_user;
        await existingRoom.save();
        } catch (error) {
          console.error(error);
          socket.emit("max-users-error", "Error updating max users");
        }
    });


    //TODO: muss noch im Android Studio implementiert werden, konfigurierbar durch Admin beim erstellen des Raums
    //Boolean, ob customPoints aktiviert sind oder nicht (siehe check-rate-answers)
    socket.on("update-custom-points", async (data) => {
      const { room_name, customPoints } = data;
      try {
        const currentRoom = await Room.findOne({ room_name });
        currentRoom.custom_points = customPoints;
        await currentRoom.save();
        } catch (error) {
          console.error(error);
          socket.emit("custom-points-error", "Error updating update-custom-points");
        }
    });


    //TODO: muss noch im Android Studio implementiert werden, konfigurierbar durch Admin beim erstellen des Raums
    socket.on("update-round-timer", async (data) => {
      const { room_name, timer } = data;
      try {
        const currentRoom = await Room.findOne({ room_name });
        currentRoom.round_timer = timer;
        await currentRoom.save();
        } catch (error) {
          console.error(error);
          socket.emit("timer-error", "Error updating timer");
        }
    });


    //TODO: muss noch im Android Studio implementiert werden, Aufruf durch Admin beim erstellen des Raums
    socket.on("rnd-letter", async (room_name) =>{
      const alphabet = [A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]
      let rndNumber = Math.floor(Math.random() * 25);
      let letter = alphabet[rndNumber];
      let currentRoom = await Room.findOne({room_name: room_name});
      currentRoom.letter = letter;
      await currentRoom.save();
    });


    //TODO: Abruf und Darstellung von letter muss noch im Android Studio implementiert werden
    socket.on("fetch-all-categories", async (room) =>
    {
      const roomname = room.room_name;
      let existingRoom = await Room.findOne({room_name: roomname });
        if (!existingRoom) {
          socket.emit("no-room-choosen")
        }

        else {
          let categories = existingRoom.fieldnames;
          let letter = existingRoom.letter;
          socket.emit("emit-all-categories", categories, letter);
        }

    });


    socket.on("fetch-existing-roms", async() =>
    {
        let rooms = await Room.distinct("room_name");
        socket.emit("all-existing-rooms", rooms);
    });


    socket.on("set-answer-in-db", async(data) =>
    {
      const { userid, answer } = data;
      let currentUser = await User.findOne({ username: userid });
      currentUser.answer.push(answer);
      await currentUser.save();
      socket.emit("answer-saved");

    });


    socket.on("add-category", async (category) =>
    {
        const newfield = category.category;
        const currentroom = category.current_room;

        const existingfield = await Room.findOne({category});
        if(existingfield)
        {
            socket.emit("field-is-existing");
        } else
        {
          let existingRoom = await Room.findOne({room_name: currentroom});
          existingRoom.fieldnames.push(newfield);
          await existingRoom.save();
          socket.emit("field-is-ok");
        }
    });


    //TODO: In Android Studio implementieren, muss am Ende einer Runde vom Admin aufgerufen werden
    //TODO: Die Checkboxen in der Bewertungsansicht sollen je nachdem ob der answer_score positiv oder negativ ist standardmäßig aktiviert oder deaktiviert sein
    //Bewertet Antworten. Im ersten Schritt wird überprüft ob die Antwort mit dem richtigen Buchstaben beginnt.
    //Danach wird eine Wörterbuch API nach der Existenz und der Verwendungshäufigkeit des Wortes abgefragt.
    //Je nachdem wie selten/kreativ das Wort ist werden 1-7 Punkte vergeben. Die Bewertung kann auch beim erstellen des Raumes deaktiviert werden, dann gibt jede richtige Antwort 1 Punkt.
    //Wird die Antwort als falsch erkannt, werden die vergebenen Punkte negiert, in der Bewertungsansicht ist die Checkbox hinter der Antwort standartmäßig auf falsch, kann aber von den Spielern überschrieben werden.
    //Infos zur Wörterbuch API: https://www.dwds.de/d/api
    //Bugfix
socket.on("check-rate-answers", async(roomname) =>{
  let currentRoom = await Room.findOne({room_name: roomname});
  let users = currentRoom.users;
  let letterShould = currentRoom.letter;
  let customPoints = currentRoom.custom_points;
  let score = 0;
  for(user in users){
    let currentUser = await User.findOne({ _id: users[user] });
    let answers = currentUser.answer;
    for(answer in answers){
      await fetch('https://www.dwds.de/api/frequency/?q=' + answers[answer])
      .then(response => {return response.json();
      })
      .then(wordData => {
        if(customPoints === true){
          score = 7 - wordData.frequency;
        }
        else{
          score = 1;
        }
        let letterIs = wordData.q.slice(0,1).toUpperCase();
        if(letterShould !==  letterIs){
          score = score * -1;
        }else if(wordData.hits < 500){
          score = score * -1;
        }
        })
        currentUser.answer_score.push(score);
        await currentUser.save();
    }
  }
}); 



    //TODO: Bewertungsansicht muss erstellt werden, Bewertungsansicht muss Array points übergeben
    //wird von jedem User bei Abgabe der Bewertung 1mal pro bewertetem Spieler aufgerufen
    socket.on("commit-score", async (user,points) =>
    {
      let currentUser = await User.findOne({ username: user });
      let counter = 0;
      for(point in points){
        currentUser.score_per_field[counter] = currentUser.score_per_field[counter] + points;
        counter = counter + 1;
      }
      await currentUser.save();
    });


    //TODO: muss noch im Android Studio implementiert werden, Aufruf durch Admin beim erstellen des Raums
    //wird am Ende einer Runde (nachdem jeder User seine Bewertung abgegeben hat) vom Admin aufgerufen, bekommt den room_name übergeben
    //wenn mindestens die Hälfte der Spieler die Antwort als gültig markiert haben wird die Antwort gezählt
    socket.on("calculate-score", async (room_name) =>
    {
      let currentRoom = await Room.findOne({room_name: room_name});
      let users = currentRoom.users;
      for(user in users){
        let currentUser = await User.findOne({ username: user });
        let score_per_field = currentUser.score_per_field;
        let answerScore = currentUser.answer_score;
        for(score in score_per_field){
          if(score >= (users.length / 2)){
            currentUser.total_score = currentUser.total_score + Math.abs(answerScore);
          }
        }
        score_per_field = [];
        answerScore = [];
        await currentUser.save();
      }
    });


    /*
    socket.on("get-answers", async(roomname) =>{
      const roomn = roomname.room_name;
      let currentRoom = await Room.findOne({room_name: roomn});
      let fields = currentRoom.fieldnames;

      let obj_fields = [];
      for(field in fields)
      {
        let fieldname = fields[field]
        let userjsonarray = [];
        let antwortjsonarray = [];
        let answer_score = [];

        for(user in currentRoom.users)
        {
          let currentUser = await User.findOne({ _id: currentRoom.users[user] });
          let username = currentUser.username;
          userjsonarray.push(username);
          let answers = currentUser.answer;
          antwortjsonarray.push(answers);
          let correct = [];
          for(score in currentUser.answer_score){
            if(currentUser.answer_score[score] < 0){
              correct[score] = false;
            }
            else{
              correct[score] = true;
            }
          }
          answer_score.push(correct[field]);
        }

        let fielddetails = {"fieldname": fieldname, "user": userjsonarray, "antwort": antwortjsonarray, "answer_score": answer_score};

        obj_fields.push(fielddetails);
      }
      let jsonResult = {obj_fields};
      let result = JSON.stringify(jsonResult);
      console.log(jsonResult);
      socket.emit("all-info", result)

    });
    */

    socket.on("get-answers", async(roomname) =>{
          const roomn = roomname;
          let currentRoom = await Room.findOne({room_name: roomn});
          let fields = currentRoom.fieldnames;
          let obj_fields = [];
          for(field in fields)
          {
            let fieldname = fields[field];
            let userjsonarray = [];
            let antwortjsonarray = [];
            let answer_score = [];
            for(user in currentRoom.users)
            {
              let currentUser = await User.findOne({ _id: currentRoom.users[user] });
              let username = currentUser.username;
              userjsonarray.push(username);
              let answers = currentUser.answer;
              antwortjsonarray.push(answers[field]);
              let correct = [];
              for(score in currentUser.answer_score){
                if(currentUser.answer_score[score] < 0){
                  correct[score] = false;
                }
                else{
                  correct[score] = true;
                }
              }
              answer_score.push(correct[field]);
            }
            //console.log(userjsonarray,antwortjsonarray,answer_score);
            let fielddetails = {"user": userjsonarray, "antwort": antwortjsonarray, "answer_score": answer_score};
            let onefield = {[fieldname] : fielddetails};
            //console.log(onefield);
            obj_fields.push(onefield);
          }
          socket.emit("all-info", obj_fields);
        });

    /*

    socket.on("get-answers", async(roomname) =>{
      let currentRoom = await Room.findOne({room_name: roomname});
      const Obj = currentRoom.fieldnames;
      for(field in currentRoom.fieldnames){
        for(user in currentRoom.users){
          let currentUser = await User.findOne({ _id: currentRoom.users[user] });
          let username = currentUser.username;
          let answers = currentUser.answer;
          let correct = [];
          for(score in currentUser.answer_score){
            if(currentUser.answer_score[score] < 0){
              correct[score] = false;
            }
            else{
              correct[score] = true;
            }


          }
          //let array = [currentRoom.fieldnames[field], username, answers[field], correct[field]]


          console.log(array);
        }


      }
    })
    */

    //TODO: muss am Ende eines Spiels aufgerufen und dargestelt werden
    socket.on("get-total-score", async (room_name) =>{
      let currentRoom = await Room.findOne({room_name: room_name});
      let users = currentRoom.users;
      const score = [];
      let counter = 0;
      for(user in users){
        let currentUser = await User.findOne({ username: user });
        score[counter] = user + ": " + currentUser.total_score.toString();
        counter = counter + 1;
      }
      socket.emit("Total Scores:", score)
    });


    socket.on("disconnect", () => {
    console.log("A user disconnected");

    //TODO: Muss im Android Studio aufgerufen werden
    socket.on("delete-room-user", async (room_name) =>
    {
      let existingRoom = await Room.findOne({room_name: room_name });
        if (!existingRoom) {
          socket.emit("room does not exist")
        }
        else {
          let users = existingRoom.users;
          for(user in users){
            await User.deleteOne({ username: user })
          }
          await Room.deleteOne({room_name: room_name})
          socket.emit("room and users deleted")
        }
    });
    });
  });


server.listen(3000, () => {
  console.log("Server started on port 3000");
});