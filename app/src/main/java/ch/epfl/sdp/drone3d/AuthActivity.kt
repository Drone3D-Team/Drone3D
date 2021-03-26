package ch.epfl.sdp.drone3d

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Authentication UI, abstract [LoginActivity] and [RegisterActivity] common behavior
 */
@AndroidEntryPoint
abstract class AuthActivity : AppCompatActivity()  {

    protected lateinit var infoText: TextView
    private lateinit var progressBar: ProgressBar

    @Inject lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun initUI() {
        infoText = findViewById(R.id.infoText)
        progressBar = findViewById(R.id.progressBar)

        infoText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    /**
     * Start the authentication process given the [authTask] that is being complete and
     * the id [failMessage] of a String the show ehn the task fails
     */
    protected fun startProcess(authTask: Task<AuthResult>, failMessage: Int) {
        progressBar.visibility = View.VISIBLE
        infoText.visibility = View.GONE
        //Process input
        authTask.addOnCompleteListener(this) {
            task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    success()
                } else {
                    // If sign in fails, display a message to the user.
                    ToastHandler.showToast(baseContext, failMessage)
                    if (task.exception?.message != null) {
                        infoText.text = task.exception?.message
                        infoText.setTextColor(Color.RED)
                        infoText.visibility = View.VISIBLE
                    }
                }
            }
    }

    /**
     * Called when the authentication task is a success
     */
    protected abstract fun success()
}