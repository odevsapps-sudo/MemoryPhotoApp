package com.odevs.photodiary.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.odevs.photodiary.graphics.SoftYellow
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollagePreviewScreen(
    navController: NavController,
    startIndex: Int,
    viewModel: PhotoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val language = LanguageProvider.language

    val backText = if (language == "hu") "⬅ Vissza" else "⬅ Back"
    val saveText = if (language == "hu") "💾 Mentés" else "💾 Save"
    val instaText = if (language == "hu") "📤 Instagram" else "📤 Instagram"
    val emptyText = if (language == "hu") "Nincs elérhető kollázs. Kérlek, készíts egyet." else "No collage available. Go back and generate one."

    val collageDir = File(context.filesDir, "collages")
    val collageFiles = remember { collageDir.listFiles()?.toList()?.reversed() ?: emptyList() }
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { collageFiles.size })

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri: Uri? ->
        uri?.let { destinationUri ->
            collageFiles.getOrNull(pagerState.currentPage)?.inputStream()?.use { input ->
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    input.copyTo(output)
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
        if (collageFiles.isEmpty()) {
            Text(emptyText)
            Button(onClick = { navController.popBackStack() }) {
                Text(backText)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fill,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page: Int ->
                Image(
                    painter = rememberAsyncImagePainter(collageFiles[page]),
                    contentDescription = "Collage Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text(backText)
                }
                Button(onClick = {
                    val file = collageFiles[pagerState.currentPage]
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        setPackage("com.instagram.android")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                }) {
                    Text(instaText)
                }
                Button(onClick = {
                    saveLauncher.launch("collage_${System.currentTimeMillis()}.jpg")
                }) {
                    Text(saveText)
                }
            }
        }
    }
}
