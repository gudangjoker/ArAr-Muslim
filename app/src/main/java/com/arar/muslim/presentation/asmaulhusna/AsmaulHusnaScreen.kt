package com.arar.muslim.presentation.asmaulhusna

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arar.muslim.core.theme.IslamicGold
import com.arar.muslim.core.theme.IslamicGreenPrimary
import com.arar.muslim.core.theme.QuranFontFamily
import com.arar.muslim.domain.model.AsmaulHusna
import com.arar.muslim.presentation.common.NumberBadge

@Composable
fun AsmaulHusnaScreen(
    viewModel: AsmaulHusnaViewModel,
    modifier: Modifier = Modifier
) {
    val names by viewModel.namesList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedName by viewModel.selectedName.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = IslamicGreenPrimary,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Asmaul Husna",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
                Text(
                    text = "99 Nama-Nama Allah yang Maha Indah",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Cari nama Allah (e.g. Ar-Rahman, Pengasih)...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = IslamicGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = IslamicGold
                    ),
                    singleLine = true
                )
            }
        }

        // Grid 2 Kolom
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(names, key = { it.number }) { item ->
                AsmaulHusnaCard(
                    item = item,
                    onClick = { viewModel.selectName(item) }
                )
            }
        }
    }

    // Detail Dialog
    selectedName?.let { item ->
        AsmaulHusnaDetailDialog(
            item = item,
            onDismiss = { viewModel.selectName(null) }
        )
    }
}

@Composable
fun AsmaulHusnaCard(
    item: AsmaulHusna,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                NumberBadge(number = item.number, size = 28.dp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.arabic,
                fontFamily = QuranFontFamily,
                fontSize = 26.sp,
                color = IslamicGreenPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.latin,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = item.translationId,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun AsmaulHusnaDetailDialog(
    item: AsmaulHusna,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberBadge(number = item.number)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.arabic,
                    fontFamily = QuranFontFamily,
                    fontSize = 36.sp,
                    color = IslamicGreenPrimary
                )
                Text(
                    text = item.latin,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Divider(color = IslamicGold.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Arti & Makna:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = IslamicGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.meaningDetail,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
            ) {
                Text("Tutup")
            }
        }
    )
}
