package com.example.securenotesapp

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class SecureNotesApp : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()
        // Sleduj životný cyklus celej aplikácie (nie len jednej aktivity)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        // Appka ide do pozadia → zamkni
        LockManager.isLocked = true
    }

    override fun onStart(owner: LifecycleOwner) {
        // Appka sa vracia do popredia → neodomykaj automaticky,
        // nech to spraví LockActivity
    }
}
