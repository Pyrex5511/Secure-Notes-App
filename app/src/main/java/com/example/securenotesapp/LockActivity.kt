package com.example.securenotesapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.concurrent.Executor

class LockActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        executor = ContextCompat.getMainExecutor(this)

        val tvPrompt = findViewById<TextView>(R.id.tvPrompt)
        val btnBiometric = findViewById<Button>(R.id.btnUseBiometric)
        val etPin = findViewById<EditText>(R.id.etPin)
        val btnSubmitPin = findViewById<Button>(R.id.btnSubmitPin)

        // Inicializuj bezpečné SharedPreferences na ukladanie PINu
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPrefs = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Ak PIN ešte nie je nastavený, nastav jednoduchý default (alebo rozbaľ flow pre nastavenie)
        if (!sharedPrefs.contains("pin")) {
            sharedPrefs.edit().putString("pin", "1234").apply() // LEN PRÍKLAD
        }

        // Biometric availability check
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        val biometricAvailable = (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)

        if (!biometricAvailable) {
            btnBiometric.isEnabled = false
            btnBiometric.text = "Fingerprint unavailable"
        }

        // BiometricPrompt callback
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                runOnUiThread {
                    // authenticated -> open main
                    openMain()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                runOnUiThread {
                    tvPrompt.text = "Authentication error: $errString"
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                runOnUiThread {
                    tvPrompt.text = "Authentication failed"
                }
            }
        })

        // Prompt info: umožní aj device credential ako fallback (PIN/pattern)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Notes")
            .setSubtitle("Authenticate to open the app")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        // Biometric button
        btnBiometric.setOnClickListener {
            // spusti Biometric prompt
            biometricPrompt.authenticate(promptInfo)
        }

        // PIN unlock
        btnSubmitPin.setOnClickListener {
            val entered = etPin.text.toString()
            val saved = sharedPrefs.getString("pin", null)
            if (saved != null && entered == saved) {
                openMain()
            } else {
                tvPrompt.text = "Wrong PIN"
            }
        }
    }

    private fun openMain() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    // optional: block back so user cannot go to previous activity (if necessary)
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // prevent going back from lock screen (optional)
        // super.onBackPressed()
    }
}
