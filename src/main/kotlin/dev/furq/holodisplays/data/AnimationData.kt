package dev.furq.holodisplays.data

import kotlinx.serialization.Serializable

@Serializable
data class AnimationData(
    val frames: List<String>,
    val interval: Int = 20,
) {
    class Builder {
        private var frames = mutableListOf<String>()
        var interval: Int = 20

        fun build() = AnimationData(frames, interval)
    }
}