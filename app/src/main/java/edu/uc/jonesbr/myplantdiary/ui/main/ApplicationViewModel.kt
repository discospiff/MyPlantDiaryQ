package edu.uc.jonesbr.myplantdiary.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.uc.jonesbr.myplantdiary.service.PlantService
import kotlinx.coroutines.launch

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private var _plantService: PlantService = PlantService(application)
    private val locationLiveData = LocationLiveData(application)
    internal fun getLocationLiveData() = locationLiveData

    init {
        fetchPlants("e")
    }

    fun fetchPlants(plantName: String) {
        viewModelScope.launch{
            _plantService.fetchPlants(plantName)
        }
    }

    internal var plantService : PlantService
        get() {return _plantService}
        set(value) {_plantService = value}
}