package edu.uc.jonesbr.myplantdiary.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uc.jonesbr.myplantdiary.dto.Plant
import edu.uc.jonesbr.myplantdiary.service.PlantService

class MainViewModel : ViewModel() {
    var plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    var plantService: PlantService = PlantService()

    init {
       fetchPlants("e")
    }

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlants(plantName)
    }
    // TODO: Implement the ViewModel
}
