package danielabbott.personalorganiser.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import danielabbott.personalorganiser.R

class BetterSwitch : AppCompatTextView {

    private var disabledValue = false
    private var activeStateValue = false

    var disabled: Boolean
        get() = disabledValue
        set(value) {
            disabledValue = value
            setColour()
        }

    private var activeState: Boolean
        get() = activeStateValue
        set(value) {
            activeStateValue = value
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
            setTypeface(null, Typeface.NORMAL)
            background!!.colorFilter = PorterDuffColorFilter(0xffc0c0c0.toInt(), PorterDuff.Mode.MULTIPLY)
        } else {
            setBackgroundResource(R.drawable.rounded_corners)
            setPadding(padding, padding, padding, padding)
            if (activeState) {
                setTypeface(null, Typeface.BOLD)
                background!!.colorFilter = PorterDuffColorFilter(0xffc2ffc4.toInt(), PorterDuff.Mode.MULTIPLY)
            } else {
                setTypeface(null, Typeface.NORMAL)
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

        setOnClickListener {
            if (!disabled) {
                activeState = !activeState
            }
        }
    }
}