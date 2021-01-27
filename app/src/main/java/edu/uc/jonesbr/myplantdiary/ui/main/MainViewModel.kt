package edu.uc.jonesbr.myplantdiary.ui.main

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import edu.uc.jonesbr.myplantdiary.dto.Event
import edu.uc.jonesbr.myplantdiary.dto.Photo
import edu.uc.jonesbr.myplantdiary.dto.Plant
import edu.uc.jonesbr.myplantdiary.dto.Specimen
import edu.uc.jonesbr.myplantdiary.service.PlantService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var _plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    private lateinit var firestore : FirebaseFirestore
    private var _specimens: MutableLiveData<ArrayList<Specimen>> = MutableLiveData<ArrayList<Specimen>>()
    private var storageReferenence = FirebaseStorage.getInstance().getReference()
    private var _specimen = Specimen()
    private var _events = MutableLiveData<List<Event>>()


    init {
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToSpecimens()
    }

    /**
     * This will hear any updates from Firestore
     */
    private fun listenToSpecimens() {
        firestore.collection("specimens").addSnapshotListener {
            snapshot, e ->
            // if there is an exception we want to skip.
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            // if we are here, we did not encounter an exception
            if (snapshot != null) {
                // now, we have a populated shapshot
                val allSpecimens = ArrayList<Specimen>()
                val documents = snapshot.documents
                documents.forEach {

                    val specimen = it.toObject(Specimen::class.java)
                    if (specimen != null) {
                        specimen.specimenId = it.id
                        allSpecimens.add(specimen!!)
                    }
                }
                _specimens.value = allSpecimens
            }
        }
    }

    fun save(
        specimen: Specimen,
        photos: java.util.ArrayList<Photo>,
        user: FirebaseUser
    ) {
        val document =
            if (specimen.specimenId != null && !specimen.specimenId.isEmpty()) {
                // updating existing
                firestore.collection("specimens").document(specimen.specimenId)
            } else {
                // create new
                firestore.collection("specimens").document()
            }
        specimen.specimenId = document.id
        val set = document.set(specimen)
            set.addOnSuccessListener {
                Log.d("Firebase", "document saved")
                if (photos != null && photos.size > 0) {
                    savePhotos(specimen, photos, user)
                }

            }
            set.addOnFailureListener {
                Log.d("Firebase", "Save Failed")
            }
    }

    internal fun save(event: Event) {
        val collection = firestore.collection("specimens").document(specimen.specimenId).collection("events")
        val task = collection.add(event)
        task.addOnSuccessListener {
            event.id = it.id
        }
        task.addOnFailureListener {
            var message = it.message
            var i = 1 + 1
        }

    }

    internal fun delete(event: Event) {
        if (event.id != null) {
             val document = firestore.collection("specimens").document(specimen.specimenId).collection("events").document(event.id)
            val task = document.delete();
            task.addOnSuccessListener {
                Log.e(TAG, "Event ${event.id} Deleted")
            }
            task.addOnFailureListener {
                Log.e(TAG, "Event ${event.id} Failed to delete.  Message: ${it.message}")
            }
        }
    }

    internal fun fetchEvents() {
        var eventsCollection = firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("events")
        eventsCollection.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            var innerEvents = querySnapshot?.toObjects(Event::class.java)
            _events.postValue(innerEvents)
        }
    }

    private fun savePhotos(
        specimen: Specimen,
        photos: java.util.ArrayList<Photo>,
        user: FirebaseUser
    ) {
        val collection = firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("photos")
            photos.forEach {
                photo -> val task = collection.add(photo)
                task.addOnSuccessListener {
                    photo.id = it.id
                    uploadPhotos(specimen, photos, user)
                }
            }
    }

    private fun uploadPhotos(specimen: Specimen, photos: ArrayList<Photo>, user: FirebaseUser) {
        photos.forEach {
            photo ->
            var uri = Uri.parse(photo.localUri)
            val imageRef = storageReferenence.child("images/" + user.uid + "/" + uri.lastPathSegment)
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                    photo.remoteUri = it.toString()
                    // update our Cloud Firestore with the public image URI.
                    updatePhotoDatabase(specimen, photo)
                }

            }
            uploadTask.addOnFailureListener {
                Log.e(TAG, it.message)
            }
        }
    }

    private fun updatePhotoDatabase(specimen: Specimen, photo: Photo) {
        firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("photos")
            .document(photo.id)
            .set(photo)
    }

    internal var plants:MutableLiveData<ArrayList<Plant>>
        get() {return _plants}
        set(value) {_plants = value}

    internal var specimens:MutableLiveData<ArrayList<Specimen>>
        get() { return _specimens}
        set(value) {_specimens = value}

    internal var specimen: Specimen
        get() {return _specimen}
        set(value) {_specimen = value}

    internal var events : MutableLiveData<List<Event>>
        get() { return _events}
        set(value) {_events = value}
}
