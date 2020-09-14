package danielabbott.personalorganiser.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.math.MathUtils
import danielabbott.personalorganiser.data.Settings

class MenuSliderView : View {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    var menuView: View? = null
    var menuContainerView: View? = null
    var menuBlackView: View? = null

    // These might be called while the menu is half in/out
    var showMenu: (() -> Unit)? = null
    var hideMenu: (() -> Unit)? = null

    private var beingDragged = false
    private var direction = 0.0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || menuContainerView == null || menuBlackView == null || menuView == null || showMenu == null || hideMenu == null) {
            return false
        }

        if (beingDragged) {
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                beingDragged = false;

                val t = 1.0f - (-menuContainerView!!.translationX / menuView!!.layoutParams.width)
                if (t < 0.3f || direction < 0.0f) {
                    hideMenu!!()
                }
                else {
                    showMenu!!()
                }
            } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                direction = event.x

                menuContainerView!!.translationX += event.x
                menuContainerView!!.translationX = MathUtils.clamp(
                    menuContainerView!!.translationX,
                    -menuView!!.layoutParams.width.toFloat().toFloat(),
                    0.0f
                )
                val t = 1.0f - (-menuContainerView!!.translationX / menuView!!.layoutParams.width)
                menuBlackView!!.alpha = t * 0.7f

                if (t >= 1.0f) {
                    // Fully open
                    showMenu!!()
                }
            }
        } else {
            if (Settings.getMenuSwipeOpenEnabled(context) && event.actionMasked == MotionEvent.ACTION_DOWN) {
                beingDragged = true;
            }
            else {
                return false
            }
        }
        return true
    }

    fun userIsDraggingMenu(): Boolean = beingDragged
}