package edu.uc.jonesbr.myplantdiary.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationLiveData = LocationLiveData(application)
    internal fun getLocationLiveData() = locationLiveData

}