package danielabbott.personalorganiser.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import danielabbott.personalorganiser.BuildConfig
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.Notifications
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Settings
import danielabbott.personalorganiser.ui.timers.TimersFragment


class SettingsFragment : Fragment() {

    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private lateinit var timetableFontSize: Spinner
    private lateinit var timetableLineWidth: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        root.findViewById<TextView>(R.id.appVersion).text =
            "App version ${BuildConfig.VERSION_NAME}"

        val morningTime = root.findViewById<TimeSelectView>(R.id.morningTime)
        val time = Settings.getMorningReminderTime(context!!)
        morningTime.setTime(time / 60, time % 60)

        morningTime.onTimeChanged = { h, m ->
            Settings.setMorningTime(context!!, h * 60 + m)
            Notifications.scheduleAllNotifications(context!!)
        }

        timetableFontSize = root.findViewById(R.id.timetableFontSize)
        timetableFontSize.adapter = ArrayAdapter(
            context!!,
            R.layout.spinner_style,
            arrayOf("16", "18", "20", "22", "24")
        )


        timetableFontSize.onItemSelectedListener = SpinnerChangeDetector {
            Settings.setTimetableFontSize(
                context!!,
                (timetableFontSize.selectedItem!! as String).toInt()
            )
        }

        val ttFontSz = Settings.getTimetableFontSize(context!!)
        if (ttFontSz <= 16) {
            timetableFontSize.setSelection(0)
        } else if (ttFontSz <= 18) {
            timetableFontSize.setSelection(1)
        } else if (ttFontSz <= 20) {
            timetableFontSize.setSelection(2)
        } else if (ttFontSz <= 22) {
            timetableFontSize.setSelection(3)
        } else {
            timetableFontSize.setSelection(4)
        }

        timetableLineWidth = root.findViewById(R.id.timetableLineWidth)
        timetableLineWidth.adapter = ArrayAdapter(
            context!!,
            R.layout.spinner_style,
            arrayOf("0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0")
        )

        timetableLineWidth.onItemSelectedListener = SpinnerChangeDetector {
            Settings.setTimetableLineWidth(
                context!!,
                (timetableLineWidth.selectedItem!! as String).toFloat()
            )
        }

        val ttLineWidth = Settings.getTimetableLineWidth(context!!)
        if (ttLineWidth <= 0.5f) {
            timetableLineWidth.setSelection(0)
        } else if (ttLineWidth <= 0.75f) {
            timetableLineWidth.setSelection(1)
        } else if (ttLineWidth <= 1.0f) {
            timetableLineWidth.setSelection(2)
        } else if (ttLineWidth <= 1.25f) {
            timetableLineWidth.setSelection(3)
        } else if (ttLineWidth <= 1.5f) {
            timetableLineWidth.setSelection(4)
        } else if (ttLineWidth <= 1.75f) {
            timetableLineWidth.setSelection(5)
        } else {
            timetableLineWidth.setSelection(6)
        }

        startTime = root.findViewById(R.id.startTime)
        setTimetableStartTime(null)

        root.findViewById<Button>(R.id.startTimeBack).setOnClickListener {
            val hour = Settings.getTimetableStartHour(context!!) - 1
            if (hour in 0..23) {
                setTimetableStartTime(hour)
            }
        }
        root.findViewById<Button>(R.id.startTimeForwards).setOnClickListener {
            val hour = Settings.getTimetableStartHour(context!!) + 1
            if (hour in 0..23 && Settings.getTimetableEndHour(context!!) - hour >= 10) {
                setTimetableStartTime(hour)
            }
        }

        endTime = root.findViewById(R.id.endTime)
        setTimetableEndTime(null)

        root.findViewById<Button>(R.id.endTimeBack).setOnClickListener {
            val hour = Settings.getTimetableEndHour(context!!) - 1
            if (hour in 1..24 && hour - Settings.getTimetableStartHour(context!!) >= 10) {
                setTimetableEndTime(hour)
            }
        }
        root.findViewById<Button>(R.id.endTimeForwards).setOnClickListener {
            val hour = Settings.getTimetableEndHour(context!!) + 1
            if (hour in 1..24) {
                setTimetableEndTime(hour)
            }
        }

        root.findViewById<Button>(R.id.optimiseDB).setOnClickListener {
            try {
                DB.optimise(context!!)
                Toast.makeText(context!!, "Database optimised", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context!!, "Error optimising database", Toast.LENGTH_SHORT).show()
            }
        }

        val enableAlarms = root.findViewById<SwitchCompat>(R.id.enableAlarms)
        val enableAlarmVibration = root.findViewById<SwitchCompat>(R.id.enableAlarmVibration)

        enableAlarms.setOnCheckedChangeListener { _, checked: Boolean ->
            Settings.setAlarmsEnabled(context!!, checked)
            if (!checked) {
                TimersFragment.clearPendingAlarms(context!!)
            }
            enableAlarmVibration.isEnabled = checked
        }
        enableAlarms.isChecked = Settings.getAlarmsEnabled(context!!)
        enableAlarmVibration.isEnabled = enableAlarms.isChecked

        enableAlarmVibration.setOnCheckedChangeListener { _, checked: Boolean ->
            Settings.setAlarmVibrationEnabled(context!!, checked)
        }
        enableAlarmVibration.isChecked = Settings.getAlarmVibrationEnabled(context!!)


        val accurateNotificationTimes =
            root.findViewById<SwitchCompat>(R.id.accurateNotificationTimes)
        accurateNotificationTimes.setOnCheckedChangeListener { _, checked: Boolean ->
            Settings.setAccurateNotificationsEnabled(context!!, checked)
        }
        accurateNotificationTimes.isChecked = Settings.getAccurateNotificationsEnabled(context!!)


        root.findViewById<Button>(R.id.disableAllReminders).setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Disable Reminders")
                .setMessage("This will disable reminders for all To-Do list tasks and all timetable events (for every timetable). Continue?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Disable Reminders") { _, _ ->
                    DB.disableAllReminders()
                    Notifications.clearPendingNotifications(context!!)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            root.findViewById<Button>(R.id.exportDB).setOnClickListener {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "data.db")
                }
                // See MainActivity.onActivityResult
                activity!!.startActivityForResult(intent, MainActivity.CREATE_FILE_REQUEST_CODE)
            }

            root.findViewById<Button>(R.id.importDB).setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle("Import database")
                    .setMessage("This will overwrite all data stored in the app. This action cannot be undone. Are you sure you want to do this?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Import database") { _, _ ->
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                        activity!!.startActivityForResult(
                            intent,
                            MainActivity.OPEN_FILE_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            val c = (root.findViewById<LinearLayout>(R.id.settingsContents) as ViewGroup)
            c.removeView(root.findViewById<TextView>(R.id.exportDB))
            c.removeView(root.findViewById<TextView>(R.id.importDB))
        }

        val qit = root.findViewById<EditText>(R.id.qit)
        qit.setText(Settings.getQIT(context!!))
        qit.addTextChangedListener {
            Settings.setQIT(context!!, qit.text.toString())
        }

        (activity as MainActivity).setToolbarTitle("Settings")

        return root
    }

    private fun setTimetableStartTime(hr: Int?) {
        val hour = hr ?: Settings.getTimetableStartHour(context!!)
        val hourString = hour.toString().padStart(2, '0')
        startTime.text = "$hourString:00"
        Settings.setTimetableStartHour(context!!, hour)
    }

    private fun setTimetableEndTime(hr: Int?) {
        val hour = hr ?: Settings.getTimetableEndHour(context!!)
        val hourString = hour.toString().padStart(2, '0')
        endTime.text = "$hourString:00"
        Settings.setTimetableEndHour(context!!, hour)
    }

}
