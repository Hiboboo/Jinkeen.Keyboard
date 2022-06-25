package com.jinkeen.keyboard

import android.content.DialogInterface
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.jinkeen.keyboard.databinding.SiDialogKeyboardLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SoftInputDialog private constructor() : DialogFragment(R.layout.si_dialog_keyboard_layout) {

    companion object {

        private const val TAG = "SoftInputDialog"

        private val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SoftInputDialog() }

        operator fun invoke(): SoftInputDialog = instance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = true
        this.setStyle(STYLE_NORMAL, R.style.SoftInputBottomDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.BOTTOM)
            val attributes = it.attributes
            attributes.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            it.attributes = attributes
        }
    }

    private lateinit var layoutBinding: SiDialogKeyboardLayoutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = super.onCreateView(inflater, container, savedInstanceState)
        contentView?.let { layoutBinding = DataBindingUtil.bind(it)!! }
        return contentView
    }

    interface OnKeyboardActionClickListener {

        fun onPress(primaryCode: Int)

        fun onKey(primaryCode: Int, keyCodes: IntArray?)

        fun onRelease(primaryCode: Int)
    }

    private var listener: OnKeyboardActionClickListener? = null

    fun setOnKeyboardActionClickListener(listener: OnKeyboardActionClickListener) {
        this.listener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.keyboardView.setOnKeyboardActionListener(object : KeyboardView.OnKeyboardActionListener {
            override fun onPress(primaryCode: Int) {
                Log.d(TAG, "onPress(primaryCode=$primaryCode)")
                listener?.onPress(primaryCode)
            }

            override fun onRelease(primaryCode: Int) {
                Log.d(TAG, "onRelease(primaryCode=$primaryCode)")
                listener?.onRelease(primaryCode)
            }

            override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
                Log.d(TAG, "onKey(primaryCode=$primaryCode, keyCodes=$keyCodes)")
                when (primaryCode) {
                    KEYBOARD_CODE_DONE -> dismiss()
                    KEYBOARD_CODE_NOTHING -> return
                    KEYBOARD_CODE_SHIFT -> changeKey(primaryCode)
                    else -> listener?.onKey(primaryCode, keyCodes)
                }
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
        })
    }

    private fun changeKey(shiftCode: Int) {
        if (!this::layoutBinding.isInitialized) return
        val isShifted = layoutBinding.keyboardView.keyboard.isShifted
        if (isShifted) // 大写切小写
            layoutBinding.keyboardView.keyboard.keys.forEach { key ->
                if (key.codes[0] == shiftCode) key.label = resources.getString(R.string.si_label_switch_uppercase)
                key.label?.let { label ->
                    if (this.isLetter(label.toString())) {
                        key.label = label.toString().lowercase()
                        key.codes[0] = key.codes[0] + 32
                    }
                }
            }
        else // 小写切大写
            layoutBinding.keyboardView.keyboard.keys.forEach { key ->
                if (key.codes[0] == shiftCode) key.label = resources.getString(R.string.si_label_switch_lowercase)
                key.label?.let { label ->
                    if (this.isLetter(label.toString())) {
                        key.label = label.toString().uppercase()
                        key.codes[0] = key.codes[0] - 32
                    }
                }
            }
        layoutBinding.keyboardView.keyboard.isShifted = !isShifted
        layoutBinding.keyboardView.invalidateAllKeys()
    }

    private fun isLetter(s: String): Boolean = s.matches(Regex("[A-Za-z]"))

    fun toggleKeyboard(boardType: Int, isAllowDecimalPoint: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            while (!this@SoftInputDialog::layoutBinding.isInitialized) {
                Log.d(TAG, "// Waiting layoutbingind init.")
            }
            withContext(Dispatchers.Main) {
                when (boardType) {
                    KEYBOARD_MODE_QWERTY -> layoutBinding.keyboardView.toggleMode(MultikeyboardView.KeyboardMode.KEYBOARD_MODEL_QWERTY)
                    KEYBOARD_MODE_NUMBER -> layoutBinding.keyboardView.toggleMode(MultikeyboardView.KeyboardMode.KEYBOARD_MODEL_NUMBER)
                    KEYBOARD_MODE_IDCARD -> layoutBinding.keyboardView.toggleMode(MultikeyboardView.KeyboardMode.KEYBOARD_MODEL_IDCARD)
                }
                layoutBinding.keyboardView.setAllowDecimalPoint(isAllowDecimalPoint)
            }
        }
    }

    private var isShowing = false

    fun show(manager: FragmentManager) {
        Log.d(TAG, "show(isAdded=${isAdded}, isShowing=$isShowing)")
        if (this.isAdded || isShowing) return
        isShowing = true
        this.show(manager, this::class.java.name)
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.d(TAG, "onDismiss()")
        isShowing = false
        super.onDismiss(dialog)
    }
}