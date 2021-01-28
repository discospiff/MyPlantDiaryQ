package edu.uc.jonesbr.myplantdiary.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import edu.uc.jonesbr.myplantdiary.R
import edu.uc.jonesbr.myplantdiary.dto.Event
import kotlinx.android.synthetic.main.rowlayout.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class DiaryFragment : Fragment() {
    protected val SAVE_IMAGE_REQUEST_CODE: Int = 1999
    protected val CAMERA_REQUEST_CODE: Int = 1998
    protected val CAMERA_PERMISSION_REQUEST_CODE = 1997
    private lateinit var currentPhotoPath: String
    protected var photoURI : Uri? = null
    internal lateinit var viewModel: MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.let {
            viewModel = ViewModelProviders.of(it!!).get(MainViewModel::class.java)
        }
    }

    /**
     * See if we have permission or not.
     */
    protected fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.CAMERA);
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)

        }
    }

    protected fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{
                takePictureIntent -> takePictureIntent.resolveActivity(context!!.packageManager)
            if (takePictureIntent == null) {
                Toast.makeText(context, "Unable to save photo", Toast.LENGTH_LONG).show()
            } else {
                // if we are here, we have a valid intent.
                val photoFile: File = createImageFile()
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(activity!!.applicationContext, "com.myplantdiary.android.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, SAVE_IMAGE_REQUEST_CODE)
                }
            }
        }
    }

    private fun createImageFile() : File {
        // genererate a unique filename with date.
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // get access to the directory where we can write pictures.
        val storageDir: File? = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PlantDiary${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, let's do stuff.
                    takePhoto()
                } else {
                    Toast.makeText(context, "Unable to take photo without permission", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }

        }
    }

    inner class EventsAdapter(val events: List<Event>, val itemLayout: Int) : RecyclerView.Adapter<DiaryFragment.EventViewHolder>() {
        /**
         * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
         * an item.
         *
         *
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         *
         *
         * The new ViewHolder will be used to display items of the adapter using
         * [.onBindViewHolder]. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary [View.findViewById] calls.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         *
         * @return A new ViewHolder that holds a View of the given view type.
         * @see .getItemViewType
         * @see .onBindViewHolder
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
            return EventViewHolder(view)
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int {
            return events.size
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the [ViewHolder.itemView] to reflect the item at the given
         * position.
         *
         *
         * Note that unlike [android.widget.ListView], RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the `position` parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
         * have the updated adapter position.
         *
         * Override [.onBindViewHolder] instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the
         * item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val event = events.get(position)
            holder.updateEvent(event)
        }

    }

    inner class EventViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private var imgEventThumbnail : ImageView = itemView.findViewById(R.id.imgEventThumbnail)
        private var lblEventInfo: TextView = itemView.findViewById(R.id.lblEventInfo)
        private var btnDeleteEvent: ImageButton = itemView.findViewById(R.id.btnDeleteEvent)

        /**
         * This function will get called once for each item in the collection that we want to show in our recylcer view
         * Paint a single row of the recycler view with this event data class.
         */
        fun updateEvent (event : Event) {
            btnDeleteEvent.setOnClickListener {
                deleteEvent(event)
            }
            lblEventInfo.text = event.toString()
            if (event.localPhotoUri != null && event.localPhotoUri != "null") {
                try {
                    // we have an image URI.
                    val source = ImageDecoder.createSource(activity!!.contentResolver, Uri.parse(event.localPhotoUri))
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    // take the image, and put it in the thumbnail of the rowlayout.
                    imgEventThumbnail.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Unable to render bitmap: " + e.message)
                }

            }
        }
     }

    private fun deleteEvent(event: Event) {
        var builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.confirm_delete))
        builder.setMessage(getString(R.string.delete_confirmation_message))
        builder.setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
            viewModel.delete(event)
            dialog.cancel()
        })
        builder.setNegativeButton(getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
            dialog.cancel()
        })
        var alert = builder.create()
        alert.show()

    }


}