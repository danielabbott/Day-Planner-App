package danielabbott.personalorganiser.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import danielabbott.personalorganiser.ColourFunctions
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Goal
import danielabbott.personalorganiser.data.GoalListData

class GoalSelector : AppCompatButton {


    private var selectedGoal: Goal? = null

    fun getSelectedGoalID() : Long? {
        return selectedGoal?.id
    }

    fun setGoal(goalID_: Long?) {
        if(goalID_ == null) {
            selectedGoal = null
        }
        else {
            selectedGoal = DB.getGoalForSelector(goalID_)
        }
        setColourAndText()
    }

    fun setGoal(goal: GoalListData) {
        selectedGoal = Goal(goal.id, goal.name, goal.colour, null)
        setColourAndText()
    }

    private fun setColourAndText() {
        setBackgroundResource(R.drawable.rounded_rectangle_button)
        if(selectedGoal == null) {
            background!!.colorFilter = null
            text = "[No Goal Selected]"
        }
        else {
            val c = ColourFunctions.lightenRGB(selectedGoal!!.colour) or 0xff000000.toInt()
            background!!.colorFilter = PorterDuffColorFilter(c, PorterDuff.Mode.MULTIPLY)
            text = selectedGoal!!.name
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
        setGoal(null)

        setOnLongClickListener {
            setGoal(null)
            true
        }

        setOnClickListener {
            val goalNames = ArrayList<CharSequence>()
            val goals = DB.getGoals()
            goals.forEach {
                goalNames.add(it.name)
            }

            AlertDialog.Builder(context)
                .setTitle("Select Goal")
                .setItems(
                    goalNames.toTypedArray()
                ) { _, which ->
                    setGoal(goals[which])
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}