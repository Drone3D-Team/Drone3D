package ch.epfl.sdp.drone3d

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class AuthActivity : AppCompatActivity()  {

    protected lateinit var infoText: TextView
    protected lateinit var progressBar: ProgressBar

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
                    Toast.makeText(baseContext, failMessage,
                            Toast.LENGTH_SHORT).show()
                    if (task.exception?.message != null) {
                        infoText.text = task.exception?.message
                        infoText.setTextColor(Color.RED)
                        infoText.visibility = View.VISIBLE
                    }
                }
            }
    }

    protected abstract fun success()
}