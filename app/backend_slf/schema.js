const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
  },

  room: {
    type: String,
    required: false
  },
  answer: {               //Antwort für jede Kategorie, wird jede Runde resettet
    type: [String],
    required: false
  },
  answer_score: {         //Bewertung der Antworten mit Daten einer Wörterbuch API
    type: [Number]        //Je nach seltenheit des Begriffs 1-7 Punkte, als ungültig bewertete Antworten werden mit negativem Vorzeichen gekennzeichnet
  },
  score_per_field: {      //Anzahl der Nutzer die den Begriff als gültig markiert haben
    type: [Number],
    default: [],
    required: false
  },
  total_score: {          //die Gesamtpunktzahl eines Spiels, wird nach jeder abgeschlossenen Runde aktualisiert
    type: Number,
    default: 0,
    required: false
  },
  is_admin: {
    type: Boolean,
    default: false
   }
});

const roomSchema = new mongoose.Schema({
  room_name: {
    type: String,         //Konfigurierbar durch Admin beim erstellen der Runde
    required: true,
    unique: true
  },
  fieldnames: {           //Namen der Kategorien/Antwortfelder
    type: [String],       //Konfigurierbar durch Admin beim erstellen der Runde
    default: ["stadt", "land", "fluss"]
  },
  letter: {               //der Buchstabe mit dem gespielt wird
    type: String          //wird zu Beginn der Runde zufällig gewählt
  },
  round_number: {         //Anzahl der Runden die gespielt werden soll
    type: Number,         //Konfigurierbar durch Admin beim erstellen der Runde
    default: 10
  },
  users: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }],
  max_user: {             //Maximale Anzahl der Benutzer
    type: Number,         //Konfigurierbar durch Admin beim erstellen der Runde
    default: 1
  },
  round_timer: {          //Zeit die die Spieler pro Runde haben
    type: Number,         //Konfigurierbar durch Admin beim erstellen der Runde
    default: 30
  },
  custom_points: {        //Bewertungsschema: jede Antwort gleich viele Punkte oder Bonuspunkte für seltene Antworten
    type: Boolean,        //Konfigurierbar durch Admin beim erstellen der Runde
    default: true
  }
});

const User = mongoose.model('User', userSchema);
const Room = mongoose.model('Room', roomSchema);

module.exports = {User, Room};