package edu.uc.jonesbr.myplantdiary.service

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.google.android.gms.tasks.Tasks.await
import edu.uc.jonesbr.myplantdiary.RetrofitClientInstance
import edu.uc.jonesbr.myplantdiary.dao.ILocalPlantDAO
import edu.uc.jonesbr.myplantdiary.dao.IPlantDAO
import edu.uc.jonesbr.myplantdiary.dao.PlantDatabase
import edu.uc.jonesbr.myplantdiary.dto.Plant
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantService(application: Application) {
    private val application = application

    internal suspend fun fetchPlants(plantName: String) {
        withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async {service?.getAllPlants()}

            updateLocalPlants(plants.await())

        }
    }

    /**
     * Store these plants locally, so that we can use the data without network latency
     */
    private suspend fun updateLocalPlants(plants: ArrayList<Plant>?) {
        var sizeOfPlants = plants?.size
        try {
            var localPlantDAO = getLocalPlantDAO()
            localPlantDAO.insertAll(plants!!)
        }catch (e: Exception) {
            Log.e(TAG, e.message)
        }

    }

    internal fun getLocalPlantDAO() : ILocalPlantDAO {
        val db = Room.databaseBuilder(application, PlantDatabase::class.java, "mydiary").build()
        val localPlantDAO = db.localPlantDAO()
        return localPlantDAO
    }

    internal fun save(plant:Plant) {
        getLocalPlantDAO().save(plant)
    }
}