package com.jinkeen.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentActivity
import kotlin.math.max

class SafetyInputEditText(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatEditText(context, attrs, defStyleAttr) {

    constructor(@NonNull context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.editTextStyle)

    constructor(@NonNull context: Context) : this(context, null)

    private val sKeyboardMode: Int
    private val isAllowDecimalPoint: Boolean

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SafetyInputEditText)
        sKeyboardMode = a.getInt(R.styleable.SafetyInputEditText_si_keyboard_type, KEYBOARD_MODE_QWERTY)
        isAllowDecimalPoint = a.getBoolean(R.styleable.SafetyInputEditText_si_decimal_point, true)
        a.recycle()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (sKeyboardMode == KEYBOARD_MODE_IDCARD || sKeyboardMode == KEYBOARD_MODE_NUMBER)
            if (id == android.R.id.paste) {
                Log.d(TAG, "不允许粘贴")
                return true
            }
        return super.onTextContextMenuItem(id)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d(TAG, "dispatchKeyEvent(event=$event)")
        event?.let {
            if (it.action == MotionEvent.ACTION_UP && it.keyCode == KeyEvent.KEYCODE_BACK && dialog.isShowing()) {
                dialog.dismiss()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> performClick()
            MotionEvent.ACTION_UP -> this.showKeyboard()
        }
        return super.onTouchEvent(event)
    }

    private var dialog = SoftInputDialog()

    private fun showKeyboard() {
        this.requestFocus()
        this.requestFocusFromTouch()
        hideSystemSoftKeyboard(this)
        (context as? FragmentActivity)?.let {
            dialog.setOnKeyboardActionClickListener(sKeyboardListener)
            dialog.show(it.supportFragmentManager)
            dialog.toggleKeyboard(sKeyboardMode, isAllowDecimalPoint)
        }
    }

    private val sIdenittyCode = System.identityHashCode(this)
    private val sLayoutRect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        sLayoutRect.setEmpty()
        this.getGlobalVisibleRect(sLayoutRect)
        Log.d(TAG, "onLayout(changedView=$this, left=${sLayoutRect.left}, top=${sLayoutRect.top}, right=${sLayoutRect.right}, bottom=${sLayoutRect.bottom})")
        dialog.replaceChildViewCoordinate(sIdenittyCode, sLayoutRect)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "onVisibilityChanged(changedView=$changedView, visibility=$visibility)")
        if (visibility != View.VISIBLE) dialog.removeChildViewCoordinate(sIdenittyCode)
    }

    private val sKeyboardListener = object : SoftInputDialog.OnKeyboardActionClickListener {
        override fun onPress(primaryCode: Int) {
            Log.d(TAG, "onPress(primaryCode=$primaryCode)")
        }

        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            Log.d(TAG, "onKey(primaryCode=$primaryCode, keyCodes=${buildString { keyCodes?.forEach { append(it).append('\t') } }})")
            when (primaryCode) {
                KEYBOARD_CODE_DELETE -> {
                    var begin = selectionStart
                    val end = selectionEnd
                    if (begin == end) begin = (end - 1)
                    editableText.delete(max(begin, 0), end)
                }
                KEYBOARD_CODE_CLEAR -> editableText.clear()
                else -> {
                    if (primaryCode == 46 && !isAllowDecimalPoint) return
                    val begin = selectionStart
                    val end = selectionEnd
                    val letter = Character.toString(primaryCode.toChar())
                    Log.d(TAG, "onKey(Letter=$letter)")
                    editableText.replace(begin, end, letter)
                    if (begin != end) setSelection(begin + length())
                }
            }
        }

        override fun onRelease(primaryCode: Int) {
            Log.d(TAG, "onRelease(primaryCode=$primaryCode)")
        }
    }

    companion object {

        private const val TAG = "SafetyInputEditText"

        fun hideSystemSoftKeyboard(editText: EditText) {
            editText.showSoftInputOnFocus = false
            try {
                val softInputOnFocus = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                softInputOnFocus.isAccessible = true
                softInputOnFocus.invoke(editText, false)
            } catch (e: Exception) {
                Log.e(TAG, "隐藏系统键盘出现异常", e)
            }
            (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }
}