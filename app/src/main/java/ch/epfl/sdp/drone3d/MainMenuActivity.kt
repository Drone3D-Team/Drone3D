package ch.epfl.sdp.drone3d

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainMenuActivity : AppCompatActivity() {
    /**
     * Creates the main menu
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    /**
     * Go to LoginActivity when log_in_button is clicked
     * TODO : replace TempTestActivity by the LoginActivity once it exists
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, TempTestActivity::class.java).apply{}
        startActivity(intent)
    }

    /**
     * Go to ItineraryCreateActivity when create_itinerary_button is clicked
     * TODO : replace TempTestActivity by the ItineraryCreateActivity once it exists
     */
    fun goToItineraryCreate(view: View) {
        val intent = Intent(this, TempTestActivity::class.java).apply{}
        startActivity(intent)
    }

    /**
     * Go to BrowseItineraryActivity when browse_itinerary_button is clicked
     * TODO : replace TempTestActivity by the BrowseItineraryActivity once it exists
     */
    fun goToItineraryBrowse(view: View) {
        val intent = Intent(this, TempTestActivity::class.java).apply{}
        startActivity(intent)
    }

    /**
     * Go to DroneConnectActivity when connect_drone_button is clicked
     * TODO : replace TempTestActivity by DroneConnectActivity once it exists
     */
    fun goToDroneConnect(view: View) {
        val intent = Intent(this, TempTestActivity::class.java).apply{}
        startActivity(intent)
    }
}