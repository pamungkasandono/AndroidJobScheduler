package com.example.androidmyjobscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.lang.Exception
import java.nio.channels.Channel
import java.text.DecimalFormat

class GetCurrentWeatherJobService : JobService() {
    companion object {
        internal const val CITY = "Purbalingga"

        internal const val APP_ID = "api_key"
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("Debug", "onStartJob")
        getCurrentWeather(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("Debug", "onStopJob")
        return true
    }

    private fun getCurrentWeather(job: JobParameters?) {
        Log.d("Debug", "getCurrentWeather: Mulai...")
        val client = AsyncHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=$APP_ID"
        Log.d("Debug", "getCurrentWeather: $url")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = String(responseBody!!)
                Log.d("Debug", result)
                try {
                    val resOb = JSONObject(result)

                    val currentWeather =
                        resOb.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description =
                        resOb.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempInKelvin = resOb.getJSONObject("main").getDouble("temp")

                    val tempInCelcius = tempInKelvin - 273
                    val temperature = DecimalFormat("##.##").format(tempInCelcius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celcius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId)

                    Log.d("Debug", "onSuccess : Selesai....")
                    jobFinished(job, false)

                } catch (e: Exception) {
                    Log.d("Debug", "onSuccess: Gagal...")
                    jobFinished(job, true)
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.d("Debug", "onFailur : Gagal...")
                jobFinished(job, true)
            }

        })
    }

    private fun showNotification(
        contex: Context?,
        title: String,
        message: String,
        notifId: Int
    ) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"

        val notificationManagerCompat =
            contex?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(contex, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_round_wb_sunny_24)
            .setContentText(message)
            .setColor(ContextCompat.getColor(contex, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }

            builder.setChannelId(CHANNEL_ID)
            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManagerCompat.notify(notifId, notification)
    }
}












