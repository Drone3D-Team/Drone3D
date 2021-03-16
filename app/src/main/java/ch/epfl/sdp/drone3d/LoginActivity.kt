package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val LOGIN_MESSAGE = "Enter your login information."

/**
 * The activity that allows the user to log in
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var infoText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeWidgets()

        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        infoText.text = LOGIN_MESSAGE

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeWidgets() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        infoText = findViewById(R.id.infoText)
        progressBar = findViewById(R.id.progressBar)
    }

    fun login(view: View) {
        progressBar.visibility = View.VISIBLE
        infoText.visibility = View.GONE
        //Process input

    }

    fun register(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}