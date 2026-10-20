package ru.mirea.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun GalleryScreen(modifier: Modifier = Modifier) {
    val samples = remember { generateSamplePictures() }
    // Сохраняем только идентификаторы. ArrayList<Int> поддерживается SavedInstanceState.
    var visibleIds by rememberSaveable {
        mutableStateOf(ArrayList(samples.take(6).map { it.id }))
    }
    var isGridLayout by rememberSaveable { mutableStateOf(true) }
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    val gallery = visibleIds.mapNotNull { id -> samples.find { it.id == id } }
    val nextPicture = samples.firstOrNull { it.id !in visibleIds }
    val selected = gallery.firstOrNull { it.id == selectedId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            nextPicture?.let { visibleIds = ArrayList(visibleIds + it.id) }
                        },
                        enabled = nextPicture != null,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text(if (nextPicture == null) "Все фото добавлены" else "+ Добавить фото")
                    }
                    TextButton(onClick = {
                        visibleIds = ArrayList(samples.take(6).map { it.id })
                        selectedId = null
                    }) { Text("Сбросить") }
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                Text(
                    "ЛИЧНАЯ КОЛЛЕКЦИЯ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text("Моменты", style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold)
                Text("Мир в фотографиях", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(selected = isGridLayout, onClick = { isGridLayout = true },
                        label = { Text("Сетка") })
                    FilterChip(selected = !isGridLayout, onClick = { isGridLayout = false },
                        label = { Text("Список") })
                    Spacer(Modifier.weight(1f))
                    Text("Фото: ${gallery.size}", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            when {
                gallery.isEmpty() -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Пока пусто", style = MaterialTheme.typography.headlineSmall)
                            Text("Добавьте первое фото кнопкой внизу.")
                        }
                    }
                }
                isGridLayout -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(144.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        gridItems(items = gallery, key = { it.id }) { picture ->
                            PictureCard(picture, onClick = { selectedId = picture.id })
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = gallery, key = { it.id }) { picture ->
                            PictureCard(picture, onClick = { selectedId = picture.id })
                        }
                    }
                }
            }
        }
    }
    selected?.let { picture ->
        PictureDetails(
            picture = picture,
            onDismiss = { selectedId = null },
            onDelete = {
                visibleIds = ArrayList(visibleIds.filterNot { it == picture.id })
                selectedId = null
            }
        )
    }
}

@Composable
private fun PictureCard(picture: Picture, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClickLabel = "Открыть фото", onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        NetworkImage(url = picture.imageUrl, description = picture.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        Column(Modifier.padding(12.dp)) {
            Text(picture.title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(picture.source, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PictureDetails(picture: Picture, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp)) {
            LazyColumn {
                item {
                    NetworkImage(url = picture.fullImageUrl, description = picture.title,
                        crop = false, modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f))
                    Column(Modifier.padding(20.dp)) {
                        Text(picture.title, style = MaterialTheme.typography.headlineSmall)
                        Text("Источник: ${picture.source}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = onDelete) {
                                Text("Удалить", color = MaterialTheme.colorScheme.error)
                            }
                            Button(onClick = onDismiss) { Text("Закрыть") }
                        }
                    }
                }
            }
        }
    }
}
