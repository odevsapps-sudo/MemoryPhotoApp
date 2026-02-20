package com.odevs.photodiary.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate
import androidx.navigation.NavHostController

@Composable
fun WeeklyPreviewScreen(navController: NavHostController, viewModel: PhotoViewModel) {
    val imageMap by viewModel.imageMap.collectAsState()
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedDate?.let { date ->
            uri?.let { viewModel.replacePhotoForDate(date, it) }
        }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        itemsIndexed(imageMap.entries.toList()) { index, entry ->
            val (date, uri) = entry
            Box(modifier = Modifier
                .padding(4.dp)
                .clickable {
                    selectedDate = date
                    launcher.launch("image/*")
                }
            ) {
                if (uri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp)
                    )
                } else {
                    Text("Add", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
