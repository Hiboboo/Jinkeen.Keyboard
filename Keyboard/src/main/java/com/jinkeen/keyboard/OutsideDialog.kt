package com.jinkeen.keyboard

import android.app.Dialog
import android.content.Context
import android.view.MotionEvent
import androidx.annotation.NonNull
import androidx.annotation.StyleRes

internal class OutsideDialog(@NonNull context: Context, @StyleRes themeResId: Int) : Dialog(context, themeResId) {

    constructor(@NonNull context: Context) : this(context, 0)

    companion object {
        private const val TAG = "OutsideDialog"
    }

    private var block: (event: MotionEvent) -> Unit = {}

    internal fun setOutsideClickListener(block: (event: MotionEvent) -> Unit) {
        this.block = block
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            block.invoke(event)
            return true
        }
        return super.onTouchEvent(event)
    }
}