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

        fun build() = AnimationData(frames.toList(), interval)
    }
}

@Serializable
data class TransformAnimationData(
    val interval: Int = 20,
    val interpolation: String = "sine",
    val loop: Boolean = false,
    val scale: FloatFromTo? = null,
    val rotation: VecFromTo? = null,
    val translation: VecFromTo? = null,
) {
    fun hasProperties(): Boolean = scale != null || rotation != null || translation != null
}

interface TimingOverride {
    val interval: Int?
    val interpolation: String?
    val loop: Boolean?
}

@Serializable
data class FloatFromTo(
    val from: Float,
    val to: Float,
    override val interval: Int? = null,
    override val interpolation: String? = null,
    override val loop: Boolean? = null,
) : TimingOverride

@Serializable
data class VecFromTo(
    val from: List<Float> = emptyList(),
    val to: List<Float> = emptyList(),
    override val interval: Int? = null,
    override val interpolation: String? = null,
    override val loop: Boolean? = null,
) : TimingOverride