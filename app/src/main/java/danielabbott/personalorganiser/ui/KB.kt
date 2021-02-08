package danielabbott.personalorganiser.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KB {
    // https://stackoverflow.com/a/17789187/11498001
    fun hideKeyboard(activity: Activity) {
        val inputManager: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var currentFocusedView = activity.currentFocus
        if (currentFocusedView == null) {
            currentFocusedView = View(activity)
        }
        inputManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken,
            0
        )
    }
}