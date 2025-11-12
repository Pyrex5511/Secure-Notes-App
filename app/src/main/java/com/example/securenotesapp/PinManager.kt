// PinManager.kt

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PinManager {
    private const val PREF_NAME = "secure_notes_pin_prefs"
    private const val PIN_KEY = "user_pin"

    private lateinit var sharedPreferences: EncryptedSharedPreferences

    fun initialize(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun savePin(pin: String) {
        sharedPreferences.edit().putString(PIN_KEY, pin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedPin = sharedPreferences.getString(PIN_KEY, null)
        return storedPin != null && storedPin == pin
    }

    fun hasPin(): Boolean {
        return sharedPreferences.contains(PIN_KEY)
    }
}