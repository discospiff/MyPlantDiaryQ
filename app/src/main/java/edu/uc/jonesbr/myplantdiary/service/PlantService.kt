package edu.uc.jonesbr.myplantdiary.service

import androidx.lifecycle.MutableLiveData
import edu.uc.jonesbr.myplantdiary.dto.Plant

class PlantService {

    fun fetchPlants(plantName: String) : MutableLiveData<ArrayList<Plant>> {
        return MutableLiveData<ArrayList<Plant>>()
    }
}