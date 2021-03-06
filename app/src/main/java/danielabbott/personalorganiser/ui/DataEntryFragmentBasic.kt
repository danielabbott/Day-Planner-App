package danielabbott.personalorganiser.ui

import android.app.AlertDialog
import androidx.fragment.app.Fragment

// Base class for timetable,task,goal edit pages
// Implements functionality common to multiple pages ^
open class DataEntryFragmentBasic : Fragment(), OnBackPressed {

    var exitWithoutUnsavedChangesWarning: Boolean = false

    var anyUnsavedChanges: (() -> Boolean)? = null

    override fun onBackPressed(onNoChangesOrDiscardChanges: () -> Unit) {
        var unsavedData = false

        if (exitWithoutUnsavedChangesWarning) {
            unsavedData = false
        } else if (anyUnsavedChanges != null) {
            unsavedData = anyUnsavedChanges!!()
        }

        if (unsavedData) {
            AlertDialog.Builder(context)
                .setTitle("Unsaved changes")
                .setMessage("Going back will discard changes")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Discard changes") { _, _ ->
                    unsavedData = false
                    onNoChangesOrDiscardChanges()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            onNoChangesOrDiscardChanges()
        }
    }

}