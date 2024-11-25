package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.mixin.BlockDisplayEntityAccessor
import dev.furq.holodisplays.mixin.DisplayEntityAccessor
import dev.furq.holodisplays.mixin.ItemDisplayEntityAccessor
import dev.furq.holodisplays.mixin.TextDisplayEntityAccessor
import dev.furq.holodisplays.utils.TextProcessor
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import java.util.*
import kotlin.experimental.or

object PacketHandler {
    private const val INITIAL_ENTITY_ID = -1
    private var nextEntityId = INITIAL_ENTITY_ID
    private val recycledIds = mutableSetOf<Int>()
    private val entityIds = mutableMapOf<UUID, MutableMap<String, MutableMap<String, Int>>>()

    fun clearAllHolograms() {
        entityIds.clear()
        nextEntityId = INITIAL_ENTITY_ID
        recycledIds.clear()
    }

    fun spawnDisplayEntity(
        player: ServerPlayerEntity,
        name: String,
        line: HologramData.DisplayLine,
        display: DisplayData,
        position: Vec3d,
        lineIndex: Int,
        hologram: HologramData,
    ) {
        val entityId = getNextEntityId()
        val displayRef = "${line.getReference()}:$lineIndex"

        entityIds.getOrPut(player.uuid) { mutableMapOf() }
            .getOrPut(name) { mutableMapOf() }[displayRef] = entityId

        val offsetPosition = position.add(display.displayType.offset.run { Vec3d(x, y, z) })

        player.networkHandler.run {
            sendPacket(createSpawnPacket(entityId, offsetPosition, display.displayType, hologram))
            sendDisplayMetadata(player, entityId, display, hologram)
        }
    }

    fun destroyHologram(player: ServerPlayerEntity, name: String) {
        val playerHolograms = entityIds[player.uuid] ?: return
        val hologramIds = playerHolograms[name] ?: return
        val idsToDestroy = hologramIds.values.toList()

        if (idsToDestroy.isNotEmpty()) {
            player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList(idsToDestroy)))
            recycledIds.addAll(idsToDestroy)
        }

        playerHolograms.remove(name)
        if (playerHolograms.isEmpty()) {
            entityIds.remove(player.uuid)
        }
    }

    private fun updateEntityMetadata(
        player: ServerPlayerEntity,
        name: String,
        displayRef: String,
        lineIndex: Int,
        metadata: List<DataTracker.SerializedEntry<*>>,
    ) {
        val uniqueRef = "$displayRef:$lineIndex"
        val entityId = entityIds[player.uuid]?.get(name)?.get(uniqueRef) ?: return
        player.networkHandler.sendPacket(EntityTrackerUpdateS2CPacket(entityId, metadata))
    }

    private fun getNextEntityId(): Int =
        recycledIds.firstOrNull()?.also { recycledIds.remove(it) } ?: --nextEntityId

    fun getEntityId(
        player: ServerPlayerEntity,
        name: String,
        displayRef: String,
        lineIndex: Int,
    ): Int? {
        return entityIds[player.uuid]?.get(name)?.get("$displayRef:$lineIndex")
    }

    fun updateTextMetadata(
        player: ServerPlayerEntity,
        name: String,
        displayRef: String,
        lineIndex: Int,
        text: Text,
    ) {
        val entries = mutableListOf<DataTracker.SerializedEntry<*>>().apply {
            add(createEntry(TextDisplayEntityAccessor.getText(), text))
        }
        updateEntityMetadata(player, name, displayRef, lineIndex, entries)
    }

    private fun createSpawnPacket(
        entityId: Int,
        position: Vec3d,
        displayType: DisplayData.DisplayType,
        hologram: HologramData,
    ): EntitySpawnS2CPacket {
        val entityType = when (displayType) {
            is DisplayData.DisplayType.Text -> EntityType.TEXT_DISPLAY
            is DisplayData.DisplayType.Item -> EntityType.ITEM_DISPLAY
            is DisplayData.DisplayType.Block -> EntityType.BLOCK_DISPLAY
        }

        val pitch = displayType.rotation?.pitch ?: hologram.rotation.pitch
        val yaw = displayType.rotation?.yaw ?: hologram.rotation.yaw

        return EntitySpawnS2CPacket(
            entityId,
            UUID.randomUUID(),
            position.x, position.y, position.z,
            pitch, yaw,
            entityType,
            0,
            Vec3d.ZERO,
            0.0
        )
    }

    private fun sendDisplayMetadata(
        player: ServerPlayerEntity,
        entityId: Int,
        display: DisplayData,
        hologram: HologramData,
    ) {
        val entries = mutableListOf<DataTracker.SerializedEntry<*>>().apply {
            addCommonProperties(display, hologram)

            when (val type = display.displayType) {
                is DisplayData.DisplayType.Text -> addTextProperties(type, player)
                is DisplayData.DisplayType.Item -> addItemProperties(type)
                is DisplayData.DisplayType.Block -> addBlockProperties(type, hologram)
            }
        }

        player.networkHandler.sendPacket(EntityTrackerUpdateS2CPacket(entityId, entries))
    }

    private fun MutableList<DataTracker.SerializedEntry<*>>.addCommonProperties(
        display: DisplayData,
        hologram: HologramData,
    ) {
        val scale = (display.displayType.scale ?: hologram.scale).let { s -> Vector3f(s, s, s) }
        add(createEntry(DisplayEntityAccessor.getScale(), scale))
        add(
            createEntry(
                DisplayEntityAccessor.getBillboard(),
                (display.displayType.billboardMode ?: hologram.billboardMode).ordinal.toByte()
            )
        )
    }

    private fun MutableList<DataTracker.SerializedEntry<*>>.addTextProperties(
        display: DisplayData.DisplayType.Text,
        player: ServerPlayerEntity,
    ) {
        val processedText = Text.literal(display.lines.joinToString("\n")).let { text ->
            TextProcessor.processText(text.string, player)
        }
        add(createEntry(TextDisplayEntityAccessor.getText(), processedText))

        display.lineWidth?.let { add(createEntry(TextDisplayEntityAccessor.getLineWidth(), it)) }

        display.backgroundColor?.let { bgColor ->
            if (bgColor.matches(Regex("^[0-9A-Fa-f]{2}[0-9A-Fa-f]{6}$"))) {
                val finalColor = bgColor.substring(0, 2).toInt(16).shl(24) or
                        bgColor.substring(2).toInt(16)
                add(createEntry(TextDisplayEntityAccessor.getBackground(), finalColor))
            }
        }

        display.textOpacity?.let {
            add(
                createEntry(
                    TextDisplayEntityAccessor.getTextOpacity(),
                    ((it.coerceIn(1, 100) / 100.0) * 255).toInt().toByte()
                )
            )
        }

        var flags: Byte = 0
        if (display.shadow == true) flags = flags or TextDisplayEntityAccessor.getShadowFlag()
        if (display.seeThrough == true) flags = flags or TextDisplayEntityAccessor.getSeeThroughFlag()
        if (display.backgroundColor == null) flags = flags or TextDisplayEntityAccessor.getDefaultBackgroundFlag()
        when (display.alignment) {
            DisplayData.TextAlignment.LEFT -> flags = flags or TextDisplayEntityAccessor.getLeftAlignmentFlag()
            DisplayData.TextAlignment.RIGHT -> flags = flags or TextDisplayEntityAccessor.getRightAlignmentFlag()
            else -> {}
        }
        add(createEntry(TextDisplayEntityAccessor.getTextDisplayFlags(), flags))
    }

    private fun MutableList<DataTracker.SerializedEntry<*>>.addItemProperties(display: DisplayData.DisplayType.Item) {
        val itemStack = ItemStack(
            Registries.ITEM.get(Identifier.tryParse(display.id) ?: return),
        )
        add(createEntry(ItemDisplayEntityAccessor.getItem(), itemStack))

        val displayType = when (display.itemDisplayType.lowercase()) {
            "none" -> 0
            "thirdperson_lefthand" -> 1
            "thirdperson_righthand" -> 2
            "firstperson_lefthand" -> 3
            "firstperson_righthand" -> 4
            "head" -> 5
            "gui" -> 6
            "ground" -> 7
            "fixed" -> 8
            else -> 7
        }
        add(createEntry(ItemDisplayEntityAccessor.getItemDisplay(), displayType.toByte()))
    }

    private fun MutableList<DataTracker.SerializedEntry<*>>.addBlockProperties(
        display: DisplayData.DisplayType.Block,
        hologram: HologramData,
    ) {
        val block = Registries.BLOCK.get(Identifier.tryParse(display.id) ?: return)
        add(createEntry(BlockDisplayEntityAccessor.getBlockState(), block.defaultState))

        val scale = display.scale ?: hologram.scale
        val translation = -0.5f * scale
        add(
            createEntry(
                DisplayEntityAccessor.getTranslation(),
                Vector3f(translation, translation, translation)
            )
        )
    }

    private fun <T> createEntry(
        trackedData: TrackedData<T>,
        value: T,
    ): DataTracker.SerializedEntry<T> =
        DataTracker.SerializedEntry(trackedData.id, trackedData.dataType, value)

    fun updateDisplayMetadata(
        player: ServerPlayerEntity,
        name: String,
        displayRef: String,
        lineIndex: Int,
        display: DisplayData,
        hologram: HologramData,
    ) {
        val entries = mutableListOf<DataTracker.SerializedEntry<*>>().apply {
            addCommonProperties(display, hologram)

            when (val type = display.displayType) {
                is DisplayData.DisplayType.Text -> addTextProperties(type, player)
                is DisplayData.DisplayType.Item -> addItemProperties(type)
                is DisplayData.DisplayType.Block -> addBlockProperties(type, hologram)
            }
        }
        updateEntityMetadata(player, name, displayRef, lineIndex, entries)
    }
}