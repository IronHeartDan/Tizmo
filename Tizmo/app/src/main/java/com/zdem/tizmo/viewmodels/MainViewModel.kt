package com.zdem.tizmo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainViewModel : ViewModel() {
    val userLiveData = MutableLiveData<FirebaseUser?>()


    fun listenFirebaseAuthChanges() {
        // Listen to Auth Changes
        FirebaseAuth.getInstance().addAuthStateListener {
            userLiveData.postValue(it.currentUser)
        }
    }
}