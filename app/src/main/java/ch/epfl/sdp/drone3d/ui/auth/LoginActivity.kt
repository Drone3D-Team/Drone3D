/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.ui.MainActivity
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.Utils

/**
 * The activity that allows the user to log in
 */
class LoginActivity : AuthActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initUI()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        infoText.text = getString(R.string.login_info_default)

        val loginButton: Button = findViewById(R.id.loginButton)
        Utils.pressButtonWhenTextIsDone(passwordEditText, loginButton)
    }

    override fun success() {
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(
            baseContext, R.string.login_success,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Login an user by taking the contents of [emailEditText] and of [passwordEditText]
     */
    fun login(view: View) {

        Utils.closeKeyboard(view, this)
        val emailText = emailEditText.text.toString()
        val passwordText = passwordEditText.text.toString()
        if (emailText == "" || passwordText == "") {
            ToastHandler.showToast(baseContext, R.string.login_fail)
            writeErrorMessage(getString(R.string.email_or_password_empty))
        } else {
            startProcess(
                authService.login(emailEditText.text.toString(), passwordEditText.text.toString()),
                R.string.login_fail
            )
        }
    }

    /**
     * Go to the activity allowing an user to register
     */
    fun register(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}