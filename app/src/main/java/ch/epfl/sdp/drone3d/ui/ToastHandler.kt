package ch.epfl.sdp.drone3d.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ch.epfl.sdp.drone3d.Drone3D

/**
 * This class centralize [Toast] creation. It allows lazy developers to show meaningful information
 * very easily.
 */
object ToastHandler {

    private val handler: Handler = Handler(Looper.getMainLooper())

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToast(context: Context = Drone3D.applicationContext(),
                  text: String,
                  duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    /**
     * Show a toast with given the test [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToast(context: Context = Drone3D.applicationContext(),
                  resId: Int,
                  duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, resId, duration).show()
    }

    /**
     * Show a [String.format] text with arguments [args] in a [Toast] of given [duration]
     * (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastF(context: Context = Drone3D.applicationContext(),
                   format: String,
                   duration: Int,
                   vararg args: Any) {
        val text = format.format(*args)
        showToast(context, text, duration)
    }

    /**
     * Show a toast with given [text] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastAsync(context: Context = Drone3D.applicationContext(),
                       text: String,
                       duration: Int = Toast.LENGTH_SHORT) {
        handler.post{ showToast(context, text, duration) }
    }

    /**
     * Show a toast with given the test [resId] and of given [duration] (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function must be called from the UI thread
     */
    fun showToastAsync(context: Context = Drone3D.applicationContext(),
                       resId: Int,
                       duration: Int = Toast.LENGTH_SHORT) {
        handler.post{ showToast(context, resId, duration) }
    }

    /**
     * Show a [String.format] text with arguments [args] in a [Toast] of given [duration]
     * (by default [Toast.LENGTH_SHORT])
     *
     * A [context] other than the application one can be provided
     *
     * This function can be called from any thread
     */
    fun showToastAsyncF(context: Context = Drone3D.applicationContext(),
                        format: String,
                        duration: Int,
                        vararg args: Any) {
        handler.post{ showToastF(context, format, duration, *args) }
    }
}