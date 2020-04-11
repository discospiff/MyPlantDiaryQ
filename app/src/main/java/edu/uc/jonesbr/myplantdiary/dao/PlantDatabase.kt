package edu.uc.jonesbr.myplantdiary.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.uc.jonesbr.myplantdiary.dto.Plant

@Database(entities=arrayOf(Plant::class), version = 1)
abstract  class PlantDatabase : RoomDatabase() {
    abstract fun localPlantDAO() : ILocalPlantDAO
}