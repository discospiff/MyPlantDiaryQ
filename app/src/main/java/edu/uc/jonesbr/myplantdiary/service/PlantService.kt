package edu.uc.jonesbr.myplantdiary.service

import androidx.lifecycle.MutableLiveData
import edu.uc.jonesbr.myplantdiary.RetrofitClientInstance
import edu.uc.jonesbr.myplantdiary.dao.IPlantDAO
import edu.uc.jonesbr.myplantdiary.dto.Plant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantService {

    internal fun fetchPlants(plantName: String) : MutableLiveData<ArrayList<Plant>> {
        var _plants = MutableLiveData<ArrayList<Plant>>()
        val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
        val call = service?.getAllPlants()
        call?.enqueue(object : Callback<ArrayList<Plant>> {
            /**
             * Invoked when a network exception occurred talking to the server or when an unexpected
             * exception occurred creating the request or processing the response.
             */
            override fun onFailure(call: Call<ArrayList<Plant>>, t: Throwable) {
                val j = 1 + 1
                val i = 1 + 1
            }

            /**
             * Invoked for a received HTTP response.
             *
             *
             * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
             * Call [Response.isSuccessful] to determine if the response indicates success.
             */
            override fun onResponse(
                call: Call<ArrayList<Plant>>,
                response: Response<ArrayList<Plant>>
            ) {
                _plants.value = response.body()
            }

        })

        return _plants
    }
}