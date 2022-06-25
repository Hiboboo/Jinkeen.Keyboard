package com.jinkeen.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.KeyboardView
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class JinkeenKeyboardManager(private val keyboardView: MultikeyboardView) {

    private var sAttachedEditText: EditText? = null

    @SuppressLint("ClickableViewAccessibility")
    fun attachTo(editText: EditText) {
        sAttachedEditText = editText
        keyboardView.setOnKeyboardActionListener(sKeyboardActionListener)
        editText.setOnClickListener { this.showKeyboard() }
        editText.setOnTouchListener { _, _ ->
            editText.requestFocus()
            editText.requestFocusFromTouch()
            hideSystemSoftKeyboard(editText)
            return@setOnTouchListener false
        }
//        editText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) this.hideKeyboard() }
    }

    private fun showKeyboard() {
        if (keyboardView.visibility != View.VISIBLE) keyboardView.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        if (keyboardView.visibility == View.VISIBLE) keyboardView.visibility = View.GONE
    }

    companion object {

        private const val TAG = "JinkeenKeyboardManager"

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

    private val sKeyboardActionListener = object : KeyboardView.OnKeyboardActionListener {

        override fun onPress(primaryCode: Int) {
            Log.d(TAG, "onPress(primaryCode=$primaryCode)")
        }

        override fun onRelease(primaryCode: Int) {
            Log.d(TAG, "onRelease(primaryCode=$primaryCode)")
            if (primaryCode == KEYBOARD_CODE_DONE) hideKeyboard()
        }

        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            Log.d(TAG, "onKey(primaryCode=$primaryCode, keyCodes=$keyCodes)")
        }

        override fun onText(text: CharSequence?) {
            Log.d(TAG, "onText(text=$text)")
        }

        override fun swipeLeft() {
            Log.d(TAG, "swipeLeft()")
        }

        override fun swipeRight() {
            Log.d(TAG, "swipeRight()")
        }

        override fun swipeDown() {
            Log.d(TAG, "swipeDown()")
        }

        override fun swipeUp() {
            Log.d(TAG, "swipeUp()")
        }
    }
}