package danielabbott.personalorganiser.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import danielabbott.personalorganiser.data.Repeat

class RepeatSelector : AppCompatButton {


    private var repeatValue = Repeat.NEVER

    var repeat: Repeat
        get() = repeatValue
        set(value) {
            repeatValue = value
            text = repeatValue.toString()
        }

    var onItemSelectedListener: ((Repeat) -> Unit)? = null


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
        setTypeface(null, Typeface.NORMAL)
        repeat = Repeat.NEVER

        setOnLongClickListener {
            repeat = Repeat.NEVER
            true
        }

        setOnClickListener {
            val repeatStrings = ArrayList<CharSequence>()
            Repeat.values().forEach {
                repeatStrings.add(it.toString())
            }

            AlertDialog.Builder(context)
                .setTitle("Select Repeat Mode")
                .setItems(
                    repeatStrings.toTypedArray()
                ) { _, which ->
                    repeat = Repeat.values()[which]
                    if (onItemSelectedListener != null) {
                        onItemSelectedListener!!(repeat)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}