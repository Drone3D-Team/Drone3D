package ch.epfl.sdp.drone3d.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.makeText
import ch.epfl.sdp.drone3d.Drone3D

/**
 * This class centralize [Toast] creation. It allows lazy developers to show meaningful information
 * very easily.
 */
object ToastHandler {

    private val lock: Any = Any()

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var lastToast: Toast? = null

    private fun update(toast: Toast): Toast {
        // cancel last toast and update it
        synchronized(lock) {
            lastToast?.cancel()
            lastToast = toast
        }

        return toast
    }

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToast(text: String,
                  duration: Int = Toast.LENGTH_SHORT,
                  context: Context = Drone3D.getInstance()) {
        update(makeText(context, text, duration)).show()
    }


    /**
     * Show a toast with given the test [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToast(resId: Int,
                  duration: Int = Toast.LENGTH_SHORT,
                  context: Context = Drone3D.getInstance()) {
        update(makeText(context, resId, duration)).show()
    }

    /**
     * Show a [String.format] text with arguments [args] in a [Toast] of given [duration]
     * (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastF(format: String,
                   duration: Int,
                   context: Context = Drone3D.getInstance(),
                   vararg args: Any) {
        val text = format.format(*args)
        showToast(text, duration, context)
    }

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastAsync(text: String,
                       duration: Int = Toast.LENGTH_SHORT,
                       context: Context = Drone3D.getInstance()) {
        handler.post{ showToast(text, duration, context) }
    }

    /**
     * Show a toast with given the test [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastAsync(context: Context = Drone3D.getInstance(),
                       resId: Int,
                       duration: Int = Toast.LENGTH_SHORT) {
        handler.post{ showToast(resId, duration, context) }
    }

    /**
     * Show a [String.format] text with arguments [args] in a [Toast] of given [duration]
     * (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function can be called from any thread
     */
    fun showToastAsyncF(format: String,
                        duration: Int,
                        context: Context = Drone3D.getInstance(),
                        vararg args: Any) {
        handler.post{ showToastF(format, duration, context, *args) }
    }
}