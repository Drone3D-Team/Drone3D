package ch.epfl.sdp.drone3d.ui.mission

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.module.PhotosModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.reactivex.subjects.SingleSubject
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.*
import java.net.URL

@HiltAndroidTest
@UninstallModules(PhotosModule::class)
class MissionPicturesActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MissionPicturesActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(activityRule)

    @BindValue
    val photosService: DronePhotos = mock(DronePhotos::class.java)

    private val randomSubject = SingleSubject.create<List<Bitmap>>()
    private val photosUrlsSubject = SingleSubject.create<List<String>>()

    init {
        `when`(photosService.getRandomPhotos(anyInt())).thenReturn(randomSubject)
        `when`(photosService.getPhotosUrl()).thenReturn(photosUrlsSubject)
    }

    @Test
    fun downloadedPicturesAreShown() {
        val bitmaps = listOf(
                "https://user-images.githubusercontent.com/44306955/119693549-adf15b80-be4c-11eb-905f-4e1642bf4e68.jpeg",
                "https://user-images.githubusercontent.com/44306955/119693554-af228880-be4c-11eb-9907-4822ba645127.jpeg",
                "https://user-images.githubusercontent.com/44306955/119693556-b053b580-be4c-11eb-926f-3efa9fb9174e.jpeg"
        ).map {
            val imageStream = URL(it).openStream()
            val image = BitmapFactory.decodeStream(imageStream)
            imageStream.close()
            image
        }

        randomSubject.onSuccess(bitmaps)

        Thread.sleep(100)

        onView(withId(R.id.mission_pictures_view))
                .check(MappingMissionSelectionActivityTest.matchCount(bitmaps.size))
    }

    @Test
    fun pressingCopyButtonUpdatesClipboard() {
        onView(withId(R.id.copy_pictures_links)).perform(click())
        photosUrlsSubject.onSuccess(listOf("AAAA", "BBBB"))

        Thread.sleep(100)

        val clipboardService = InstrumentationRegistry.getInstrumentation()
                .targetContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        assertThat(clipboardService.primaryClip?.getItemAt(0)?.text, `is`("AAAA\nBBBB"))
    }
}