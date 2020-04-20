package edu.uc.jonesbr.myplantdiary

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

class NotificationReceiver : BroadcastReceiver() {

    private var context: Context? = null
    private lateinit var channel : NotificationChannel
    private var isChannelCreated = false
    private val EVENT_CHANNEL_ID = "EVENT_CHANNEL_ID"

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * [android.content.Context.registerReceiver]. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     *
     *
     * **If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.** This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a [android.app.job.JobService] with
     * [android.app.job.JobScheduler].
     *
     * If you wish to interact with a service that is already running and previously
     * bound using [bindService()][android.content.Context.bindService],
     * you can use [.peekService].
     *
     *
     * The Intent filters used in [android.content.Context.registerReceiver]
     * and in application manifests are *not* guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, [onReceive()][.onReceive]
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        val action = intent?.action
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        var networkType = ""

        capabilities ?: return

        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                networkType = "Wifi"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                networkType = "Cellular"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                networkType = "Ethernet"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                networkType = "Bluetooth"
            }
        }

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                notifyUser()
            }
            Intent.ACTION_POWER_DISCONNECTED ->  {

            }
        }
    }

    private fun notifyUser() {

        if (!isChannelCreated) {
            createChannel()
        }
        val mBuilder = NotificationCompat.Builder(context!!, EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Watch your Plants!")
            .setContentText("Your Redbud is blooming!")
        val notification = mBuilder.build()
        val notificationManagerCompat = NotificationManagerCompat.from(context!!)
        notificationManagerCompat.notify(1, notification)

    }

    private fun createChannel() {
        channel = NotificationChannel(EVENT_CHANNEL_ID, "Plant Events", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "A channel for plant events"
        channel.lightColor = Color.GREEN
        val notificationManager = getSystemService(context!!, NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
        isChannelCreated = true
    }
}