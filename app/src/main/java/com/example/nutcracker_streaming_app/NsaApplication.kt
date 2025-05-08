package com.example.nutcracker_streaming_app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.StreamerHelper
import com.example.nutcrackerstreamingapp.R

class NsaApplication: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        NsaPreferences.initWithApplicationContext(this)
        StreamerHelper.initSupportedSettings(this)
    }
    companion object {
        const val ENCRYPTED_SHARED_PREFS_NAME = "ENCRYPTED_SHARED_PREFS_NAME"
        lateinit var encryptedPrefs: SharedPreferences
            private set
        lateinit var prefs: SharedPreferences
            private set

    }

    override fun onCreate() {
        super.onCreate()

        // 1. Build (or retrieve) a MasterKey using the application context
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // 2. Create your EncryptedSharedPreferences with the application context
        encryptedPrefs = EncryptedSharedPreferences.create(
            /* context = */           applicationContext,
            /* fileName = */          ENCRYPTED_SHARED_PREFS_NAME,
            /* masterKey = */    masterKey,
            /* prefKeyEncryptionScheme = */   EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            /* prefValueEncryptionScheme = */ EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val name = applicationContext.getString(R.string.default_preference_name)
        val mode = MODE_PRIVATE
        prefs = applicationContext.getSharedPreferences(name, mode)
    }
}
