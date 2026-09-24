package com.mahroch.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.net.wifi.WifiManager
import android.os.Build

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, intent: Intent) {
        val active = NetworkMonitor.isTarget(c)
        if (active && Policy.enabled(c) && VpnService.prepare(c) == null) {
            val x = Intent(c, FirewallVpnService::class.java)
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(x) else c.startService(x)
        } else if (!active) {
            c.stopService(Intent(c, FirewallVpnService::class.java))
        }
    }
}
