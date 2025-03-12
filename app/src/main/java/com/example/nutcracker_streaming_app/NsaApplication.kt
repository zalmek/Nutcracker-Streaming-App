package com.example.nutcracker_streaming_app

import android.app.Application
import android.content.Context
import com.example.nutcracker_streaming_app.utils.NsaPreferences

class NsaApplication: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        NsaPreferences.initWithApplicationContext(this)
    }
}