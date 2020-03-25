package edu.uc.jonesbr.myplantdiary.dto

data class Event(var type: String = "", var date: String = "", var quantity : Double? = 0.0, var units : String = "", var description : String = "") {
}