/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Drone3D : Application() {

    init {
        instance = this
    }

    // Singleton pattern
    companion object {
        private var instance: Drone3D? = null

        fun getInstance(): Drone3D {
            return instance!!
        }

        /**
         * Create a formatted CharSequence from a string resource containing arguments and HTML formatting
         *
         * The string resource must be wrapped in a CDATA section so that the HTML formatting is conserved.
         *
         * Example of an HTML formatted string resource:
         * <string name="html_formatted"><![CDATA[ bold text: <B>%1$s</B> ]]></string>
         *
         * [Source](https://stackoverflow.com/questions/23503642)
         */
        fun Context.getText(@StringRes id: Int, vararg args: Any?): CharSequence =
                HtmlCompat.fromHtml(getString(id, *args), HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}