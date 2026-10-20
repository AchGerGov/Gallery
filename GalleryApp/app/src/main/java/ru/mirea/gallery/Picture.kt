package ru.mirea.gallery

// Неизменяемая модель данных: аналог data class из лекции 4.
data class Picture(
    val id: Int,
    val title: String,
    val source: String = "Lorem Picsum"
) {
    val imageUrl: String get() = "https://picsum.photos/id/$id/800/600"
    val fullImageUrl: String get() = "https://picsum.photos/id/$id/1400/1050"
}

// В приложение включены только адреса. Сами изображения загружает Glide по HTTPS.
fun generateSamplePictures(): List<Picture> = listOf(
    Picture(10, "Лесное озеро"),
    Picture(15, "Горный поток"),
    Picture(16, "У моря"),
    Picture(28, "Среди деревьев"),
    Picture(29, "Вершины"),
    Picture(37, "Побережье"),
    Picture(54, "Далёкий горизонт"),
    Picture(57, "Тихий берег"),
    Picture(65, "В пути"),
    Picture(76, "Природные линии"),
    Picture(83, "Отражения"),
    Picture(110, "Открытый простор")
)
