package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val REGISTER_MESSAGE = "Enter your register information."

/**
 * The activity that allows the user to register
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var pseudoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var infoText: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeWidgets()

        pseudoEditText.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        infoText.text = REGISTER_MESSAGE

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

    fun register(view: View) {
        progressBar.visibility = View.VISIBLE
        infoText.visibility = View.GONE
        //Process input

    }

    fun login(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}