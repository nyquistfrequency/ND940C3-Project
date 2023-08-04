package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loadingButton: LoadingButton

    private var downloadStatus = "Failed"
    private var downloadID: Long = 0
    private val notificationID = 0


    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        createChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        loadingButton = findViewById(R.id.custom_button)

        val radioGroup = findViewById<RadioGroup>(R.id.download_radio_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            radioButtonSelection(radioButton)
        }

        loadingButton.setOnClickListener {
            val selectedRadioButtonId =
                radioGroup.checkedRadioButtonId // Update the selectedRadioButtonId
            if (selectedRadioButtonId == -1) {
                Toast.makeText(
                    this,
                    getString(R.string.button_with_no_selection),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingButton.buttonState = ButtonState.Loading
                val radioButton = findViewById<RadioButton>(selectedRadioButtonId)
                val url =
                    radioButtonSelection(radioButton) // Retrieve the URL from radioButtonSelection()
                Toast.makeText(this, url, Toast.LENGTH_SHORT).show() // check for used URL
                download(url) // Pass the URL to the download() function
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadID)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    downloadStatus = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        "Success"
                    } else {
                        "Failed"
                    }
                }
                cursor.close()

                notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.sendNotification(
                    "Download complete!",
                    applicationContext,
                    downloadStatus
                )
                loadingButton.buttonState = ButtonState.Completed
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

    }

    private fun radioButtonSelection(radioButton: RadioButton): String {
        return when (radioButton.id) {
            R.id.glide_radio_selection -> URL.GLIDE_URI.uri
            R.id.loadapp_radio_selection -> URL.UDACITY_PROJECT_URI.uri
            R.id.retrofit_radio_selection -> URL.RETROFIT_URI.uri
            else -> ""
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                applicationContext.getString(R.string.repo_notification_channel_id),
                "LoadAppChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.description = "Download complete!"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context,
        status: String
    ) {

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        contentIntent.putExtra("fileName", "Test") //ToDo
        contentIntent.putExtra("status", status)

        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action =
            NotificationCompat.Action.Builder(0, "Show Details", contentPendingIntent).build()

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.repo_notification_channel_id)
        )
            .setContentTitle("Test")
            .setContentText(status)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.cloud_image)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notify(notificationID, builder.build())
    }

    companion object {
        private enum class URL(val uri: String, val notificationTitle: String) {
            GLIDE_URI(
                "https://github.com/bumptech/glide/archive/master.zip",
                "The glide repository is downloaded"
            ),
            UDACITY_PROJECT_URI(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
                "The project 3 repository is downloaded"
            ),
            RETROFIT_URI(
                "https://github.com/square/retrofit/archive/master.zip",
                "The retrofit repository is downloaded"
            )
        }
    }
}