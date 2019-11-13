package com.example.learnnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var idNotification = 0

    private val stackNotify = ArrayList<NotificationItem>()

    companion object{
        private const val CHANNEL_NAME = "dicoding_channel"
        private const val GROUP_KEY_EMAILS = "group_key_emails"
        private const val NOTIFICATION_REQUEST_CODE = 200
        private const val MAX_NOTIFICATION = 2
        const val CHANNEL_ID = "channel_01"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSend.setOnClickListener {
            val sender =  edtSender.text.toString()
            val message = edtMessage.text.toString()
            if (sender.isEmpty() || message.isEmpty()){
                Toast.makeText(this@MainActivity, "Data harus diisi", Toast.LENGTH_SHORT).show()
            }else{
                stackNotify.add(NotificationItem(idNotification, sender, message))
                sendNotify()
                idNotification++
                edtSender.setText("")
                edtMessage.setText("")

                //Tutup keyboard ketika tombol di klik
                val methodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                methodManager.hideSoftInputFromWindow(edtMessage.windowToken, 0)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        stackNotify.clear()
        idNotification = 0
    }

    private fun sendNotify() {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_white_48dp)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mBuilder: NotificationCompat.Builder

        //Melakukan pengecekan jika idNotification lebih kecil dari max Notif

        if (idNotification < MAX_NOTIFICATION){
            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New Email from " + stackNotify[idNotification].sender)
                .setContentText(stackNotify[idNotification].message)
                .setSmallIcon(R.drawable.ic_email_white_48dp)
                .setLargeIcon(largeIcon)
                .setGroup(GROUP_KEY_EMAILS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }else{
            val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("New Email from " + stackNotify[idNotification].sender)
                .addLine("New Email from " + stackNotify[idNotification - 1].sender)
                .setBigContentTitle("$idNotification new emails")
                .setSummaryText("mail@dicoding")

            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("$idNotification new emails")
                .setContentText("mail@dicoding")
                .setSmallIcon(R.drawable.ic_email_white_48dp)
                .setGroup(GROUP_KEY_EMAILS)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .setAutoCancel(true)
        }

        /*
        Untuk android Oreo ke atas perlu menambahkan notification channel
        Materi ini akan dibahas lebih lanjut di modul extended
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Create or update
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

            mBuilder.setChannelId(CHANNEL_ID)

            mNotificationManager.createNotificationChannel(channel)
        }

        val notification = mBuilder.build()
        mNotificationManager.notify(idNotification, notification)
    }
}
