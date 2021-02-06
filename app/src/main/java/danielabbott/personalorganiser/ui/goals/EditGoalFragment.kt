package danielabbott.personalorganiser.ui.goals

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import danielabbott.personalorganiser.ColourFunctions
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.Notifications
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Goal
import danielabbott.personalorganiser.data.Milestone
import danielabbott.personalorganiser.ui.DataEntryFragment
import danielabbott.personalorganiser.ui.LimitedColourPickerView

class EditGoalFragment(private val goalId: Long?) : DataEntryFragment() {


    private var colour = LimitedColourPickerView.colours[0]

    private var newMilestones = ArrayList<Milestone>()
    private var milestonesToRemove = ArrayList<Milestone>()
    private var milestonesChanged = ArrayList<Milestone>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit_goal, container, false)
        (activity!! as MainActivity).setToolbarTitle("Edit Goal")

        super.init(root)

        val name = root.findViewById<EditText>(R.id.name)
        val notes = root.findViewById<EditText>(R.id.notes)
        val colourChangeButton = root.findViewById<GoalColourPickerButton>(R.id.setColour)
        val recyclerView = root.findViewById<RecyclerView>(R.id.list)
        picturePreviewsView = root.findViewById<LinearLayout>(R.id.PicturePreviews)

        var e: Goal? = null

        // Load data
        if (goalId != null) {
            e = DB.getGoal(goalId)

            colour = e.colour

            notes.setText(e.notes ?: "")
            name.setText(e.name)

        }

        // Milestones list

        // Data
        val milestonesArray = ArrayList<Milestone>()
        if (goalId != null) {
            milestonesArray.addAll(DB.getMilestones(goalId))
        }

        val recyclerViewOriginalHeight = recyclerView.layoutParams.height
        if (milestonesArray.isEmpty()) {
            recyclerView.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
        }

        // Initialise RecyclerView
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)

            adapter = MilestoneRecyclerViewAdapter(milestonesArray,

                // ON CLICK
                {
                    // On single click, show modify dialog
                    MilestoneDialog({ name: String, deadline: Long? ->
                        it.name = name
                        it.deadline = deadline
                        if (it.id >= 0) {
                            milestonesChanged.add(it)
                        }
                        (adapter!! as MilestoneRecyclerViewAdapter).update()
                    }, it.name, it.deadline).show(fragmentManager!!, null)
                },

                // ON LONG CLICK
                {
                    // On long tap, delete (confirm first)
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Delete milestone")
                        .setMessage("Delete milestone '${it.name}'?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Delete") { _, _ ->
                            if (it.id >= 0) {
                                milestonesChanged.remove(it)
                                milestonesToRemove.add(it)
                            } else {
                                newMilestones.remove(it)
                            }
                            if ((adapter as MilestoneRecyclerViewAdapter).remove(it) == 0) {
                                recyclerView.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    0
                                )
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }, context!!)
        }

        if (goalId == null) {
            findUnusedColour()
        }

        colourChangeButton.colour = colour


        // Button for setting colour of goal
        colourChangeButton.setOnClickListener {
            val colourPicker = LimitedColourPickerView(context!!)

            val builder = AlertDialog.Builder(activity!!)
                .setView(colourPicker)
                .show()

            colourPicker.setOnClickListener {
                if (colourPicker.selectedColour != null) {
                    // A colour was picked

                    colour = colourPicker.selectedColour!!
                    colourChangeButton.colour = colour
                }
                builder.dismiss()
            }

        }

        // Save button
        val fab: FloatingActionButton = root.findViewById(R.id.fab_save)
        fab.setOnClickListener { _ ->
            if (name.text.isEmpty()) {
                android.app.AlertDialog.Builder(context)
                    .setTitle("Invalid data")
                    .setMessage("Goal name cannot be blank")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Okay", null)
                    .show()
            } else {
                // Data is valid, update/insert in database

                val e2 = Goal(
                    goalId ?: -1,
                    name.text.toString().trim(),
                    colour,
                    if (notes.text.trim().isEmpty()) null else notes.text.trim().toString()
                )

                val id = DB.updateOrCreateGoal(e2)

                // Add/remove milestones

                newMilestones.forEach {
                    try {
                        it.goal_id = id
                        DB.addMilestone(it)
                    } catch (_: Exception) {
                    }
                }

                milestonesToRemove.forEach {
                    try {
                        DB.removeMilestone(it)
                    } catch (_: Exception) {
                    }
                }

                milestonesChanged.forEach {
                    try {
                        DB.updateMilestone(it)
                    } catch (_: Exception) {
                    }
                }

                // Add/remove pictures

                newPhotos.forEach {
                    try {
                        DB.addGoalPhoto(id, it)
                    } catch (_: Exception) {
                    }
                }

                imagesToRemove.forEach {
                    try {
                        DB.removeGoalPhoto(id, it)
                    } catch (_: Exception) {
                    }
                }

                (activity!! as MainActivity).hideKeyboard()

                exitWithoutUnsavedChangesWarning = true
                (activity as MainActivity).onBackPressed()


            }

        }

        val deleteButton = root.findViewById(R.id.bDelete) as Button
        deleteButton.visibility = if (goalId != null) View.VISIBLE else View.INVISIBLE


        if (goalId != null) {
            deleteButton.setOnClickListener {
                showDeleteDialog(context!!, goalId) {
                    exitWithoutUnsavedChangesWarning = true
                    (activity as MainActivity).onBackPressed()
                }
            }


            // Get images in background
            object : Thread() {
                override fun run() {
                    DB.getGoalPhotos(goalId).forEach {
                        try {
                            addImage(Uri.parse(it))
                        } catch (e: Exception) {
                            DB.removeGoalPhoto(goalId, it)
                        }
                    }
                }
            }.start()
        }

        root.findViewById<Button>(R.id.addMilestone).setOnClickListener {
            MilestoneDialog({ name: String, date: Long? ->
                val m = Milestone(-1, name, date, goalId ?: -1)
                newMilestones.add(m)
                if ((recyclerView.adapter as MilestoneRecyclerViewAdapter).add(m) == 1) {
                    val sv = root.findViewById(R.id.scroll) as NestedScrollView
                    sv.scrollTo(0, sv.bottom)
                }

                recyclerView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    recyclerViewOriginalHeight
                )
            }, null, null).show(fragmentManager!!, null)
        }


        anyUnsavedChanges = { ->
            if (goalId == null) {
                notes.text.toString().trim().isNotEmpty() ||
                        name.text.toString().trim().isNotEmpty() || newPhotos.size > 0
            }
            else if (newMilestones.size > 0) true
            else if (milestonesToRemove.size > 0) true
            else if (milestonesChanged.size > 0) true
            else if (newPhotos.size > 0) true
            else if (imagesToRemove.size > 0) true
            else if (e!!.name.trim() != name.text.toString().trim()) true
            else if (e.colour != colour) true
            else if ((e.notes == null) != notes.text.toString().trim().isEmpty()) true
            else if (e.notes != null && e.notes?.trim() != notes.text.toString().trim()) true
            else false
        }

        return root
    }

    companion object {

        private fun doDelete(
            deleteEvents: Boolean,
            goalId: Long,
            context: Context,
            onDelete: () -> Unit
        ) {
            DB.deleteGoal(goalId, deleteEvents)
            if (deleteEvents) {
                Notifications.scheduleAllNotifications(context.applicationContext)
            }
            onDelete()
        }

        fun showDeleteDialog(
            context: Context,
            goalId: Long,
            onDelete: () -> Unit
        ) {
            android.app.AlertDialog.Builder(context)
                .setTitle("Delete goal")
                .setMessage("Are you sure you want to delete this goal? This cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete") { _, _ ->
                    if (DB.goalHasAssociatedEventsOrTasks(goalId)) {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Delete event")
                            .setMessage(
                                "Do you want to delete any associated timetable events or " +
                                        "To Do list tasks?"
                            )
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Delete events/tasks") { _, _ ->
                                doDelete(true, goalId, context, onDelete)
                            }
                            .setNegativeButton("Keep events/tasks") { _, _ ->
                                doDelete(false, goalId, context, onDelete)
                            }
                            .show()
                    } else {
                        doDelete(true, goalId, context, onDelete)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun findUnusedColour() {
        val colours = DB.allUsedGoalColours()


        var i: Int = 0
        while (i < LimitedColourPickerView.colours.count() * 3) {
            val c = LimitedColourPickerView.colours[i % LimitedColourPickerView.colours.count()]
            if (!colours.contains(c)) {
                colour = c
                return
            }
            i += 3
        }
    }
}