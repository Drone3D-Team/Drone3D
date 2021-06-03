/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import ch.epfl.sdp.drone3d.Drone3D.Companion.getText
import timber.log.Timber

/**
 * This class centralize [Toast] creation. It allows lazy developers to show meaningful information
 * very easily.
 */
object ToastHandler {

    private val handler: Handler = Handler(Looper.getMainLooper())
    @Volatile
    private var lastToast: Toast? = null

    private fun update(toast: Toast): Toast {
        // cancel last toast and update it
        synchronized(this) {
            lastToast?.cancel()
            lastToast = toast
        }

        return toast
    }

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     * The [context] in which the toast will be displayed must be provided
     *
     * The text can be formatted with arguments following [String.format]
     *
     * This function can only be called from the UI thread
     */
    fun showToast(
        context: Context,
        text: String,
        duration: Int = Toast.LENGTH_SHORT,
        vararg args: Any?
    ) {

        // Make sure to not format texts that should not be formatted
        val textF = if (args.isEmpty()) text else text.format(*args)
        Timber.i("Showing toast with text : $textF")
        update(makeText(context, textF, duration)).show()
    }

    /**
     * Show a toast with given the text [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     * The [context] in which the toast will be displayed must be provided
     *
     * The text can be formatted with arguments following [String.format]
     *
     * This function can only be called from the UI thread
     */
    fun showToast(
        context: Context,
        @StringRes resId: Int,
        duration: Int = Toast.LENGTH_SHORT,
        vararg args: Any?
    ) {

        // Make sure to not format texts that should not be formatted
        val textF = if (args.isEmpty()) context.getText(resId) else context.getText(resId, *args)

        Timber.i("Showing toast with text : $textF")
        update(makeText(context, textF, duration)).show()
    }

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     * The [context] in which the toast will be displayed must be provided
     *
     * The text can be formatted with arguments following [String.format]
     *
     * This function can be called from any thread
     */
    fun showToastAsync(
        context: Context,
        text: String,
        duration: Int = Toast.LENGTH_SHORT,
        vararg args: Any?
    ) {
        handler.post { showToast(context, text, duration, *args) }
    }

    /**
     * Show a toast with given the text [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     * The [context] in which the toast will be displayed must be provided
     *
     * The text can be formatted with arguments following [String.format]
     *
     * This function can be called from any thread
     */
    fun showToastAsync(
        context: Context,
        @StringRes resId: Int,
        duration: Int = Toast.LENGTH_SHORT,
        vararg args: Any?
    ) {
        handler.post { showToast(context, resId, duration, *args) }
    }
}