package ch.epfl.sdp.drone3d

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider.getIP
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider.getPort
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider.isConnected
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider.isSimulation
import ch.epfl.sdp.drone3d.ui.drone.ConnectedDroneActivity
import ch.epfl.sdp.drone3d.ui.drone.DroneConnectActivity
import junit.framework.Assert.assertEquals
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DroneConnectActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DroneConnectActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * Make sure the context of the app is the right one
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @Test
    fun droneConnectWorks() {
        val ip = "1.1.1.1"
        val port = "1111"
        onView(ViewMatchers.withId(R.id.text_IP_address))
            .perform(typeText(ip))
        onView(ViewMatchers.withId(R.id.text_port))
            .perform(typeText(port))
        onView(ViewMatchers.withId(R.id.connect_button))
            .perform(ViewActions.click())

        assertEquals(ip, getIP())
        assertEquals(port, getPort())
        assertEquals(true, isConnected())
        assertEquals(true, isSimulation())
        Intents.intended(
            hasComponent(hasClassName(ConnectedDroneActivity::class.java.name))
        )
    }
}