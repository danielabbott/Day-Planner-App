package danielabbott.personalorganiser.ui.goals

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.GoalListData
import danielabbott.personalorganiser.ui.CircleView
import danielabbott.personalorganiser.ui.LimitedColourPickerView


class GoalRecyclerViewAdapter(
    private val mValues: List<GoalListData>,
    private val parentFragmentManager: FragmentManager,
    private val activity: Activity
) : RecyclerView.Adapter<GoalRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == mValues.size) {
            // Extra space at bottom (empty View)
            holder.circle.visibility = View.INVISIBLE
            holder.clickableArea.visibility = View.INVISIBLE
            holder.name.text = ""
            holder.clickableArea.setOnClickListener(null)
            holder.clickableArea.setOnLongClickListener(null)
            holder.circle.setOnClickListener(null)
            holder.dots?.visibility = View.INVISIBLE
            holder.camera?.visibility = View.INVISIBLE
            return
        }

        val item = mValues[position]
        holder.name.text = item.name
        holder.circle.colour = item.colour
        holder.mView.tag = item.id
        holder.dots?.visibility = if (item.hasNotes) View.VISIBLE else View.INVISIBLE
        holder.camera?.visibility = if (item.hasImages) View.VISIBLE else View.INVISIBLE

        holder.clickableArea.setOnClickListener {
            // When clicked, open edit page for the goal
            val fragment = EditGoalFragment(item.id)
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentView, fragment).addToBackStack(null)
            fragmentTransaction.commit()
        }

        holder.clickableArea.setOnLongClickListener {
            EditGoalFragment.showDeleteDialog(activity, item.id) {
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentView, GoalsFragment())
                fragmentTransaction.commit()
            }
            true
        }

        holder.circle.setOnClickListener {
            val colourPicker = LimitedColourPickerView(activity)
            val builder = AlertDialog.Builder(activity)
                .setView(colourPicker)
                .show()

            colourPicker.setOnClickListener {
                if (colourPicker.selectedColour != null) {
                    // A colour was picked

                    DB.changeGoalColour(item.id, colourPicker.selectedColour!!)
                    holder.circle.colour = colourPicker.selectedColour!!
                }
                builder.dismiss()
            }
        }
    }

    override fun getItemCount(): Int = mValues.size + 1

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val clickableArea: LinearLayout = mView.findViewById(R.id.goalClickable)
        val name: TextView = mView.findViewById(R.id.name)
        val circle: CircleView = mView.findViewById(R.id.circle)
        val dots: TextView? = mView.findViewById(R.id.dots)
        val camera: ImageView? = mView.findViewById(R.id.camera)
    }
}
