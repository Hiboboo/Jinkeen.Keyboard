@file:JvmName("KeyboardConstants")

package com.jinkeen.keyboard

import android.graphics.Color
import android.view.WindowManager

/** 为键盘设置统一的简单样式 */
object KeyboardStyle {

    /** 键盘宽度 */
    var KEYBOARD_WIDTH = WindowManager.LayoutParams.MATCH_PARENT

    /** 按键的文字大小 */
    var KEY_STYLE_TEXT_SIZE = 15.0f

    /** 按键的文字颜色 */
    var KEY_STYLE_TEXT_COLOR = Color.BLACK

    /** 删除键图标 */
    var KEY_STYLE_DEL_ICON = R.drawable.ic_abs_keyboard_delete
}

/** SHIFT键 */
const val KEYBOARD_CODE_SHIFT = -20

/** 完成/OK/键 */
const val KEYBOARD_CODE_DONE = -21

/** 空白键 */
const val KEYBOARD_CODE_NOTHING = -22

/** 删除键 */
const val KEYBOARD_CODE_DELETE = -23

/** 全键盘 */
const val KEYBOARD_MODE_QWERTY = 30

/** 数字键盘 */
const val KEYBOARD_MODE_NUMBER = 31

/** 身份证键盘 */
const val KEYBOARD_MODE_IDCARD = 32