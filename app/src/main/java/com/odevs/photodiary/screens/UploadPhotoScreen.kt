package com.odevs.photodiary.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.odevs.photodiary.ui.components.BubbleButton
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate

@Composable
fun UploadPhotoScreen(
    navController: NavController,
    selectedDate: String,
    viewModel: PhotoViewModel = hiltViewModel()
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.replacePhotoForDate(LocalDate.parse(selectedDate), it)
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDA769)),
        contentAlignment = Alignment.Center
    ) {
        BubbleButton(
            text = "Select Photo for today",
            color = Color(0xFF90EE90),
            onClick = { launcher.launch("image/*") },
            fontSize = 20,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}
