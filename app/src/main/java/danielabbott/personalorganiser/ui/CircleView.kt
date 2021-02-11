package danielabbott.personalorganiser.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Just a coloured circle
class CircleView : View {

    private var colourValue: Int = 0xffff0000.toInt()

    var colour: Int
        get() = colourValue
        set(value) {
            colourValue = value
            invalidate()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private fun min(a: Int, b: Int): Int {
        return if (a < b) a else b
    }

    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.color = colour or 0xff000000.toInt()

        val x = paddingLeft
        val y = paddingTop
        val w = width - paddingRight
        val h = height - paddingBottom
        canvas.drawCircle(x + w * 0.5f, y + h * 0.5f, min(w, h) * 0.5f, paint)

    }
}
