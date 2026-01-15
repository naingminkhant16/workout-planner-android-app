package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.nmk.fitlife.data.database.AppDatabase
import com.nmk.fitlife.data.user.User
import com.nmk.fitlife.data.user.UserRepository
import com.nmk.fitlife.data.user.UserViewModel
import com.nmk.fitlife.data.user.UserViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var tvNameErrMsg: TextView
    private lateinit var tvEmailErrMsg: TextView
    private lateinit var tvPasswordErrMsg: TextView

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository(
                AppDatabase.getDatabase(this@RegisterActivity).userDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeComponents()

        btnLogin.setOnClickListener {
            startActivity(
                Intent(
                    this@RegisterActivity,
                    LoginActivity::class.java
                )
            )
        }

        btnRegister.setOnClickListener {
            handleBtnRegisterOnClick()
        }
    }

    private fun initializeComponents() {
        // initialize ui components
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        tvNameErrMsg = findViewById(R.id.errMsgName)
        tvEmailErrMsg = findViewById(R.id.errMsgEmail)
        tvPasswordErrMsg = findViewById(R.id.errMsgPassword)

        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun handleBtnRegisterOnClick() {
        // validate user inputs
        hideErrors()

        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        val isValid = when {
            name.isEmpty() -> {
                showNameError("Required"); false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showEmailError("Invalid Email"); false
            }

            password.length < 6 -> {
                showPasswordError("Too short"); false
            }

            password != confirmPassword -> {
                showPasswordError("Passwords don't match"); false
            }

            else -> true
        }

        if (isValid) {
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                try {
                    val userExists = userViewModel.getUserByEmail(email)
                    if (userExists != null) {
                        // check email exists
                        Toast.makeText(
                            this@RegisterActivity,
                            "Email already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        userViewModel.registerUser(
                            User(
                                name = name,
                                email = email,
                                password = password,
                                createdAt = System.currentTimeMillis().toString()
                            )
                        )
                        Toast.makeText(
                            this@RegisterActivity,
                            "Successfully Registered",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    }
                } finally {
                    btnRegister.isEnabled = true
                }
            }
        }
    }

    private fun hideErrors() {
        tvNameErrMsg.visibility = View.GONE
        tvEmailErrMsg.visibility = View.GONE
        tvPasswordErrMsg.visibility = View.GONE
    }

    private fun showNameError(msg: String) {
        tvNameErrMsg.text = "* $msg"
        tvNameErrMsg.visibility = View.VISIBLE
    }

    private fun showEmailError(msg: String) {
        tvEmailErrMsg.text = "* $msg"
        tvEmailErrMsg.visibility = View.VISIBLE
    }

    private fun showPasswordError(msg: String) {
        tvPasswordErrMsg.text = "* $msg"
        tvPasswordErrMsg.visibility = View.VISIBLE
    }
}