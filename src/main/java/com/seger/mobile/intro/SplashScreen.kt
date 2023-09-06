package com.seger.mobile.intro

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.seger.mobile.R
import com.seger.mobile.web.DetectConnection
import com.seger.mobile.web.DialogNoInternet
import com.seger.mobile.web.MainActivity


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private lateinit var textViewCurrentVersion: TextView
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash)
        textViewCurrentVersion = findViewById<View>(R.id.versionCode) as TextView
        textViewCurrentVersion.text = getString(R.string.currentVerCode )  + getVersionCode()

        val defaultsRate = HashMap<String, Any>()
        defaultsRate["new_version_code"] = getVersionCode().toString()

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .build()

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(defaultsRate)

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val newVersionCode = mFirebaseRemoteConfig.getString("new_version_code")
                if (newVersionCode.toInt() > getVersionCode()) showTheDialog()
            } else Log.e(
                "MY LOG",
                "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful"
            )
        }

        val handler = Handler()
        handler.postDelayed({
            if (!DetectConnection.checkInternetConnection(this)) {
                val i = Intent(applicationContext, DialogNoInternet::class.java)
                startActivity(i)
            } else {
                startActivity(Intent(applicationContext, IntroActivity::class.java))
            }
            finish()
        }, 3000L)
    }

    private fun showTheDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_title)
            .setMessage(getString(R.string.update_message))
            .setPositiveButton(R.string.update_button, null)
            .show()
        dialog.setCancelable(false)
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/smileymunabbich/seger-mobile/releases/download/release/seger-latest-release.apk")
                    )
                )
            } catch (ActivityNotFoundException: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/smileymunabbich/seger-mobile/releases/download/release/seger-latest-release.apk")
                    )
                )
            }
        }

    }

    private var pInfo: PackageInfo? = null
    private fun getVersionCode(): Int {
        pInfo = null
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("MY LOG", "NameNotFoundException: " + e.message)
        }
        return pInfo!!.versionCode
    }
}