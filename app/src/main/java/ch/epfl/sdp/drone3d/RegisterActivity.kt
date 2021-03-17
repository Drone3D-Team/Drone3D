package ch.epfl.sdp.drone3d

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity that allows the user to register
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var pseudoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var infoText: TextView
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeWidgets()

        pseudoEditText.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        infoText.text = getString(R.string.register_info_default)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeWidgets() {
        pseudoEditText = findViewById(R.id.pseudoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        infoText = findViewById(R.id.infoText)
        progressBar = findViewById(R.id.progressBar)
    }

    fun register(@Suppress("UNUSED_PARAMETER") view: View) {
        progressBar.visibility = View.VISIBLE
        infoText.visibility = View.GONE
        //Process input
        authService.register(emailEditText.text.toString(), passwordEditText.text.toString())
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sign in success, show Toast
                        startActivity(Intent(this, MainActivity::class.java))
                        Toast.makeText(baseContext, R.string.register_success,
                                Toast.LENGTH_SHORT).show()
                        //TODO Set pseudo
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, R.string.register_fail,
                                Toast.LENGTH_SHORT).show()
                        if (task.exception?.message != null) {
                            infoText.text = task.exception?.message
                            infoText.setTextColor(Color.RED)
                            infoText.visibility = View.VISIBLE
                        }
                    }
                }
    }

    fun login(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}