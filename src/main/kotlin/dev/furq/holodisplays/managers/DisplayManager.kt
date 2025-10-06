package dev.furq.holodisplays.managers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.*
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.handlers.DisplayHandler.DisplayProperty.*
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.utils.ConditionEvaluator
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import org.joml.Vector3f
import net.minecraft.entity.EntityPose as MinecraftEntityPose

object DisplayManager {

    private inline fun requireDisplayExists(
        name: String,
        source: ServerCommandSource,
        action: () -> Unit
    ) {
        if (DisplayConfig.exists(name)) action()
        else FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
    }

    private inline fun requireDisplayNew(
        name: String,
        source: ServerCommandSource,
        action: () -> Unit
    ) {
        if (!DisplayConfig.exists(name)) action()
        else FeedbackManager.send(source, FeedbackType.DISPLAY_EXISTS, "name" to name)
    }

    private fun updateProperty(
        name: String,
        source: ServerCommandSource,
        property: DisplayHandler.DisplayProperty,
        feedbackType: FeedbackType,
        vararg details: Pair<String, Any>
    ) = requireDisplayExists(name, source) {
        DisplayHandler.updateDisplayProperty(name, property)
        FeedbackManager.send(source, feedbackType, *details)
    }

    private fun createDisplay(name: String, display: BaseDisplay, type: String, source: ServerCommandSource): Boolean {
        var created = false
        requireDisplayNew(name, source) {
            DisplayConfig.saveDisplay(name, DisplayData(display))
            FeedbackManager.send(source, FeedbackType.DISPLAY_CREATED, "type" to type, "name" to name)
            created = true
        }
        return created
    }

    fun createTextDisplay(name: String, text: String, source: ServerCommandSource): Boolean =
        createDisplay(name, TextDisplay(mutableListOf(text)), "text", source)

    fun createItemDisplay(name: String, itemId: String, source: ServerCommandSource): Boolean {
        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            FeedbackManager.send(source, FeedbackType.INVALID_ITEM)
            return false
        }

        return createDisplay(name, ItemDisplay(id = fullItemId), "item", source)
    }

    fun createBlockDisplay(name: String, blockId: String, source: ServerCommandSource): Boolean {
        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            FeedbackManager.send(source, FeedbackType.INVALID_BLOCK)
            return false
        }

        return createDisplay(name, BlockDisplay(id = fullBlockId), "block", source)
    }

    fun createEntityDisplay(name: String, entityId: String, source: ServerCommandSource): Boolean {
        val fullEntityId = if (!entityId.contains(":")) "minecraft:$entityId" else entityId
        val entityIdentifier = Identifier.tryParse(fullEntityId)

        if (entityIdentifier == null || !Registries.ENTITY_TYPE.containsId(entityIdentifier)) {
            FeedbackManager.send(source, FeedbackType.INVALID_ENTITY)
            return false
        }

        return createDisplay(name, EntityDisplay(id = fullEntityId), "entity", source)
    }

    fun deleteDisplay(name: String, source: ServerCommandSource) = requireDisplayExists(name, source) {
        HologramConfig.getHolograms()
            .filterValues { hologram -> hologram.displays.any { it.name == name } }
            .forEach { (hologramName, hologram) ->
                val indicesToRemove = hologram.displays.mapIndexedNotNull { index, displayLine -> index.takeIf { displayLine.name == name } }
                indicesToRemove.reversed().forEach { index ->
                    HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.RemoveLine(index))
                }
            }

        DisplayConfig.deleteDisplay(name)
        FeedbackManager.send(source, FeedbackType.DISPLAY_DELETED, "name" to name)
    }

    fun updateScale(name: String, scale: Vector3f?, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            val newScale = scale ?: Vector3f(1f)

            if (scale != null && (newScale.x < 0.1f || newScale.y < 0.1f || newScale.z < 0.1f)) {
                FeedbackManager.send(source, FeedbackType.INVALID_SCALE)
                return
            }

            updateProperty(name, source, Scale(newScale),
                FeedbackType.SCALE_UPDATED, *FeedbackManager.formatVector3f(newScale)
            )
        }
    }

    fun updateBillboard(name: String, billboard: String?, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            val newMode = if (billboard == null) {
                BillboardMode.CENTER
            } else {
                try {
                    BillboardMode.valueOf(billboard.uppercase())
                } catch (_: IllegalArgumentException) {
                    FeedbackManager.send(source, FeedbackType.INVALID_BILLBOARD)
                    return
                }
            }

            updateProperty(name, source, BillboardMode(newMode),
                FeedbackType.BILLBOARD_UPDATED, "mode" to (billboard?.lowercase() ?: "center")
            )
        }
    }

    fun updateRotation(name: String, pitch: Float?, yaw: Float?, roll: Float?, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            val rotation = if (pitch == null || yaw == null || roll == null) Vector3f() else Vector3f(pitch, yaw, roll)

            if (listOf(rotation.x, rotation.y, rotation.z).any { it !in -180f..180f }) {
                FeedbackManager.send(source, FeedbackType.INVALID_ROTATION)
                return
            }

            updateProperty(name, source, Rotation(rotation),
                FeedbackType.ROTATION_UPDATED, *FeedbackManager.formatRotation(rotation.x, rotation.y, rotation.z))
        }
    }

    fun updateBackground(name: String, color: String?, opacity: Int?, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            if (color != null) {
                if (!color.matches(Regex("^[0-9A-Fa-f]{6}$"))) {
                    FeedbackManager.send(source, FeedbackType.INVALID_COLOR)
                    return
                }

                val opacityHex = ((opacity?.coerceIn(0, 100) ?: (100 / 100.0 * 255))).toInt()
                    .toString(16)
                    .padStart(2, '0')
                    .uppercase()

                updateProperty(name, source, TextBackgroundColor("$opacityHex$color"),
                    FeedbackType.BACKGROUND_UPDATED, "color" to color, "opacity" to opacity!!)
            } else {
                updateProperty(name, source, TextBackgroundColor(null), FeedbackType.BACKGROUND_UPDATED)
            }
        }
    }

    fun updateTextOpacity(name: String, opacity: Int, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            if (opacity !in 1..100) {
                FeedbackManager.send(source, FeedbackType.INVALID_TEXT_OPACITY)
                return
            }

            updateProperty(name, source, TextOpacity(opacity), FeedbackType.OPACITY_UPDATED, "opacity" to opacity)
        }
    }

    fun updateShadow(name: String, shadow: Boolean, source: ServerCommandSource) =
        updateProperty(name, source, TextShadow(shadow), FeedbackType.DISPLAY_UPDATED, "detail" to "shadow ${if (shadow) "enabled" else "disabled"}")

    fun updateAlignment(name: String, alignment: String, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            val textAlignment = try {
                TextDisplay.TextAlignment.valueOf(alignment.uppercase())
            } catch (_: IllegalArgumentException) {
                FeedbackManager.send(source, FeedbackType.INVALID_ALIGNMENT)
                return
            }

            updateProperty(name, source, TextAlignment(textAlignment), FeedbackType.DISPLAY_UPDATED, "detail" to "text alignment set to ${alignment.lowercase()}")
        }
    }

    fun updateItemDisplayType(name: String, type: String, source: ServerCommandSource) =
        updateProperty(name, source, ItemDisplayType(type), FeedbackType.DISPLAY_UPDATED, "detail" to "item display type set to $type")

    fun updateCustomModelData(name: String, customModelData: Int?, source: ServerCommandSource) =
        updateProperty(name, source, ItemCustomModelData(customModelData), FeedbackType.DISPLAY_UPDATED, "detail" to "custom model data set to ${customModelData ?: "none"}")

    fun updateCondition(name: String, condition: String?, source: ServerCommandSource) {
        requireDisplayExists(name, source) {
            if (condition != null && ConditionEvaluator.parseCondition(condition) == null) {
                FeedbackManager.send(source, FeedbackType.INVALID_CONDITION)
                return
            }

            updateProperty(name, source, ConditionalPlaceholder(condition), FeedbackType.DISPLAY_UPDATED, "detail" to "condition set to ${condition ?: "none"}")
        }
    }

    fun updateLineWidth(displayName: String, width: Int, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is TextDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "text")
                return
            }

            updateProperty(displayName, source, TextLineWidth(width), FeedbackType.LINE_WIDTH_UPDATED, "width" to width.toString())
        }
    }

    fun updateSeeThrough(displayName: String, seeThrough: Boolean, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is TextDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "text")
                return
            }

            updateProperty(displayName, source, TextSeeThrough(seeThrough), FeedbackType.SEE_THROUGH_UPDATED, "enabled" to seeThrough.toString())
        }
    }

    fun updateItemId(displayName: String, itemId: String, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is ItemDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "item")
                return
            }

            updateProperty(displayName, source, ItemId(itemId), FeedbackType.ITEM_ID_UPDATED, "id" to itemId)
        }
    }

    fun updateBlockId(displayName: String, blockId: String, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is BlockDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "block")
                return
            }

            updateProperty(displayName, source, BlockId(blockId), FeedbackType.BLOCK_ID_UPDATED, "id" to blockId)
        }
    }

    fun updateEntityId(displayName: String, entityId: String, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is EntityDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "entity")
                return
            }

            updateProperty(displayName, source, EntityId(entityId), FeedbackType.ENTITY_ID_UPDATED, "id" to entityId)
        }
    }

    fun updateEntityGlow(displayName: String, glow: Boolean, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is EntityDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "entity")
                return
            }

            updateProperty(displayName, source, EntityGlow(glow), FeedbackType.DISPLAY_UPDATED, "detail" to "glow ${if (glow) "enabled" else "disabled"}")
        }
    }

    fun updateEntityPose(displayName: String, pose: MinecraftEntityPose?, source: ServerCommandSource) {
        requireDisplayExists(displayName, source) {
            val display = DisplayConfig.getDisplay(displayName)
            if (display?.type !is EntityDisplay) {
                FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "entity")
                return
            }

            updateProperty(displayName, source, EntityPose(pose), FeedbackType.DISPLAY_UPDATED)
        }
    }
}