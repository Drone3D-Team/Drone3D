package ch.epfl.sdp.drone3d

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

/**
 * The activity that creates the main menu
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    /**
     * Go to LoginActivity when log_in_button is clicked
     */
    fun goToLogin(view: View) {
        open(LoginActivity::class)
    }

    /**
     * Go to ItineraryCreateActivity when create_itinerary_button is clicked
     */
    fun goToItineraryCreate(view: View) {
        open(ItineraryCreateActivity::class)
    }

    /**
     * Go to BrowseItineraryActivity when browse_itinerary_button is clicked
     * TODO : replace TempTestActivity by the BrowseItineraryActivity once it exists
     */
    fun goToItineraryBrowse(view: View) {
        open(MappingMissionSelectionActivity::class)
    }

    /**
     * Go to DroneConnectActivity when connect_drone_button is clicked
     * TODO : replace TempTestActivity by DroneConnectActivity once it exists
     */
    fun goToDroneConnect(view: View) {
        open(TempTestActivity::class)
    }

    private fun <T> open(activity: KClass<T>) where T : Activity {
        val intent = Intent(this, activity.java).apply {}
        startActivity(intent)
    }
}