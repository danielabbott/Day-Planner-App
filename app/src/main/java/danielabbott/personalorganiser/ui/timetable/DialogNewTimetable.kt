package danielabbott.personalorganiser.ui.timetable

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import danielabbott.personalorganiser.Notifications
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Settings

// A dialog with a text field
class DialogNewTimetable(
    private val cloneFrom: Long? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val textBox = EditText(context)
        textBox.hint = "Timetable name..."
        textBox.isSingleLine = true
        textBox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        val builder = AlertDialog.Builder(activity!!)
            .setView(textBox)
            .setPositiveButton("Create") { _, _ ->

                if (textBox.text != null && textBox.text.isNotEmpty()) {
                    val name = textBox.text.toString()
                    try {
                        val id: Long = if (cloneFrom == null) {
                            DB.createNewTimetable(name)
                        } else {
                            DB.cloneTimetable(cloneFrom, name)
                        }
                        Settings.setActiveTimetable(id, context!!)
                        Notifications.scheduleAllNotifications(context!!)

                        val fragment = TimetableFragment()
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.replace(R.id.fragmentView, fragment)
                        fragmentTransaction.commit()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
        return builder.create()
    }
}
