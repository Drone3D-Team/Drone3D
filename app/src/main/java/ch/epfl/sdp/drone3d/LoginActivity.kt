/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*

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
    }

    override fun success() {
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(baseContext, R.string.login_success,
                Toast.LENGTH_SHORT).show()
    }

    fun login(@Suppress("UNUSED_PARAMETER") view: View) {
        startProcess(authService.login(emailEditText.text.toString(), passwordEditText.text.toString()),
                R.string.login_fail)
    }

    fun register(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}