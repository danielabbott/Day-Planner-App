package danielabbott.personalorganiser.ui.notes

import android.app.Dialog
import android.content.DialogInterface
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

class DialogAddTag(val callback: (String) -> Unit): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var textBox = EditText(context)
        textBox.hint = "Tag name..."
        textBox.isSingleLine = true
        textBox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        // TODO autocomplete in textbox (similar to URL bar in a web browser)

        val builder = AlertDialog.Builder(activity!!)
            .setView(textBox)
            .setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->

                if (textBox.text != null && textBox.text.isNotEmpty()) {
                    callback(textBox.text.toString())
                }
            })
            .setNegativeButton("Cancel", null)
        return builder.create()
    }
}
