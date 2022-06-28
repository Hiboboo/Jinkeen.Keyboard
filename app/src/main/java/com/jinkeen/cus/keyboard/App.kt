package com.jinkeen.cus.keyboard

import android.app.Application
import android.graphics.Color
import com.jinkeen.keyboard.KeyboardStyle

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        KeyboardStyle.KEYBOARD_WIDTH = 1150
        KeyboardStyle.KEY_STYLE_TEXT_SIZE = 26.0f
        KeyboardStyle.KEY_STYLE_TEXT_COLOR = Color.BLUE
        KeyboardStyle.KEY_STYLE_DEL_ICON = R.mipmap.common_icon_click_keyboard_delete
    }
}