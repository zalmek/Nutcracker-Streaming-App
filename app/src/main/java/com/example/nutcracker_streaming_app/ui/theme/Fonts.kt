package com.example.nutcracker_streaming_app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.nutcrackerstreamingapp.R

object Fonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val roboto = GoogleFont("Roboto")

    val robotoFamily = FontFamily(
        Font(googleFont = roboto, fontProvider = provider)
    )
}