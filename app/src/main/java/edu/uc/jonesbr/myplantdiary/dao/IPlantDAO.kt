package edu.uc.jonesbr.myplantdiary.dao

import edu.uc.jonesbr.myplantdiary.dto.Plant
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IPlantDAO {

    @GET("/perl/mobile/viewplantsjsonarray.pl")
    suspend fun getAllPlants(): ArrayList<Plant>

    @GET("/perl/mobile/viewplantsjsonarray.pl")
    fun getPlants(@Query("Combined_Name") plantName:String) : Call<ArrayList<Plant>>
}