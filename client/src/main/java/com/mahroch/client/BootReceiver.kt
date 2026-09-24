package com.mahroch.client

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (!Policy.enabled(c) || !NetworkMonitor.isTarget(c)) return
        if (VpnService.prepare(c) == null) {
            val x = Intent(c, FirewallVpnService::class.java)
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(x)
            else c.startService(x)
        }
    }
}
