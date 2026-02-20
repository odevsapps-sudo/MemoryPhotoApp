package com.odevs.photodiary.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.viewmodel.PhotoViewModel

@Composable
fun CollageResultScreen(
    navController: NavController,
    viewModel: PhotoViewModel = hiltViewModel()
) {
    val language = LanguageProvider.language
    val title = if (language == "hu") "Kollázs előnézet" else "Collage Preview"
    val noCollageText = if (language == "hu") "Nincs elérhető kollázs." else "No collages available."
    val backButton = if (language == "hu") "Vissza" else "Back"
    val errorLoading = if (language == "hu") "❌ Nem sikerült betölteni: " else "❌ Failed to load: "

    val generatedCollages by viewModel.generatedCollages.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(title)
        Spacer(modifier = Modifier.height(16.dp))

        if (generatedCollages.isNotEmpty()) {
            LazyColumn {
                items(generatedCollages) { file ->
                    val bitmap = BitmapFactory.decodeFile(file.path)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(8.dp)
                        )
                    } ?: Text("$errorLoading${file.name}")
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(noCollageText)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text(backButton)
                }
            }
        }
    }
}
