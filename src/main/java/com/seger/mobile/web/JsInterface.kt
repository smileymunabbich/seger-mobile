package com.seger.mobile.web

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.seger.mobile.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class JsInterface(private val context: Context) {
    @JavascriptInterface
    @Throws(IOException::class)
    fun getBase64FromBlobData(base64Data: String) {
        Log.e("mimeType", "getBase64FromBlobData:  $mimTp")
        convertBase64StringToPdfAndStoreIt(base64Data)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToPdfAndStoreIt(base64PDf: String) {
        Log.e("BASE 64", base64PDf)
        val notificationId = 1
        val dwldsPath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$fileName")
        val pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:$mimTp;base64,".toRegex(), ""), 0)
        val os = FileOutputStream(dwldsPath, false)
        os.write(pdfAsBytes)
        os.flush()

        if (dwldsPath.exists()) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val apkURI = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", dwldsPath)
            intent.setDataAndType(apkURI, mimTp)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            var intentFlagType = PendingIntent.FLAG_ONE_SHOT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                intentFlagType = PendingIntent.FLAG_IMMUTABLE
            }
            val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, intentFlagType)
            val CHANNEL_ID = "MYCHANNEL"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_HIGH)
                val notification = Notification.Builder(
                    context, CHANNEL_ID
                )
                    .setContentText(fileName)
                    .setContentTitle("File downloaded")
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.app_icon_notif)
                    .build()
                notificationManager.createNotificationChannel(notificationChannel)
                notificationManager.notify(notificationId, notification)
            } else {
                val b = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.app_icon_notif)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("File downloaded")
                notificationManager.notify(notificationId, b.build())
                val h = Handler()
                val delayInMilliseconds: Long = 1000
                h.postDelayed({ notificationManager.cancel(notificationId) }, delayInMilliseconds)
            }
        }
    }

    companion object {
        var mimTp:String = ""
        var fileName:String = ""
        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String,fileName:String): String {
            mimTp = mimeType
            this.fileName = fileName
            return if (blobUrl.startsWith("blob")) {
                "javascript: var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','$mimeType');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobPdf = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobPdf);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            Android.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            } else "javascript: console.log('It is not a Blob URL');"
        }
    }
}