package edu.uc.jonesbr.myplantdiary.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.uc.jonesbr.myplantdiary.MainActivity
import edu.uc.jonesbr.myplantdiary.MapsActivity
import edu.uc.jonesbr.myplantdiary.R
import edu.uc.jonesbr.myplantdiary.dto.Event
import edu.uc.jonesbr.myplantdiary.dto.Photo
import edu.uc.jonesbr.myplantdiary.dto.Plant
import edu.uc.jonesbr.myplantdiary.dto.Specimen
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : DiaryFragment(), DateSelected, NewPlantCreated {

    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private val AUTH_REQUEST_CODE = 2002
    private lateinit var applicationViewModel: ApplicationViewModel
    private var _plantId = 0
    private var user : FirebaseUser? = null
    private var photos : ArrayList<Photo> = ArrayList<Photo>()
    private var specimen = Specimen()
    private var _events = ArrayList<Event>()
    var selectedPlant: Plant = Plant("", "", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applicationViewModel = ViewModelProviders.of(this).get(ApplicationViewModel::class.java)

        applicationViewModel.plantService.getLocalPlantDAO().getAllPlants().observe(this, Observer {
            plants -> actPlantName.setAdapter(ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, plants))
        })
        viewModel.specimens.observe(this, Observer {
            specimens -> spnSpecimens.setAdapter(ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, specimens))
        })
        /**
         * An existing item was clicked from the predefined autocomplete list.
         */
        actPlantName.setOnItemClickListener { parent, view, position, id ->
            selectedPlant = parent.getItemAtPosition(position) as Plant
            _plantId = selectedPlant.plantId
        }
        actPlantName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPlant = Plant("", "", "")
            }

            /**
             * An existing item was clicked from the predefined autocomplete list.
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPlant = parent!!.getItemAtPosition(position) as Plant
                _plantId = selectedPlant.plantId
            }
        }
        actPlantName.setOnFocusChangeListener { view, hasFocus ->
            var enteredPlant = actPlantName.text.toString()
            if (selectedPlant != null && enteredPlant.isNotEmpty() && !enteredPlant.equals(selectedPlant.toString())) {
                // we have a new plant.
                // we want to ask the user to create a new entry.
                val newPlantDialogFragment = NewPlantDialogFragment(enteredPlant, this)
                newPlantDialogFragment.show(fragmentManager!!, "New Plant")
            }

        }
        btnMap.setOnClickListener {
            (activity as MainActivity).onOpenMap()
        }
        btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        btnLogon.setOnClickListener {
            logon()
        }
        prepRequestLocationUpdates()
        btnSave.setOnClickListener {
            saveSpecimen()
        }
        btnForward.setOnClickListener {
            (activity as MainActivity).onLeftSwipe()
        }
        btnDatePlanted.setOnClickListener {
            showDatePicker()
        }
        spnSpecimens.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             *
             * Callback method to be invoked when an item in this view has been
             * selected. This callback is invoked only when the newly selected
             * position is different from the previously selected position or if
             * there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the
             * data associated with the selected item.
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                specimen = parent?.getItemAtPosition(position) as Specimen
                // use this specimen object to populate our UI fields
                actPlantName.setText(specimen.plantName)
                txtDescription.setText(specimen.description)
                btnDatePlanted.setText(specimen.datePlanted)
                viewModel.specimen = specimen
                // trigger an update of the events for this specimen.
                viewModel.fetchEvents()
            }

        }

        rcyEventsForSpecimens.hasFixedSize()
        rcyEventsForSpecimens.layoutManager = LinearLayoutManager(context)
        rcyEventsForSpecimens.itemAnimator = DefaultItemAnimator()
        rcyEventsForSpecimens.adapter = EventsAdapter(_events, R.layout.rowlayout)

        viewModel.events.observe(this, Observer{
                events ->
            // remove everthing that is in there.
            _events.removeAll(_events)
            // update with the new events that we have observed.
            _events.addAll(events)
            // tell the recycler view to update.
            rcyEventsForSpecimens.adapter!!.notifyDataSetChanged()
        })

    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment(this)
        datePickerFragment.show(fragmentManager!!, "datePicker")

    }

    private fun logon() {
        var providers = arrayListOf(
           AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), AUTH_REQUEST_CODE
        )
    }

    /**
     * Persist our specimen to long term storage.
     */
    internal fun saveSpecimen() {
        storeSpecimen()

        viewModel.save(specimen, photos, user!!)

        specimen = Specimen()
        photos = ArrayList<Photo>()
    }

    /**
     * Populate a specimen object based on the details entered into the user interface.
     */
    internal fun storeSpecimen() {
        specimen.apply {
            latitude = lblLatitudeValue.text.toString()
            longitude = lblLongitudeValue.text.toString()
            plantName = actPlantName.text.toString()
            description = txtDescription.text.toString()
            datePlanted = btnDatePlanted.text.toString()
            plantId = _plantId
        }
        viewModel.specimen = specimen
    }

    private fun prepRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestLocationUpdates() {

        applicationViewModel.getLocationLiveData().observe(this, Observer {
            lblLatitudeValue.text = it.latitude
            lblLongitudeValue.text = it.longitude
        })
    }

    private fun prepOpenImageGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==  PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                } else {
                    Toast.makeText(context, "Unable to update location without permission", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE)  {
                // now we can get the thumbnail
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
            } else if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(context, "Image Saved", Toast.LENGTH_LONG).show()
                var photo = Photo(localUri = photoURI.toString())
                photos.add(photo)
            } else if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data != null) {
                    val image = data.data
                    val source = ImageDecoder.createSource(activity!!.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                }
            } else if (requestCode == AUTH_REQUEST_CODE) {
                user = FirebaseAuth.getInstance().currentUser
            }
        }
    }

    class DatePickerFragment(val dateSelected : DateSelected) : DialogFragment(), DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month  = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(context!!, this, year, month, dayOfMonth)
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            dateSelected.receiveDate(year, month, dayOfMonth)
            Log.d(TAG, "Got the date")

        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    /**
     * This is the function that will be invoked in our fragment when a user picks a date.
     */
    override fun receiveDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        val viewFormatter = SimpleDateFormat("dd-MMM-YYYY")
        var viewFormattedDate = viewFormatter.format(calendar.getTime())
        btnDatePlanted.setText(viewFormattedDate)
    }

    class NewPlantDialogFragment(val enteredPlant:String, val newPlantCreated:NewPlantCreated) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                val inflater = requireActivity().layoutInflater
                var newPlantView = inflater.inflate(R.layout.newplantdialog, null)
                val txtCommon = newPlantView.findViewById<EditText>(R.id.edtCommon)
                val txtGenus = newPlantView.findViewById<EditText>(R.id.edtGenus)
                val txtSpecies = newPlantView.findViewById<EditText>(R.id.edtSpecies)
                txtCommon.setText(enteredPlant)
                builder.setView(newPlantView)
                        .setPositiveButton(getString(R.string.save), DialogInterface.OnClickListener{ dialog, which ->
                            val common = txtCommon.text.toString()
                            val genus = txtGenus.text.toString()
                            val species = txtSpecies.text.toString()
                            val newPlant = Plant(genus, species, common)
                            newPlantCreated.receivePlant(newPlant)
                            getDialog()?.cancel()
                        })
                        .setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
                            getDialog()?.cancel()
                        })
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }

    override fun receivePlant(plant: Plant) {
       // applicationViewModel.plantService.save(plant)
    }

}

interface DateSelected {
    fun receiveDate(year: Int, month: Int, dayOfMonth: Int)
}

interface NewPlantCreated {
    fun receivePlant(plant: Plant)
}
