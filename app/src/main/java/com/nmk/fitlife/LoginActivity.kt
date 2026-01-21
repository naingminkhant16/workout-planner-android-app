package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.nmk.fitlife.data.database.AppDatabase
import com.nmk.fitlife.data.user.UserRepository
import com.nmk.fitlife.data.user.UserViewModel
import com.nmk.fitlife.data.user.UserViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvEmailErrMsg: TextView
    private lateinit var tvPasswordErrMsg: TextView
    private lateinit var cbShowPassword: CheckBox
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository(
                AppDatabase.getDatabase(this@LoginActivity).userDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeComponents()

        // listen register btn
        btnRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        // listen login btn
        btnLogin.setOnClickListener { handleLoginBtnListener() }

        // listen show password checkbox
        cbShowPassword.setOnCheckedChangeListener { _, checked ->
            etPassword.transformationMethod =
                if (checked) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()

            etPassword.setSelection(etPassword.text.length)
        }
    }

    private fun hideErrMessages() {
        tvEmailErrMsg.visibility = View.GONE
        tvPasswordErrMsg.visibility = View.GONE
    }

    private fun handleLoginBtnListener() {
        val email: String = etEmail.text.toString()
        val password: String = etPassword.text.toString()

        hideErrMessages()
        btnLogin.isEnabled = false

        var validationFailed = false
        if (email.isEmpty()) {
            tvEmailErrMsg.visibility = View.VISIBLE
            validationFailed = true
        }

        if (password.isEmpty()) {
            tvPasswordErrMsg.visibility = View.VISIBLE
            validationFailed = true
        }

        if (!validationFailed) {
            lifecycleScope.launch {
                try {
                    val user = userViewModel.getUserByEmail(email)
                    if (user == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Email doesn't exist!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    if (userViewModel.verifyPassword(password, user.password)) {
                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT)
                            .show()

                        // stored auth user data in shared preferences
                        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)

                        authPrefs.edit {
                            putInt("id", user.id)
                            putString("name", user.name)
                            putString("email", user.email)
                        }

                        // start home page activity
                        startActivity(
                            Intent(this@LoginActivity, MainActivity::class.java)
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Password incorrect!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun initializeComponents() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        tvEmailErrMsg = findViewById(R.id.errMsgEmail)
        tvPasswordErrMsg = findViewById(R.id.errMsgPassword)

        cbShowPassword = findViewById(R.id.cbShowPassword)
    }
}