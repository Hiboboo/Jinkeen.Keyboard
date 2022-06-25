package com.jinkeen.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@SuppressLint("ResourceType")
class MultikeyboardView(context: Context, attrs: AttributeSet) : KeyboardView(context, attrs) {

    // 数字键盘
    private val sKeyboardNumber: Keyboard by lazy { Keyboard(context, R.xml.keyboard_number_layout) }
    // 全键盘
    private val sKeyboardQwerty: Keyboard by lazy { Keyboard(context, R.xml.keyboard_qwerty_layout) }
    // 身份证键盘
    private val sKeyboardIdcard: Keyboard by lazy { Keyboard(context, R.xml.keyboard_idcard_layout) }

    companion object {
        // 全键盘模式模式下按键背景
        private val QWERTY_KEY_BACKGROUND = Triple(R.drawable.bg_selector_key_done, R.drawable.bg_selector_key_action, R.drawable.bg_selector_key_normal)
        // 数字键盘模式模式下按键背景
        private val NUMBER_KEY_BACKGROUND = Triple(R.drawable.bg_selector_key_done, R.drawable.bg_selector_key_action, R.drawable.bg_selector_key_normal)
        // 身份证键盘模式模式下按键背景
        private val IDCARD_KEY_BACKGROUND = Triple(R.drawable.bg_selector_key_done, R.drawable.bg_selector_key_action, R.drawable.bg_selector_key_normal)
    }

    // 键盘文字大小
    private var sLabelTextSize = 15
    // 普通按键的文字颜色
    private var sKeyTextColor: Int = Color.BLACK

    // 按键背景资源
    private var sKeyBackgroundResourceTriple = QWERTY_KEY_BACKGROUND

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
                sKeyBackgroundResourceTriple = QWERTY_KEY_BACKGROUND
            }
            KeyboardMode.KEYBOARD_MODEL_NUMBER -> {
                keyboard = sKeyboardNumber
                sKeyBackgroundResourceTriple = NUMBER_KEY_BACKGROUND
            }
            KeyboardMode.KEYBOARD_MODEL_IDCARD -> {
                keyboard = sKeyboardIdcard
                sKeyBackgroundResourceTriple = IDCARD_KEY_BACKGROUND
            }
        }
    }

    private var isAllowDecimalPoint = true

    fun setAllowDecimalPoint(isAllow: Boolean) {
        isAllowDecimalPoint = isAllow
    }

    private val mPaint: Paint by lazy { Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    } }

    init {
        val a = context.obtainStyledAttributes(attrs, intArrayOf(
            android.R.attr.labelTextSize, android.R.attr.keyTextColor
        ))
        sLabelTextSize = a.getDimensionPixelSize(0, sLabelTextSize)
        sKeyTextColor = a.getColor(1, sKeyTextColor)
        a.recycle()

        this.keyboard = sKeyboardQwerty
        this.isEnabled = true
        this.isPreviewEnabled = false
        this.keyboard.isShifted = true
    }

    @Deprecated("Deprecated in Java")
    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        keyboard?.keys?.forEach { key -> when(val code = key.codes[0]) {
            KEYBOARD_CODE_DONE -> {
                drawKeyBackground(sKeyBackgroundResourceTriple.first, canvas, key)
                drawText(canvas, key, Color.WHITE)
                drawIcon(canvas, key)
            }
            KEYBOARD_CODE_SHIFT, KEYBOARD_CODE_DELETE -> {
                drawKeyBackground(sKeyBackgroundResourceTriple.second, canvas, key)
                drawText(canvas, key, sKeyTextColor)
                drawIcon(canvas, key)
            }
            KEYBOARD_CODE_NOTHING -> drawKeyBackground(R.drawable.bg_key_no_decimal_point, canvas, key)
            else -> {
                if (code == 46 && !isAllowDecimalPoint) drawKeyBackground(R.drawable.bg_key_no_decimal_point, canvas, key)
                else drawKeyBackground(sKeyBackgroundResourceTriple.third, canvas, key)
                drawText(canvas, key, sKeyTextColor)
                drawIcon(canvas, key)
            }
        } }
    }

    // 绘制按键背景
    private fun drawKeyBackground(@DrawableRes backgroundRes: Int, canvas: Canvas, key: Keyboard.Key) {
        val background = ContextCompat.getDrawable(context, backgroundRes) ?: return
        if (key.codes[0] != 0) background.state = key.currentDrawableState
        key.run { background.setBounds(x + paddingStart, y + paddingTop, x + paddingStart + width, y + paddingTop + height) }
        background.draw(canvas)
    }

    // 绘制按键label
    private fun drawText(canvas: Canvas, key: Keyboard.Key, color: Int) {
        key.label ?: return
        mPaint.color = color
        mPaint.textSize = sLabelTextSize.toFloat()
        canvas.drawText(
            key.label.toString(), (key.x + ((key.width - paddingLeft - paddingRight) / 2 + paddingLeft)).toFloat(),
            key.y + ((key.height - paddingTop - paddingBottom) / 2).toFloat() + (mPaint.textSize - mPaint.descent()) / 2 + paddingTop.toFloat(),
            mPaint
        )
    }

    // 绘制按键icon
    private fun drawIcon(canvas: Canvas, key: Keyboard.Key) {
        key.icon ?: return

        key.icon.setBounds(
            key.x + (key.width - key.icon.intrinsicWidth) / 2 + paddingStart,
            key.y + (key.height - key.icon.intrinsicHeight) / 2 + paddingTop,
            key.x + (key.width - key.icon.intrinsicWidth) / 2 + key.icon.intrinsicWidth + paddingStart,
            key.y + (key.height - key.icon.intrinsicHeight) / 2 + key.icon.intrinsicHeight + paddingTop
        )
        key.icon.draw(canvas)
    }
}