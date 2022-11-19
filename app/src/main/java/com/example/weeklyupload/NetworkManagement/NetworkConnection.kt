package com.example.finalandroid.adapter.NetworkManagement

import android.app.usage.NetworkStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LiveData

class NetworkConnection(val context: Context) : LiveData<Boolean>() {


    private lateinit var networkConnectionCallback: ConnectivityManager.NetworkCallback

    private val connectivity: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun onActive() {
        super.onActive()
        updateConnection()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivity.registerDefaultNetworkCallback(connetionCallback())
            }
            else -> {

                context.registerReceiver(
                    networkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }

        }
    }

    override fun onInactive() {
        super.onInactive()
        //Toast.makeText(context, "Connection tracking END", Toast.LENGTH_SHORT).show()
        connectivity.unregisterNetworkCallback(networkConnectionCallback)
    }

    fun connetionCallback(): ConnectivityManager.NetworkCallback {

        networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
        }

        return networkConnectionCallback
    }

    private fun updateConnection() {
        val networkConnection: NetworkInfo? = connectivity.activeNetworkInfo
        postValue(networkConnection?.isConnected == true)
    }

    private val networkReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            updateConnection()
        }
    }
}