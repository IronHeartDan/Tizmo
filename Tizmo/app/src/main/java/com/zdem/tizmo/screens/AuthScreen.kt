package com.zdem.tizmo.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

val auth = FirebaseAuth.getInstance()
var verifyId by mutableStateOf("")
var codeSent by mutableStateOf(false)
var isLoading by mutableStateOf(false)

@Composable
fun AuthScreen() {
    var textInput by remember {
        mutableStateOf("")
    }
    var isValid by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = codeSent, block = {
        if (codeSent) {
            textInput = ""
            isValid = false
        }
    })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (!codeSent) {
                    Text(text = "Enter Phone")
                } else {
                    Text(text = "Enter OTP")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            value = textInput, onValueChange = {
                isValid = if (!codeSent) {
                    it.length == 10
                } else {
                    it.length == 6
                }
                textInput = it
            })
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isValid && !isLoading,
            onClick = {
                if (!codeSent) {
                    verifyPhone("+91$textInput")
                } else {
                    val credential = PhoneAuthProvider.getCredential(verifyId, textInput)
                    signInWithPhoneAuthCredential(credential)
                }
            }) {
            if (!codeSent && !isLoading) {
                Text(text = "Send OTP")
            } else if (!codeSent && isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sending OTP")
                    Spacer(modifier = Modifier.width(20.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (codeSent && !isLoading) {
                Text(text = "LogIn")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Logging...")
                    Spacer(modifier = Modifier.width(20.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

fun verifyPhone(phoneNumber: String) {
    isLoading = true
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            codeSent = true
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            isLoading = false
            Log.d("AuthScreen", "onVerificationFailed: ${e.message}")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            codeSent = true
            verifyId = verificationId
            isLoading = false
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber) // Phone number to verify
        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    isLoading = true
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Log.d("AuthScreen", "onVerificationFailed: Invalid Code")
                    isLoading = false
                }
            }
        }
}