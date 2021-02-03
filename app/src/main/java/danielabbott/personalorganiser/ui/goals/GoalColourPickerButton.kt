package danielabbott.personalorganiser.ui.goals

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import danielabbott.personalorganiser.ColourFunctions
import danielabbott.personalorganiser.R

class GoalColourPickerButton : AppCompatButton {

    private var colour_: Int = 0xffff0000.toInt()

    var colour: Int
        get() = colour_
        set(value) {
            colour_ = ColourFunctions.lightenRGB(value) or 0xff000000.toInt()
            setColour()
        }


    private fun setColour() {
        setBackgroundResource(R.drawable.rounded_rectangle_button)
        background!!.colorFilter = PorterDuffColorFilter(colour, PorterDuff.Mode.MULTIPLY)
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

}