package ch.epfl.sdp.drone3d.ui.mission

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.ui.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MissionPicturesActivity : AppCompatActivity() {

    companion object {
        private const val SAMPLE_SIZE = 20
    }

    private var disposable: Disposable? = null

    @Inject lateinit var photos: DronePhotos
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_pictures)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.mission_pictures_view)
        val adapter = PictureViewAdapter()
        recyclerView.adapter = adapter

        disposable = photos.getRandomPhotos(SAMPLE_SIZE)
                .subscribeOn(Schedulers.io())
                .retry(10 )
                .subscribe(
                        {
                            adapter.submitList(it)
                        },
                        {
                            ToastHandler.showToastAsync(this, R.string.error, Toast.LENGTH_SHORT, it.message)
                            Timber.e(it, "Failed to download pictures")
                            finish()
                        }
                )
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
        disposable = null
    }
}