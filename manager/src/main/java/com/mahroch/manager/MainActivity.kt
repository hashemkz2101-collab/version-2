package com.mahroch.manager

import android.app.Activity
import android.os.Bundle
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_main)
        val ip = findViewById<EditText>(R.id.clientIp)
        val token = findViewById<EditText>(R.id.token)
        val domains = findViewById<EditText>(R.id.domains)
        val result = findViewById<TextView>(R.id.result)
        domains.setText("mahroch.com\nmahroch-ir.ir\nscript.google.com")

        findViewById<Button>(R.id.test).setOnClickListener {
            val host = ip.text.toString().trim()
            if (host.isBlank()) { result.text = "ابتدا IP گوشی Client را وارد کنید."; return@setOnClickListener }
            Thread {
                try {
                    val c = open("http://$host:8765/health")
                    c.requestMethod = "GET"
                    val code = c.responseCode
                    val body = c.inputStream.bufferedReader().use { it.readText() }
                    runOnUiThread { result.text = if (code == 200) "ارتباط برقرار است.\n$body" else "پاسخ Client: HTTP $code" }
                    c.disconnect()
                } catch (e: Exception) {
                    runOnUiThread { result.text = "خطا در اتصال: ${e.message}" }
                }
            }.start()
        }

        findViewById<Button>(R.id.push).setOnClickListener {
            val host = ip.text.toString().trim()
            val t = token.text.toString().trim()
            val d = domains.text.toString()
            if (host.isBlank()) { result.text = "IP گوشی Client را وارد کنید."; return@setOnClickListener }
            if (t.isBlank()) { result.text = "کد اتصال Client را وارد کنید."; return@setOnClickListener }
            Thread {
                try {
                    val c = open("http://$host:8765/policy")
                    c.requestMethod = "POST"
                    c.doOutput = true
                    c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    c.setRequestProperty("X-Mahroch-Token", t)
                    val body = "token=${URLEncoder.encode(t, "UTF-8")}&domains=${URLEncoder.encode(d, "UTF-8")}"
                    c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    val code = c.responseCode
                    val stream = if (code in 200..399) c.inputStream else c.errorStream
                    val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
                    runOnUiThread {
                        result.text = when (code) {
                            200 -> "سیاست با موفقیت به Client ارسال شد.\n$response"
                            401 -> "کد اتصال اشتباه است."
                            400 -> "دامنه‌های ارسالی معتبر نیستند."
                            else -> "پاسخ Client: HTTP $code\n$response"
                        }
                    }
                    c.disconnect()
                } catch (e: Exception) {
                    runOnUiThread { result.text = "خطا در ارسال: ${e.message}" }
                }
            }.start()
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 5000; readTimeout = 5000 }
}
