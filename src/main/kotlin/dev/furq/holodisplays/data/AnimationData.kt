package dev.furq.holodisplays.data

data class AnimationData(
    val frames: List<Frame>,
    val interval: Int = 20,
) {
    data class Frame(val text: String)

    class Builder {
        private var frames = mutableListOf<Frame>()
        var interval: Int = 20

        fun addFrame(text: String) {
            frames.add(Frame(text))
        }

        fun build() = AnimationData(frames, interval)
    }
}