package com.tenli.oneview.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentHashMap

class NetworkMonitor(context: Context, private val dispatcherProvider: DispatcherProvider) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        init(context)
    }

    /**
     * true khi có ÍT NHẤT 1 mạng có Internet. Theo dõi tập hợp mạng để khi chuyển Wi-Fi ↔ 4G
     * (mạng cũ onLost trong khi mạng mới vẫn còn) không bị báo "mất mạng" nhầm.
     */
    val isOnline: Flow<Boolean> = callbackFlow {
        val onlineNetworks = ConcurrentHashMap.newKeySet<Network>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onlineNetworks.add(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                onlineNetworks.remove(network)
                trySend(onlineNetworks.isNotEmpty())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    onlineNetworks.add(network)
                } else {
                    onlineNetworks.remove(network)
                }
                trySend(onlineNetworks.isNotEmpty())
            }
        }

        // Send initial state and seed onlineNetworks so initial callbacks don't cause a false offline flicker
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isInitiallyOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (isInitiallyOnline && activeNetwork != null) {
            onlineNetworks.add(activeNetwork)
        }
        trySend(isInitiallyOnline)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.conflate().distinctUntilChanged().flowOn(dispatcherProvider.io)

    companion object {
        @Volatile
        private var appConnectivityManager: ConnectivityManager? = null

        fun init(context: Context) {
            if (appConnectivityManager == null) {
                appConnectivityManager = context.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            }
        }

        /** Kiểm tra nhanh thiết bị hiện có kết nối mạng (Wi-Fi / Cellular / Ethernet / VPN) hay không. */
        fun isNetworkAvailable(): Boolean {
            val cm = appConnectivityManager ?: return true
            return try {
                val activeNetwork = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (_: Exception) {
                true
            }
        }
    }
}
