package edu.uc.jonesbr.myplantdiary.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import edu.uc.jonesbr.myplantdiary.dto.Plant

@Dao
interface ILocalPlantDAO {

    @Query("SELECT * FROM plant")
    fun getAllPlants()  : LiveData<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(plants: ArrayList<Plant>)

    @Delete
    fun delete(plant: Plant)

    @Insert
    fun save(plant: Plant)
}