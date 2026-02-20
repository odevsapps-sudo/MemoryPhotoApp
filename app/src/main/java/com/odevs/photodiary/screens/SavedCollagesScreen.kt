package com.odevs.photodiary.screens

import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.odevs.photodiary.graphics.LimeGreen
import com.odevs.photodiary.graphics.SoftYellow
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.ui.components.BubbleButton
import com.odevs.photodiary.ui.components.SmallBubbleButton
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedCollagesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val language = LanguageProvider.language

    val title = if (language == "hu") "Mentett kollázsok" else "Saved Collages"
    val noItems = if (language == "hu") "Még nincsenek mentett kollázsok" else "There are no saved collages yet"
    val deleteText = if (language == "hu") "Kijelöltek törlése" else "Delete selected"
    val homepageText = if (language == "hu") "Kezdőlap" else "Homepage"
    val confirmTitle = if (language == "hu") "Biztosan törölni szeretnéd ezeket?" else "Are you sure you want to delete them?"
    val yesText = if (language == "hu") "Igen" else "Yes"
    val cancelText = if (language == "hu") "Mégse" else "Cancel"
    val previewTitle = if (language == "hu") "Előnézet" else "Preview"
    val exportText = if (language == "hu") "📤 Export Instagramra" else "📤 Export to Instagram"
    val saveText = if (language == "hu") "💾 Mentés a telefonra" else "💾 Save to phone"
    val closeText = if (language == "hu") "Bezárás" else "Close"

    val collageDir = File(context.filesDir, "collages")
    var collageFiles by remember { mutableStateOf(collageDir.listFiles()?.toList()?.reversed() ?: emptyList()) }
    val selectedFiles = remember { mutableStateListOf<File>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var selectedImageForPreview by remember { mutableStateOf<File?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri: Uri? ->
        uri?.let { destinationUri ->
            selectedImageForPreview?.let { sourceFile ->
                try {
                    sourceFile.inputStream()?.use { input ->
                        context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftYellow)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (collageFiles.isEmpty()) {
            Text(noItems)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(collageFiles) { file ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                if (file in selectedFiles)
                                    Color.Red.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .combinedClickable(
                                onClick = {
                                    val index = collageFiles.indexOf(file)
                                    navController.navigate("collagePreview/$index")
                                },
                                onLongClick = {
                                    if (selectedFiles.contains(file)) {
                                        selectedFiles.remove(file)
                                    } else {
                                        selectedFiles.add(file)
                                    }
                                }
                            )
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = file),
                            contentDescription = "Collage",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (selectedFiles.isNotEmpty()) {
                BubbleButton(
                    text = deleteText,
                    color = Color.Red,
                    icon = Icons.Filled.Delete,
                    onClick = {
                        selectedFiles.forEach { it.delete() }
                        collageFiles = collageDir.listFiles()?.toList() ?: emptyList()
                        selectedFiles.clear()
                    }
                )
            }

            BubbleButton(
                text = homepageText,
                color = LimeGreen,
                icon = Icons.Filled.Home,
                onClick = { navController.navigate("home") }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(confirmTitle) },
            confirmButton = {
                TextButton(onClick = {
                    selectedFiles.forEach { it.delete() }
                    collageFiles = collageDir.listFiles()?.toList() ?: emptyList()
                    selectedFiles.clear()
                    showDeleteDialog = false
                }) {
                    Text(yesText, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(cancelText)
                }
            }
        )
    }

    if (showPreview && selectedImageForPreview != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = { Text(previewTitle) },
            text = {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageForPreview),
                    contentDescription = "Full Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 5f)
                )
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        selectedImageForPreview?.let { file ->
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                setType("image/*")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                setPackage("com.instagram.android")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        showPreview = false
                    }) {
                        Text(exportText, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = {
                        saveLauncher.launch(selectedImageForPreview?.name ?: "collage.jpg")
                        showPreview = false
                    }) {
                        Text(saveText, color = Color.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreview = false }) {
                    Text(closeText, color = Color.Black)
                }
            }
        )
    }
}
