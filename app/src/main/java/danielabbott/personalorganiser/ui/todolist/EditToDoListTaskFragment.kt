package danielabbott.personalorganiser.ui.todolist

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import danielabbott.personalorganiser.DateTimeUtil
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.Notifications
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Repeat
import danielabbott.personalorganiser.data.ToDoListTask
import danielabbott.personalorganiser.ui.*

class EditToDoListTaskFragment(val taskId: Long?) : DataEntryFragment() {

    private lateinit var dateForwards: Button
    private lateinit var dateBack: Button
    private lateinit var name: EditText
    private lateinit var notes: EditText
    private lateinit var time: TimeSelectView
    private lateinit var date: DateSelectView
    private lateinit var repeat: RepeatSelector
    private lateinit var rOnTime: BetterSwitch
    private lateinit var r30: BetterSwitch
    private lateinit var r1: BetterSwitch
    private lateinit var r2: BetterSwitch
    private lateinit var rMorn: BetterSwitch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DB.init(context!!)
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_to_do_list_task, container, false)
        (activity!! as MainActivity).setToolbarTitle("Edit Task")
        setHasOptionsMenu(true)

        notes = root.findViewById(R.id.notes)
        name = root.findViewById(R.id.name)
        repeat = root.findViewById(R.id.repeat)
        rOnTime = root.findViewById(R.id.rOnTime)
        r30 = root.findViewById(R.id.r30)
        r1 = root.findViewById(R.id.r1)
        r2 = root.findViewById(R.id.r2)
        rMorn = root.findViewById(R.id.rMorn)
        time = root.findViewById(R.id.time)
        date = root.findViewById(R.id.date)
        dateForwards = root.findViewById(R.id.dateForwards)
        dateBack = root.findViewById(R.id.dateBack)
        val goal = root.findViewById<GoalSelector>(R.id.goal)

        super.init(root)


        var originalTaskData: ToDoListTask? = null

        if (taskId != null) {
            // Load the task from the database

            try {
                originalTaskData = DB.getToDoListTask(taskId)
            } catch (e: Exception) {
                val fragmentTransaction = fragmentManager!!.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentView, ToDoListFragment())
                fragmentTransaction.commit()
                return root
            }

            notes.setText(if (originalTaskData.notes == null) "" else originalTaskData.notes!!)
            name.setText(originalTaskData.name)
            repeat.repeat = originalTaskData.repeat
            rOnTime.isChecked = originalTaskData.remindOnTime
            r30.isChecked = originalTaskData.remind30Mins
            r1.isChecked = originalTaskData.remind1Hr
            r2.isChecked = originalTaskData.remind2Hrs
            rMorn.isChecked = originalTaskData.remindMorning
            goal.setGoal(originalTaskData.goal_id)

            if (originalTaskData.repeat != Repeat.NEVER && originalTaskData.dateTime != null) {
                dateForwards.visibility = View.VISIBLE
                dateBack.visibility = View.VISIBLE
            } else {
                dateForwards.visibility = View.INVISIBLE
                dateBack.visibility = View.INVISIBLE
            }

            if (originalTaskData.dateTime != null) {
                originalTaskData.dateTime =
                    originalTaskData.dateTime!! - originalTaskData.dateTime!! % 1000
                if (originalTaskData.hasTime) {
                    val t = DateTimeUtil.getHoursAndMinutes(originalTaskData.dateTime!!)
                    time.setTime(t.first, t.second)
                }
                val d = DateTimeUtil.getYearMonthDay(originalTaskData.dateTime!!)
                date.setDate(d.first, d.second, d.third)
            }
        } else {
            dateForwards.visibility = View.INVISIBLE
            dateBack.visibility = View.INVISIBLE
        }

        picturePreviewsView = root.findViewById(R.id.PicturePreviews)

        // Save button
        val fab: FloatingActionButton = root.findViewById(R.id.fab_save)
        fab.setOnClickListener { _ ->
            val nameString = name.text.toString()

            if (nameString.isEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Invalid data")
                    .setMessage("Task name cannot be blank")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Okay", null)
                    .show()
            } else {
                // Data is valid, update/insert in database

                val notes = notes.text

                if (!date.dateSelected && time.timeSelected) {
                    date.setDate(System.currentTimeMillis())
                }

                var dateTime = if (date.dateSelected) DateTimeUtil.getDateTimeMillis(
                    date.year,
                    date.month,
                    date.day,
                    if (time.timeSelected) time.hour else 23,
                    if (time.timeSelected) time.minute else 59
                )
                else null

                if (dateTime != null) {
                    dateTime -= dateTime % 1000
                }

                val newTask = ToDoListTask(
                    taskId ?: -1,
                    dateTime,
                    time.timeSelected,
                    nameString,
                    if (notes.isEmpty()) null else notes.toString(),
                    rOnTime.isChecked,
                    r30.isChecked,
                    r1.isChecked,
                    r2.isChecked,
                    rMorn.isChecked,
                    repeat.repeat,
                    goal.getSelectedGoalID()
                )

                val eventId = DB.updateOrCreateToDoListTask(newTask)
                newTask.id = eventId

                // Add/remove pictures

                newPhotos.forEach {
                    try {
                        DB.addToDoListTaskPhoto(eventId, it)
                    } catch (_: Exception) {
                    }
                }

                imagesToRemove.forEach {
                    try {
                        DB.removeToDoListTaskPhoto(eventId, it)
                    } catch (_: Exception) {
                    }
                }

                // Reschedule all notifications

                var needToRescheduleNotifications = true

                if (!newTask.remindOnTime && !newTask.remind30Mins && !newTask.remind1Hr && !newTask.remind2Hrs && !newTask.remindMorning) {
                    if (taskId == null || (!originalTaskData!!.remindOnTime && !originalTaskData.remind30Mins && !originalTaskData.remind1Hr && !originalTaskData.remind2Hrs && !originalTaskData.remindMorning)) {
                        needToRescheduleNotifications = false
                    }
                }

                if (needToRescheduleNotifications) {
                    Notifications.scheduleForTask(context!!, newTask, taskId == null)
                }

                (activity!! as MainActivity).hideKeyboard()

                exitWithoutUnsavedChangesWarning = true
                (activity as MainActivity).onBackPressed()
            }

        }

        val deleteButton = root.findViewById(R.id.bDelete) as Button
        deleteButton.visibility = if (taskId != null) View.VISIBLE else View.GONE
        if (taskId != null) {
            deleteButton.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete task")
                    .setMessage("Are you sure you want to delete this task? This cannot be undone.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete") { _, _ ->
                        DB.deleteToDoListTask(taskId)
                        Notifications.unscheduleNotificationsForTask(context!!, taskId)
                        exitWithoutUnsavedChangesWarning = true
                        (activity!! as MainActivity).hideKeyboard()
                        (activity as MainActivity).onBackPressed()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Get images in background
            object : Thread() {
                override fun run() {
                    DB.getToDoListTaskPhotos(taskId).forEach {
                        try {
                            addImage(Uri.parse(it))
                        } catch (e: Exception) {
                            DB.removeToDoListTaskPhoto(taskId, it)
                        }
                    }
                }
            }.start()
        }

        date.onDateChange = { _, _, _ ->
            setDateChangeButtonsVisibility()
            setNotificationCheckboxesEnabledState()
        }

        time.onTimeChanged = { _: Int, _: Int ->
            if (!date.dateSelected) {
                date.setDate(System.currentTimeMillis())
            }
            setNotificationCheckboxesEnabledState()
        }

        time.onTimeCleared = {
            setNotificationCheckboxesEnabledState()
        }

        date.onDateCleared = {
            setNotificationCheckboxesEnabledState()
        }

        setNotificationCheckboxesEnabledState()


        dateForwards.setOnClickListener {
            val rep = repeat.repeat
            if (date.dateSelected && rep != Repeat.NEVER) {
                val ymd: Triple<Int, Int, Int>

                if (rep == Repeat.MONTHLY) {
                    var m = date.month + 1 // Add month
                    var yr = date.year

                    // If gone past december, go to january of next year
                    if (m > 12) {
                        m = 1
                        yr++
                    }

                    val ms = DateTimeUtil.getDateTimeMillis(yr, m, date.day, 6, 0)
                    ymd = DateTimeUtil.getYearMonthDay(ms)
                } else {
                    var m = DateTimeUtil.getDateTimeMillis(date.year, date.month, date.day, 6, 0)
                    m += 24 * 60 * 60 * 1000 * rep.days()
                    ymd = DateTimeUtil.getYearMonthDay(m)
                }
                date.setDate(ymd.first, ymd.second, ymd.third)
            }
        }

        dateBack.setOnClickListener {
            val rep = repeat.repeat
            if (date.dateSelected && rep != Repeat.NEVER) {
                val ymd: Triple<Int, Int, Int>

                if (rep == Repeat.MONTHLY) {
                    var m = date.month - 1 // Add month
                    var yr = date.year

                    // If gone before january, go to december of previous year
                    if (m < 1) {
                        m = 12
                        yr--
                    }

                    val ms = DateTimeUtil.getDateTimeMillis(yr, m, date.day, 6, 0)
                    ymd = DateTimeUtil.getYearMonthDay(ms)
                } else {
                    var m = DateTimeUtil.getDateTimeMillis(date.year, date.month, date.day, 6, 0)
                    m -= 24 * 60 * 60 * 1000 * rep.days()
                    ymd = DateTimeUtil.getYearMonthDay(m)
                }
                date.setDate(ymd.first, ymd.second, ymd.third)
            }
        }

        date.setOnLongClickListener {
            dateForwards.visibility = View.INVISIBLE
            dateBack.visibility = View.INVISIBLE
            time.reset()
            true
        }



        name.addTextChangedListener {
            updateShareActionProvider()
        }
        notes.addTextChangedListener {
            updateShareActionProvider()
        }

        repeat.onItemSelectedListener =
            {
                setDateChangeButtonsVisibility()
                setNotificationCheckboxesEnabledState()
            }

        anyUnsavedChanges = {
            val notes = notes.text.toString().trim()

            var dateTime = if (date.dateSelected) DateTimeUtil.getDateTimeMillis(
                date.year,
                date.month,
                date.day,
                if (time.timeSelected) time.hour else 23,
                if (time.timeSelected) time.minute else 59
            )
            else null

            if (dateTime != null) {
                dateTime -= dateTime % 1000
            }

            val newGoal = goal.getSelectedGoalID()

            if (taskId == null) {
                notes.isNotEmpty() || name.text.toString().trim()
                    .isNotEmpty() || date.dateSelected || newPhotos.size > 0
            } else if (newPhotos.size > 0) true
            else if (imagesToRemove.size > 0) true
            else if (originalTaskData!!.dateTime != dateTime) true
            else if (originalTaskData.hasTime != time.timeSelected) true
            else if (originalTaskData.name.trim() != name.text.toString().trim()) true
            else if ((originalTaskData.notes == null) != notes.isEmpty()) true
            else if (originalTaskData.notes != null && originalTaskData.notes?.trim() != notes
                    .trim()
            ) true
            else if (originalTaskData.remindOnTime != rOnTime.isChecked) true
            else if (originalTaskData.remind30Mins != r30.isChecked) true
            else if (originalTaskData.remind1Hr != r1.isChecked) true
            else if (originalTaskData.remind2Hrs != r2.isChecked) true
            else if (originalTaskData.remindMorning != rMorn.isChecked) true
            else if (originalTaskData.repeat != repeat.repeat) true
            else originalTaskData.goal_id != newGoal
        }

        return root
    }

    private fun setNotificationCheckboxesEnabledState() {
        val rep = repeat.repeat
        if (time.timeSelected) {
            rOnTime.disabled = false
            r30.disabled = false
            r1.disabled = false
            r2.disabled = false
        } else {
            rOnTime.disabled = true
            r30.disabled = true
            r1.disabled = true
            r2.disabled = true
            rOnTime.isChecked = false
            r30.isChecked = false
            r1.isChecked = false
            r2.isChecked = false
        }

        if (date.dateSelected || rep == Repeat.DAILY) {
            rMorn.disabled = false
        } else {
            rMorn.disabled = true
            rMorn.isChecked = false
        }
    }

    private fun setDateChangeButtonsVisibility() {
        val rep = repeat.repeat
        if (rep == Repeat.NEVER || !date.dateSelected) {
            dateForwards.visibility = View.INVISIBLE
            dateBack.visibility = View.INVISIBLE
        } else {
            dateForwards.visibility = View.VISIBLE
            dateBack.visibility = View.VISIBLE
        }
    }


    private lateinit var shareMenuItem: MenuItem
    private lateinit var shareIntent: Intent

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()

        inflater.inflate(R.menu.share_action_menu_item, menu)
        shareMenuItem = menu.findItem(R.id.action_share)
        val sap = MenuItemCompat.getActionProvider(shareMenuItem) as ShareActionProvider

        shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        updateShareActionProvider()
        sap.setShareIntent(shareIntent)

        activity!!.onCreateOptionsMenu(menu)
    }

    private fun updateShareActionProvider() {
        var t = name.text.toString() + '\n'
        if (date.dateSelected || time.timeSelected) {
            if (date.dateSelected) {
                t += DateTimeUtil.getDateString(context!!, date.getDate()!!) + ' '
            }
            if (time.timeSelected) {
                t += DateTimeUtil.getTimeString(time.getTime()!!)
            }
            t += '\n'
        }
        t += notes.text.toString()

        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            t
        )
    }

}
