package dev.furq.holodisplays.config

import org.joml.Vector3f
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter

object JsonUtils {
    val jsonFilter = FileFilter { it.extension == "json" }

    fun parseVector3f(json: JsonReader): Vector3f {
        json.beginArray()
        val x = json.nextDouble().toFloat()
        val y = json.nextDouble().toFloat()
        val z = json.nextDouble().toFloat()
        json.endArray()
        return Vector3f(x, y, z)
    }

    fun parseStringList(json: JsonReader): List<String> {
        val result = mutableListOf<String>()
        json.beginArray()
        while (json.hasNext()) {
            result.add(json.nextString())
        }
        json.endArray()
        return result
    }

    fun writeVector3f(json: JsonWriter, name: String, vector: Vector3f) {
        json.name(name).beginArray()
        json.value(vector.x)
        json.value(vector.y)
        json.value(vector.z)
        json.endArray()
    }

    fun writeStringList(json: JsonWriter, name: String, list: List<String>) {
        json.name(name).beginArray()
        list.forEach { json.value(it) }
        json.endArray()
    }
}