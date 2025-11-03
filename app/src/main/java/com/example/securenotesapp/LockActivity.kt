package com.example.securenotesapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LockActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var tvPrompt: TextView
    private var authenticationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        tvPrompt = findViewById(R.id.tvPrompt)
        executor = ContextCompat.getMainExecutor(this)

        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            setupBiometricPrompt()
        } else {
            tvPrompt.text = "Biometria nie je dostupná."
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authenticationInProgress = false
                LockManager.isLocked = false
                openMain()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                authenticationInProgress = false
                tvPrompt.text = "Chyba autentifikácie: $errString"
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                tvPrompt.text = "Autentifikácia zlyhala"
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Odomknutie Secure Notes")
            .setSubtitle("Použi odtlačok prsta alebo PIN zariadenia")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    override fun onStart() {
        super.onStart()

        if (!authenticationInProgress && ::biometricPrompt.isInitialized) {
            authenticationInProgress = true
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun openMain() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // neumožni návrat bez odomknutia
    }
}
