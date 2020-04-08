package edu.uc.jonesbr.myplantdiary.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName="plant")
data class Plant(var genus: String, var species : String, var common :String, @PrimaryKey @SerializedName("id") var plantId:Int = 0) {
    override fun toString(): String {
        return common
    }
}