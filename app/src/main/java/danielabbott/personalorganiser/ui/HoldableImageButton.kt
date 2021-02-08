package danielabbott.personalorganiser.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton

class HoldableImageButton : AppCompatImageButton {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private lateinit var callbackRunner: Runnable
    private var mHandler: Handler? = null
    private var fingerDown = false
    private var delay = 500L

    private fun init() {
        mHandler = Handler(Looper.getMainLooper())
        callbackRunner = Runnable {
            if (fingerDown) {
                super.callOnClick()
                mHandler!!.postDelayed(callbackRunner, delay)
                delay = 100
            }
        }
    }

    override fun performClick(): Boolean {
        fingerDown = true
        delay = 500L
        callbackRunner.run()
        return super.performClick()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
        } else if (event.action == MotionEvent.ACTION_UP) {
            fingerDown = false
        }
        return true
    }

}