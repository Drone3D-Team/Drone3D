package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * The activity that allows the user to log in
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var infoText: TextView
    private lateinit var progressBar: ProgressBar

    private val loginMessage = "Enter your login information."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        backButton = findViewById(R.id.backButton)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText = findViewById(R.id.infoText)
        progressBar = findViewById(R.id.progressBar)

        infoText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        infoText.text = loginMessage

        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            infoText.visibility = View.GONE
            //Process input
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}