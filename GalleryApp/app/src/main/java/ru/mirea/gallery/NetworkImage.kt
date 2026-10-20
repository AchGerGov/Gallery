package ru.mirea.gallery

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

// Glide загружает и кэширует изображения. AndroidView связывает ImageView с Compose.
@Composable
fun NetworkImage(
    url: String,
    description: String,
    modifier: Modifier = Modifier,
    crop: Boolean = true
) {
    var attempt by remember(url) { mutableIntStateOf(0) }
    var isLoading by remember(url, attempt) { mutableStateOf(true) }
    var hasError by remember(url, attempt) { mutableStateOf(false) }
    // Новый ключ создаёт новый запрос после нажатия «Повторить».
    val requestKey = "$url|$attempt|$crop"

    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context -> ImageView(context) },
            modifier = Modifier.fillMaxSize(),
            onReset = null,
            onRelease = { view -> Glide.with(view).clear(view) },
            update = { view ->
                view.contentDescription = description
                if (view.tag != requestKey) {
                    view.tag = requestKey
                    view.scaleType = if (crop) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
                    val request = Glide.with(view)
                        .load(url)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?, target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (view.tag == requestKey) {
                                    isLoading = false
                                    hasError = true
                                }
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable, model: Any, target: Target<Drawable>?,
                                dataSource: DataSource, isFirstResource: Boolean
                            ): Boolean {
                                if (view.tag == requestKey) {
                                    isLoading = false
                                    hasError = false
                                }
                                return false
                            }
                        })
                    if (crop) request.centerCrop().into(view) else request.fitCenter().into(view)
                }
            }
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        }
        if (hasError) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Не удалось загрузить", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { attempt++ }) { Text("Повторить") }
            }
        }
    }
}
