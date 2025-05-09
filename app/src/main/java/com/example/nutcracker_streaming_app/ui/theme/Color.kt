package com.example.nutcracker_streaming_app.ui.theme

import android.graphics.Color

object Colors {
    object Utility {
        val ripple = getColor("#FFFFFF")
    }

    object Button {
        val radioButtonChecked = getColor("#49A7FF")
    }

    object Background {
        val main = getColor("#171F27")
        val row = getColor("#1C252E")
        val dialog = getColor("#25333E")
        val button = getColor("#49A7FF")
    }

    object Text {
        val primary = getColor("#FFFFFF")
        val secondary = getColor("#C5C5C5")
        val primaryContract = getColor("#000000")
        val action = getColor("#49A7FF")
    }

    object Icons {
        val primary = getColor("#FFFFFF")
    }

}

private fun getColor(colorString: String): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(Color.parseColor(colorString))
}