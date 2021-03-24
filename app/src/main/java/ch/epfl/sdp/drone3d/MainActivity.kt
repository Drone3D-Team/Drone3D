package ch.epfl.sdp.drone3d

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * The activity that creates the main menu
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    /**
     * Go to LoginActivity when log_in_button is clicked
     */
    fun goToLogin(@Suppress("UNUSED_PARAMETER") view: View) {
        open(LoginActivity::class)
    }

    /**
     * Go to ItineraryCreateActivity when create_itinerary_button is clicked
     */
    fun goToItineraryCreate(@Suppress("UNUSED_PARAMETER") view: View) {
        open(ItineraryCreateActivity::class)
    }

    /**
     * Go to MappingMissionSelectionActivity when browse_itinerary_button is clicked
     */
    fun goToMappingMissionSelection(@Suppress("UNUSED_PARAMETER") view: View) {
        if (authService.hasActiveSession()) {
            open(MappingMissionSelectionActivity::class)
        } else {
            goToLogin(view)
        }
    }

    /**
     * Go to DroneConnectActivity when connect_drone_button is clicked
     * TODO : replace TempTestActivity by DroneConnectActivity once it exists
     */
    fun goToDroneConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        open(TempTestActivity::class)
    }

    private fun <T> open(activity: KClass<T>) where T : Activity {
        val intent = Intent(this, activity.java).apply {}
        startActivity(intent)
    }
}