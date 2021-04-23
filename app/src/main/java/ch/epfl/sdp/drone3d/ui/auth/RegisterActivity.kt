/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.ui.MainActivity
import ch.epfl.sdp.drone3d.ui.ToastHandler

/**
 * The activity that allows the user to register
 */
class RegisterActivity : AuthActivity() {

    private lateinit var pseudoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initUI()

        pseudoEditText = findViewById(R.id.pseudoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        pseudoEditText.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText.text = getString(R.string.register_info_default)
    }

    override fun success() {
        // Registration is a success, show Toast and main activity
        startActivity(Intent(this, MainActivity::class.java))
        ToastHandler.showToast(baseContext, R.string.register_success)
        //TODO Set pseudo
    }

    /**
     * Register an user by taking the contents of [emailEditText], of [passwordEditText] and of [pseudoEditText]
     */
    fun register(@Suppress("UNUSED_PARAMETER") view: View) {
        val emailText = emailEditText.text.toString()
        val passwordText = passwordEditText.text.toString()
        if (emailText == "" || passwordText == "") {
            ToastHandler.showToast(baseContext, R.string.login_fail)
            writeErrorMessage(getString(R.string.email_or_password_empty))
        } else {
            startProcess(
                authService.register(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()),
                R.string.register_fail)
        }
    }

    /**
     * Go to the activity allowing an user to login
     */
    fun login(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}