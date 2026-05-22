package com.example.a207402_yanglizixuan_cikgulzwan_lab5.ui.theme

import androidx.compose.ui.graphics.Color

var appPrimaryColor: Color = Color(0xFF2196F3)
var appAccentColor: Color = Color(0xFFFF9800)

fun setAppThemeColor(newPrimary: Color, newAccent: Color) {
    appPrimaryColor = newPrimary
    appAccentColor = newAccent
}