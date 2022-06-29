package com.jinkeen.keyboard

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Rect
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.util.ArrayMap
import android.util.DisplayMetrics
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class SoftInputDialog private constructor() : DialogFragment(R.layout.si_dialog_keyboard_layout) {

    companion object {

        private const val TAG = "SoftInputDialog"

        private val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SoftInputDialog() }

        operator fun invoke(): SoftInputDialog = instance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = false
        this.setStyle(STYLE_NORMAL, R.style.SoftInputBottomDialogStyle)
    }

    private val sChildViewsCoordinates = ArrayMap<Int, Rect>()

    fun replaceChildViewCoordinate(key: Int, rect: Rect) {
        sChildViewsCoordinates[key] = rect
    }

    fun removeChildViewCoordinate(key: Int) {
        if (sChildViewsCoordinates.containsKey(key)) sChildViewsCoordinates.remove(key)
    }

    private val sAppWindowRect by lazy { Rect().apply { dialog?.window?.decorView?.getWindowVisibleDisplayFrame(this) } }
    private val outMetrics by lazy { DisplayMetrics().apply { dialog?.window?.windowManager?.defaultDisplay?.getRealMetrics(this) } }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return OutsideDialog(requireContext(), theme).apply {
            Log.d(TAG, "onCreateDialog($this)")
            setOutsideClickListener { event ->
                Log.d(TAG, "screenHeight=${outMetrics.heightPixels}, AppRect($sAppWindowRect), ChildCoordinates($sChildViewsCoordinates)")
                val mDialogHeight = layoutBinding.keyboardViewContainer.height
                val mNavigationBarHeight = outMetrics.heightPixels - sAppWindowRect.bottom
                val sClickCoordinateX = abs(event.x).toInt()
                val sClickCoordinateY = (outMetrics.heightPixels - mNavigationBarHeight - mDialogHeight - abs(event.y)).toInt()
                Log.d(TAG, "点击的实际坐标点(x=$sClickCoordinateX, y=$sClickCoordinateY)")
                var isClickedEditView = false
                sChildViewsCoordinates.values.forEach { rect ->
                    Log.d(TAG, "click rect=$rect")
                    if (isClickedEditView) return@forEach
                    isClickedEditView = (max(rect.left, sClickCoordinateX) == min(sClickCoordinateX, rect.right)) &&
                            max(rect.top, sClickCoordinateY) == min(sClickCoordinateY, rect.bottom)
                }
                if (!isClickedEditView) this@SoftInputDialog.dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(KeyboardStyle.KEYBOARD_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT)
            it.attributes.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            it.setGravity(Gravity.BOTTOM)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause(isShowing=${isShowing()})")
        if (isShowing()) this.dismiss()
    }

    private lateinit var layoutBinding: SiDialogKeyboardLayoutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = super.onCreateView(inflater, container, savedInstanceState)
        contentView?.let { layoutBinding = DataBindingUtil.bind(it)!! }
        Log.d(TAG, "onCreateView(contentView=$contentView)")
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

    // DialogFragment的show()方法在调用之后，本页面中的Views才被创建并初始化
    // 因此，为了确保KeyboardView能够准确显示，因此这里需要一个初始化状态的监听开关
    private val isViewCreated = AtomicBoolean(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated(keyboardView=${layoutBinding.keyboardView})")
        val widthPixels = resources.displayMetrics.widthPixels
        Log.d(TAG, "onViewCreated(widthPixels=$widthPixels)")
        val isHideLRShadow = (KeyboardStyle.KEYBOARD_WIDTH >= widthPixels || KeyboardStyle.KEYBOARD_WIDTH == WindowManager.LayoutParams.MATCH_PARENT)
        layoutBinding.keyboardViewContainer.setShadowHiddenLeft(isHideLRShadow)
        layoutBinding.keyboardViewContainer.setShadowHiddenRight(isHideLRShadow)
        isViewCreated.set(true)
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
                    // 透明占位键与完成键重叠
                    KEYBOARD_CODE_DONE, KEYBOARD_CODE_EMPTY -> dismiss()
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
        if (!isViewCreated.get()) return

        fun isLetter(s: String): Boolean = s.matches(Regex("[A-Za-z]"))

        val isShifted = layoutBinding.keyboardView.keyboard.isShifted
        if (isShifted) // 大写切小写
            layoutBinding.keyboardView.keyboard.keys.forEach { key ->
                if (key.codes[0] == shiftCode) key.label = resources.getString(R.string.si_label_switch_uppercase)
                key.label?.let { label ->
                    if (isLetter(label.toString())) {
                        key.label = label.toString().lowercase()
                        key.codes[0] = key.codes[0] + 32
                    }
                }
            }
        else // 小写切大写
            layoutBinding.keyboardView.keyboard.keys.forEach { key ->
                if (key.codes[0] == shiftCode) key.label = resources.getString(R.string.si_label_switch_lowercase)
                key.label?.let { label ->
                    if (isLetter(label.toString())) {
                        key.label = label.toString().uppercase()
                        key.codes[0] = key.codes[0] - 32
                    }
                }
            }
        layoutBinding.keyboardView.keyboard.isShifted = !isShifted
        layoutBinding.keyboardView.invalidateAllKeys()
    }

    fun toggleKeyboard(boardType: Int, isAllowDecimalPoint: Boolean = true) {
        CoroutineScope(Dispatchers.IO).launch {
            var b = false
            while (!isViewCreated.get()) {
                if (!b) {
                    Log.d(TAG, "等待KeyboardView初始化")
                    b = true
                }
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "boardType=$boardType, keyboardview=${layoutBinding.keyboardView}")
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

    fun isShowing(): Boolean = isShowing

    fun show(manager: FragmentManager) {
        Log.d(TAG, "show(isAdded=${isAdded}, isShowing=$isShowing)")
        if (this.isAdded || isShowing) return
        isShowing = true
        isViewCreated.set(false) // 每一次重新显示，也就意味着布局可能被重新刷新一次
        this.show(manager, this::class.java.name)
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.d(TAG, "onDismiss()")
        isShowing = false
        isViewCreated.set(false)
        super.onDismiss(dialog)
    }
}