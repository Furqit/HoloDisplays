package dev.furq.holodisplays.data.common

data class Position(
    val world: String = "minecraft:overworld",
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
)

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f,
)

data class Scale(
    val x: Float = 1.0f,
    val y: Float = 1.0f,
    val z: Float = 1.0f,
)

data class Offset(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
)