package ch.epfl.sdp.drone3d

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

    private lateinit var errorMessageText: TextView
    private lateinit var progressBar: ProgressBar

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

        errorMessageText = findViewById(R.id.errorMessageText)
        progressBar = findViewById(R.id.progressBar)

        errorMessageText.visibility = View.GONE
        progressBar.visibility = View.GONE

        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            //Process input
        }

    }
}