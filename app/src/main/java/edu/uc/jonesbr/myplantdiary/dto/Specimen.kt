package edu.uc.jonesbr.myplantdiary.dto

data class Specimen(var plantName:String = "", var latitude:String = "", var longitude: String = "", var description: String = "", var datePlanted:String = "", var specimenId : Int = 0, var plantId: Int = 0 ) {
}