package edu.uc.jonesbr.myplantdiary.dto

import com.google.firebase.firestore.Exclude


data class Specimen(var plantName:String = "", var latitude:String = "", var longitude: String = "", var description: String = "", var datePlanted:String = "", var specimenId : String = "", var plantId: Int = 0) {

    private var _events: ArrayList<Event> = ArrayList<Event>()

    var events : ArrayList<Event>
        @Exclude get() {return _events}
        set(value) {_events = value}

    override fun toString(): String {
        return "$plantName $description $latitude $longitude"
    }
}