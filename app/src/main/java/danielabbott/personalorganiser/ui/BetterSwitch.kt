package danielabbott.personalorganiser.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import danielabbott.personalorganiser.R

class BetterSwitch : AppCompatTextView {

    private var disabled_ = false
    private var activeState_ = false

    var disabled: Boolean
        get() = disabled_
        set(value) {
            disabled_ = value
            setColour()
        }

    var activeState: Boolean
        get() = activeState_
        set(value) {
            activeState_ = value
            setColour()
        }


    var isChecked: Boolean
        get() = activeState
        set(value) {
            activeState = value
        }


    private fun setColour() {
        val padding = 10
        if (disabled) {
            setBackgroundResource(R.drawable.rounded_corners_no_shadow)
            setPadding(padding, padding, padding, padding)
            background!!.colorFilter = PorterDuffColorFilter(0xffc0c0c0.toInt(), PorterDuff.Mode.MULTIPLY)
        } else {
            setBackgroundResource(R.drawable.rounded_corners)
            setPadding(padding, padding, padding, padding)
            if (activeState) {
                background!!.colorFilter = PorterDuffColorFilter(0xffc2ffc4.toInt(), PorterDuff.Mode.MULTIPLY)

            } else {
                background!!.colorFilter = null
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }


    private fun init() {
        setColour()
    }

    override fun performClick(): Boolean {
        if (!disabled) {
            activeState = !activeState
        }
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_UP) {
            performClick()
            return true
        }

        return true
    }

}