package danielabbott.personalorganiser.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import danielabbott.personalorganiser.ImagePick
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.GoalListData

// Base class for timetable,task,goal edit pages
// Implements functionality common to multiple pages ^
open class DataEntryFragmentBasic : Fragment(), OnBackPressed {

    companion object {
        const val TAG = "DataEntryFragmentBasic"
    }

    var unsavedData: Boolean = false

    override fun onBackPressed(onNoChangesOrDiscardChanges: () -> Unit) {
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

    // https://stackoverflow.com/a/26911627/11498001
    fun hideKeyboard() {
        val inputManager: InputMethodManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val currentFocusedView = activity!!.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

}