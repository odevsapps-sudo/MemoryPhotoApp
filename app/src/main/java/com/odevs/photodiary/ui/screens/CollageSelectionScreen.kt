package com.odevs.photodiary.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.odevs.photodiary.viewmodel.PhotoViewModel
import com.odevs.photodiary.graphics.SoftYellow
import com.odevs.photodiary.ui.LanguageProvider

@Composable
fun CollageSelectionScreen(
    navController: NavController
) {
    val parentEntry = remember { navController.getBackStackEntry("home") }
    val viewModel: PhotoViewModel = hiltViewModel(parentEntry)

    val language = LanguageProvider.language
    val context = LocalContext.current

    val allUris by viewModel.imageMap.collectAsState()
    val selectedUris by viewModel.selectedCollageUris.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    val sortedUris by remember(allUris.entries.toList()) {
        derivedStateOf {
            allUris.entries.sortedBy { it.key }.map { it.value }
        }
    }

    var isGenerating by remember { mutableStateOf(false) }

    val title = if (language == "hu") "Válassz képeket kollázshoz (2–12)" else "Select images for collage (2–12)"
    val buttonText = if (language == "hu") "Kollázs készítése" else "Create Collage"
    val errorText = if (language == "hu") "Hiba történt a kollázs készítésekor" else "An error occurred during collage creation"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftYellow)
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedUris) { uri ->
                val isSelected = selectedUris.contains(uri)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            width = if (isSelected) 4.dp else 1.dp,
                            color = if (isSelected) Color.Green else Color.Gray
                        )
                        .clickable { viewModel.toggleCollageSelection(uri) }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isGenerating) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isGenerating = true
                    viewModel.generateCollage(context) { success ->
                        isGenerating = false
                        if (success) {
                            navController.navigate("collagePreview/0")
                        } else {
                            Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = selectedUris.size in 2..12,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("$buttonText (${selectedUris.size})")
            }
        }
    }
}
