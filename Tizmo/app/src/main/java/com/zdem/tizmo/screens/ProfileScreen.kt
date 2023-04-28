package com.zdem.tizmo.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


@Composable
fun ProfileScreen(user: FirebaseUser) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(150.dp)
                .padding(0.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(label = {
            Text(text = "Name")
        }, value = "${user.displayName}", onValueChange = {})
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(label = {
            Text(text = "Email")
        }, value = "${user.email}", onValueChange = {})
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(label = {
            Text(text = "Phone")
        }, value = "${user.phoneNumber}", onValueChange = {})
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {

        }) {
            Text(text = "Save")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
        }) {
            Text(text = "SignOut")
        }
    }
}