package danielabbott.personalorganiser.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import danielabbott.personalorganiser.R

class BetterSwitch : AppCompatButton {

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

    private fun setColour() {
        if(disabled) {
            ViewCompat.setBackgroundTintList(this, AppCompatResources.getColorStateList(context!!, R.color.disabled))
        }
        else {
            if(activeState) {
                ViewCompat.setBackgroundTintList(
                    this,
                    AppCompatResources.getColorStateList(context!!, R.color.activestate)
                )
            }else {
                ViewCompat.setBackgroundTintList(
                    this,
                    AppCompatResources.getColorStateList(context!!, R.color.inactivestate)
                )
            }
        }
        invalidate() // TODO: Needed?
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

        Log.e("a", "uwu")
        setOnClickListener {
            Log.e("a", "122333")
            if(!disabled) {
                Log.e("a", "12233wete4tberte3")
                activeState = !activeState
            }
        }
    }

}