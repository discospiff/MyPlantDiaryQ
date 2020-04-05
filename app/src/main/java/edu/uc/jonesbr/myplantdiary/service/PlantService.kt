package edu.uc.jonesbr.myplantdiary.service

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks.await
import edu.uc.jonesbr.myplantdiary.RetrofitClientInstance
import edu.uc.jonesbr.myplantdiary.dao.IPlantDAO
import edu.uc.jonesbr.myplantdiary.dto.Plant
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantService {

    internal suspend fun fetchPlants(plantName: String) {
        withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async {service?.getAllPlants()}

            updateLocalPlants(plants.await())

            delay(30000)
        }
    }

    /**
     * Store these plants locally, so that we can use the data without network latency
     */
    private suspend fun updateLocalPlants(plants: ArrayList<Plant>?) {
        var sizeOfPlants = plants?.size
        delay(30000)

    }
}