// LockActivity.kt (Aktualizované)

package com.example.securenotesapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import java.util.concurrent.Executor

class LockActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Biometrické UI prvky
    private lateinit var tvPrompt: TextView
    private lateinit var btnTryAgain: Button

    // NOVÉ PIN UI prvky
    private lateinit var tvPinTitle: TextView
    private lateinit var etPin: EditText
    private lateinit var btnPinConfirm: Button

    private var authenticationInProgress = false
    private var isBiometricsAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        // Inicializácia PinManagera
        PinManager.initialize(applicationContext)

        // Inicializácia UI prvkov
        tvPrompt = findViewById(R.id.tvPrompt)
        btnTryAgain = findViewById(R.id.btnTryAgain)
        // Nové PIN prvky
        tvPinTitle = findViewById(R.id.tvPinTitle)
        etPin = findViewById(R.id.etPin)
        btnPinConfirm = findViewById(R.id.btnPinConfirm)

        executor = ContextCompat.getMainExecutor(this)

        checkBiometricAvailability()

        if (isBiometricsAvailable) {
            setupBiometricPrompt()
            // Logika Biometrie sa spustí v onStart
        } else {
            // Biometria nie je k dispozícii - prepnutie na PIN
            tvPrompt.text = "Biometria nie je dostupná."
            startPinFallback()
        }

        // Listener pre tlačidlo "Skúsiť znova" (pre Biometriu)
        btnTryAgain.setOnClickListener {
            btnTryAgain.isVisible = false
            tvPrompt.text = "Authentification"
            if (::biometricPrompt.isInitialized) {
                startBiometricAuth()
            }
        }

        // Listener pre tlačidlo "Potvrdiť PIN" (pre PIN)
        btnPinConfirm.setOnClickListener {
            handlePinConfirmation()
        }
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        isBiometricsAvailable = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    // --- LOGIKA BIOMETRIE ---

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthenticationFailedOrError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Zlyhanie (nesprávny prst), systémový dialóg to obslúži.
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlocking Secure Notes")
            .setSubtitle("Use fingerprint or PIN to unlock")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    private fun startBiometricAuth() {
        if (!authenticationInProgress && ::biometricPrompt.isInitialized) {
            authenticationInProgress = true
            tvPrompt.text = "Authentification" // Reset textu
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun onAuthenticationSuccess() {
        authenticationInProgress = false
        // Predpokladáme, že LockManager.isLocked je nastavený na false v openMain() alebo niekde inde.
        openMain()
    }

    private fun onAuthenticationFailedOrError(errorCode: Int, errString: CharSequence) {
        authenticationInProgress = false

        val message: String
        var showTryAgainButton = true

        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED -> {
                message = "Authentification was cancelled."
            }
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                showTryAgainButton = false
                message = "Príliš veľa neúspešných pokusov. Skúste neskôr."
            }
            else -> {
                message = "Chyba overenia: $errString"
            }
        }

        tvPrompt.text = message
        btnTryAgain.isVisible = showTryAgainButton
    }

    // --- LOGIKA PIN KÓDU (FALLBACK) ---

    private fun startPinFallback() {
        // Skryjeme Biometrické UI
        tvPrompt.isVisible = false
        btnTryAgain.isVisible = false

        // Zobrazíme PIN UI
        tvPinTitle.isVisible = true
        etPin.isVisible = true
        btnPinConfirm.isVisible = true

        if (PinManager.hasPin()) {
            startPinVerification()
        } else {
            startPinSetup()
        }
    }

    private fun startPinSetup() {
        etPin.setText("")
        tvPinTitle.text = "Nastavte si 4-miestny PIN pre odomknutie (záloha):"
    }

    private fun startPinVerification() {
        etPin.setText("")
        tvPinTitle.text = "Zadajte záložný PIN pre odomknutie:"
    }

    private fun handlePinConfirmation() {
        val pin = etPin.text.toString()

        if (pin.length < 4) {
            Toast.makeText(this, "PIN musí mať aspoň 4 číslice.", Toast.LENGTH_SHORT).show()
            return
        }

        if (PinManager.hasPin()) {
            // Režim overovania
            if (PinManager.verifyPin(pin)) {
                openMain()
            } else {
                Toast.makeText(this, "Nesprávny PIN.", Toast.LENGTH_SHORT).show()
                etPin.setText("")
            }
        } else {
            // Režim nastavenia
            PinManager.savePin(pin)
            Toast.makeText(this, "PIN úspešne nastavený!", Toast.LENGTH_SHORT).show()
            openMain()
        }
    }

    // --- SPOLOČNÉ METÓDY ---

    override fun onStart() {
        super.onStart()

        // Ak je biometria k dispozícii, spustíme ju
        if (isBiometricsAvailable) {
            startBiometricAuth()
        }
        // Ak nie je, fallback (PIN) UI je už zobrazené a čaká na interakciu v onCreate.
    }

    private fun openMain() {
        // Správne nastavenie stavu odomknutia
        LockManager.isLocked = false
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Ukončí celú aplikáciu, ak používateľ nechce zadať PIN/Biometriu
        finishAffinity()
    }
}