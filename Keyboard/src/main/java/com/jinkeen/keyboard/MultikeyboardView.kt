package com.jinkeen.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@SuppressLint("ResourceType")
internal class MultikeyboardView(context: Context, attrs: AttributeSet) : KeyboardView(context, attrs) {

    // 数字键盘
    private val sKeyboardNumber: Keyboard by lazy { Keyboard(context, R.xml.keyboard_number_layout) }

    // 全键盘
    private val sKeyboardQwerty: Keyboard by lazy { Keyboard(context, R.xml.keyboard_qwerty_layout) }

    // 身份证键盘
    private val sKeyboardIdcard: Keyboard by lazy { Keyboard(context, R.xml.keyboard_idcard_layout) }

    init {
        this.keyboard = sKeyboardQwerty
        this.isEnabled = true
        this.isPreviewEnabled = false
        this.keyboard.isShifted = true
    }

    enum class KeyboardMode {
        /** 全键盘模式 */
        KEYBOARD_MODEL_QWERTY,

        /** 数字键盘模式 */
        KEYBOARD_MODEL_NUMBER,

        /** 身份证键盘模式 */
        KEYBOARD_MODEL_IDCARD
    }

    // 键盘模式
    private var keyboardMode = KeyboardMode.KEYBOARD_MODEL_QWERTY

    fun toggleMode(mode: KeyboardMode) {
        keyboardMode = mode
        when (mode) {
            KeyboardMode.KEYBOARD_MODEL_QWERTY -> {
                keyboard = sKeyboardQwerty
                keyboard.isShifted = true
            }
            KeyboardMode.KEYBOARD_MODEL_NUMBER -> {
                keyboard = sKeyboardNumber
            }
            KeyboardMode.KEYBOARD_MODEL_IDCARD -> {
                keyboard = sKeyboardIdcard
            }
        }
    }

    private var isAllowDecimalPoint = true

    fun setAllowDecimalPoint(isAllow: Boolean) {
        isAllowDecimalPoint = isAllow
    }

    private val mPaint: Paint by lazy {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        Log.d("MultikeyboardView", "keyboard=$keyboard")
        keyboard?.keys?.forEach { key ->
            when (val code = key.codes[0]) {
                KEYBOARD_CODE_DONE -> {
                    drawKeyBackground(R.drawable.bg_selector_key_done, canvas, key)
                    drawText(canvas, key)
                }
                KEYBOARD_CODE_SHIFT, KEYBOARD_CODE_DELETE -> {
                    drawKeyBackground(R.drawable.bg_selector_key_action, canvas, key)
                    drawText(canvas, key)
                    if (code == KEYBOARD_CODE_DELETE) drawIcon(canvas, key)
                }
                KEYBOARD_CODE_NOTHING -> drawKeyBackground(R.drawable.bg_key_no_decimal_point, canvas, key)
                else -> {
                    if (code == 46 && !isAllowDecimalPoint) drawKeyBackground(R.drawable.bg_key_no_decimal_point, canvas, key)
                    else if (key.edgeFlags == Keyboard.EDGE_LEFT) drawKeyBackground(R.drawable.bg_selector_key2_normal, canvas, key)
                    else drawKeyBackground(R.drawable.bg_selector_key_normal, canvas, key)
                    drawText(canvas, key)
                }
            }
        }
    }

    // 绘制按键背景
    private fun drawKeyBackground(@DrawableRes backgroundRes: Int, canvas: Canvas, key: Keyboard.Key) {
        val background = ContextCompat.getDrawable(context, backgroundRes) ?: return
        if (key.codes[0] != 0) background.state = key.currentDrawableState
        key.run { background.setBounds(x + paddingStart, y + paddingTop, x + paddingStart + width, y + paddingTop + height) }
        background.draw(canvas)
    }

    // 绘制按键label
    private fun drawText(canvas: Canvas, key: Keyboard.Key) {
        key.label ?: return
        mPaint.color = KeyboardStyle.KEY_STYLE_TEXT_COLOR
        mPaint.textSize = KeyboardStyle.KEY_STYLE_TEXT_SIZE
        canvas.drawText(
            key.label.toString(), (key.x + ((key.width - paddingLeft - paddingRight) / 2 + paddingLeft)).toFloat(),
            key.y + ((key.height - paddingTop - paddingBottom) / 2).toFloat() + (mPaint.textSize - mPaint.descent()) / 2 + paddingTop.toFloat(),
            mPaint
        )
    }

    // 绘制按键icon
    private fun drawIcon(canvas: Canvas, key: Keyboard.Key) {
        ContextCompat.getDrawable(context, KeyboardStyle.KEY_STYLE_DEL_ICON)?.let { drawable ->
            val intrinsicWidth = drawable.intrinsicWidth
            val intrinsicHeight = drawable.intrinsicHeight
            val drawKeyWidth = if (intrinsicWidth > key.width) key.width else intrinsicWidth
            val drawKeyHeight = if (intrinsicHeight > key.height) key.height else intrinsicHeight
            val left = (key.x + (key.width - drawKeyWidth) / 2 + paddingLeft)
            val top = (key.y + (key.height - drawKeyHeight) / 2 + paddingTop)
            drawable.setBounds(left, top, left + drawKeyWidth, top + drawKeyHeight)
            drawable.draw(canvas)
        }
    }
}