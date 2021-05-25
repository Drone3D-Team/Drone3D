package ch.epfl.sdp.drone3d.ui

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

object Utils {

    /**
     * Creates a listener on [editText] that presses [button] when the done
     * button is pressed on the keyboard
     * Requires the editText to have 'android:imeOptions="actionDone"' in its layout
     */
    fun setupPortTextListener(editText: EditText, button: Button) {
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                button.performClick()
                return@OnEditorActionListener true
            }
            false
        })
    }

    /**
     * Closes the keyboard of the activity whose context and view is passed
     */
    fun closeKeyboard(view: View, context: Context) {
        val inputMethodManager: InputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }
}