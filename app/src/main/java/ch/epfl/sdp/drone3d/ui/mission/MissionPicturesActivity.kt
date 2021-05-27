/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.ui.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * An activity showing a sample of the pictures taken by the drone
 */
@AndroidEntryPoint
class MissionPicturesActivity : AppCompatActivity() {

    companion object {
        private const val SAMPLE_SIZE = 20

        private const val CLIPBOARD_NAME = "mission_pictures"
    }

    private val disposables = CompositeDisposable()

    @Inject
    lateinit var photos: DronePhotos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_pictures)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.mission_pictures_view)
        val adapter = PictureViewAdapter()
        recyclerView.adapter = adapter

        disposables.add(photos.getRandomPhotos(SAMPLE_SIZE)
            .subscribeOn(Schedulers.io())
            .retry(10)
            .subscribe(
                {
                    runOnUiThread { adapter.submitList(it) }
                },
                {
                    ToastHandler.showToastAsync(this, R.string.error, Toast.LENGTH_SHORT, it.message)
                    Timber.e(it, "Failed to download pictures")
                    finish()
                }
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    fun copyUrls(@Suppress("UNUSED_PARAMETER") view: View) {
        disposables.add(photos.getPhotosUrl()
            .subscribe(
                {
                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(CLIPBOARD_NAME, it.joinToString("\n"))
                    clipboardManager.setPrimaryClip(clipData)

                    ToastHandler.showToastAsync(this, getString(R.string.pictures_copied))
                },
                {
                    Timber.e(it)
                    ToastHandler.showToastAsync(this, R.string.error, Toast.LENGTH_SHORT, it.message)
                }
            )
        )
    }
}