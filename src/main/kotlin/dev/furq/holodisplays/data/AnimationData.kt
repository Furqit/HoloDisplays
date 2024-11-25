package dev.furq.holodisplays.data

data class AnimationData(
    val frames: List<Frame>,
    val interval: Int = 20,
) {
    data class Frame(
        val text: String,
    )
}