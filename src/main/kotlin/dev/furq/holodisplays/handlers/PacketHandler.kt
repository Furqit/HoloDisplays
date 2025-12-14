package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.*
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.mixin.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.block.BlockState
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.CustomModelDataComponent
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.property.Property
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.joml.Math.toRadians
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import kotlin.experimental.or

object PacketHandler {
    private const val INITIAL_ENTITY_ID = -1
    private var nextEntityId = INITIAL_ENTITY_ID
    private val recycledIds = mutableSetOf<Int>()
    private val entityIds = mutableMapOf<UUID, MutableMap<String, MutableMap<String, Int>>>()
    private val hexColorPattern = "^[0-9A-Fa-f]{2}[0-9A-Fa-f]{6}$".toRegex()

    private val itemDisplayTypeMap = mapOf(
        "none" to 0.toByte(),
        "thirdperson_lefthand" to 1.toByte(),
        "thirdperson_righthand" to 2.toByte(),
        "firstperson_lefthand" to 3.toByte(),
        "firstperson_righthand" to 4.toByte(),
        "head" to 5.toByte(),
        "gui" to 6.toByte(),
        "ground" to 7.toByte(),
        "fixed" to 8.toByte()
    )

    private fun getNextEntityId(): Int = recycledIds.firstOrNull()?.also(recycledIds::remove) ?: --nextEntityId

    private fun <T> createEntry(trackedData: TrackedData<T>, value: T):
            DataTracker.SerializedEntry<T> = DataTracker.SerializedEntry(trackedData.id, trackedData.dataType, value)

    private fun calculateTranslation(display: BaseDisplay, offset: Vector3f, scale: Vector3f, isFromApi: Boolean): Vector3f = Vector3f(offset).apply {
        if (display is BlockDisplay && !isFromApi) add(-0.5f * scale.x, -0.5f * scale.y, -0.5f * scale.z)
    }

    fun resetEntityTracking() {
        entityIds.clear()
        nextEntityId = INITIAL_ENTITY_ID
        recycledIds.clear()
    }

    fun spawnDisplayEntity(
        player: ServerPlayerEntity,
        hologramName: String,
        line: HologramData.DisplayLine,
        displayData: DisplayData,
        position: Vector3f,
        lineIndex: Int,
        hologram: HologramData,
    ) = safeCall {
        val entityId = getNextEntityId()
        val displayRef = "${line.name}:$lineIndex"

        entityIds.getOrPut(player.uuid, ::mutableMapOf)
            .getOrPut(hologramName, ::mutableMapOf)[displayRef] = entityId

        val display = displayData.type
        player.networkHandler.run {
            if (display is EntityDisplay) {
                val translation = line.offset
                val effectivePos = position.add(translation)
                val rotation = display.rotation ?: hologram.rotation

                sendPacket(createSpawnPacket(entityId, effectivePos, display, rotation.x, rotation.y, rotation.y.toDouble()))
            } else {
                sendPacket(createSpawnPacket(entityId, position, displayData.type))
            }
            sendDisplayMetadata(player, entityId, displayData, hologram, line)
        }
    }

    fun destroyDisplayEntity(player: ServerPlayerEntity, hologramName: String) {
        val playerHolograms = entityIds[player.uuid] ?: return
        val hologramIds = playerHolograms[hologramName] ?: return
        val idsToDestroy = hologramIds.values.toList()

        if (idsToDestroy.isNotEmpty()) {
            player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList(idsToDestroy)))
            recycledIds.addAll(idsToDestroy)
        }

        playerHolograms.remove(hologramName)
        if (playerHolograms.isEmpty()) {
            entityIds.remove(player.uuid)
        }
    }

    private fun createSpawnPacket(
        entityId: Int,
        position: Vector3f,
        display: BaseDisplay,
        pitch: Float = 0f,
        yaw: Float = 0f,
        headYaw: Double = 0.0
    ): EntitySpawnS2CPacket = safeCall {
        val entityType = when (display) {
            is TextDisplay -> EntityType.TEXT_DISPLAY
            is ItemDisplay -> EntityType.ITEM_DISPLAY
            is BlockDisplay -> EntityType.BLOCK_DISPLAY
            is EntityDisplay -> Registries.ENTITY_TYPE.get(Identifier.tryParse(display.id))
            else -> throw DisplayException("Unknown display type")
        }

        val pos = Vec3d(position)
        EntitySpawnS2CPacket(
            entityId, UUID.randomUUID(), pos.x, pos.y, pos.z,
            pitch, yaw, entityType, 0, Vec3d.ZERO, headYaw
        )
    } ?: throw DisplayException("Failed to create spawn packet")

    private fun sendDisplayMetadata(
        player: ServerPlayerEntity,
        entityId: Int,
        displayData: DisplayData,
        hologram: HologramData,
        line: HologramData.DisplayLine,
    ) = safeCall {
        val entries = buildDisplayMetadata(displayData, hologram, line, player)
        player.networkHandler.sendPacket(EntityTrackerUpdateS2CPacket(entityId, entries))

        if (displayData.type is EntityDisplay) {
            val scaleAttr = EntityAttributeInstance(/*? if 1.20.6 {*/ EntityAttributes.GENERIC_SCALE /*?}*//*? if >=1.21.3 {*/ /*EntityAttributes.SCALE *//*?}*/) { }
            scaleAttr.baseValue = displayData.type.scale?.x()?.toDouble() ?: 1.0
            val scalePacket = EntityAttributesS2CPacket(entityId, listOf(scaleAttr))
            player.networkHandler.sendPacket(scalePacket)
        }
    }

    fun updateDisplayMetadata(
        player: ServerPlayerEntity,
        hologramName: String,
        displayId: String,
        lineIndex: Int,
        displayData: DisplayData,
        hologram: HologramData,
    ) = safeCall {
        val line = hologram.displays.getOrNull(lineIndex) ?: return@safeCall
        val entries = buildDisplayMetadata(displayData, hologram, line, player)
        val displayRef = "$displayId:$lineIndex"
        updateEntityMetadata(player, hologramName, displayRef, entries)
    }

    fun updateTextMetadata(
        player: ServerPlayerEntity,
        hologramName: String,
        displayId: String,
        lineIndex: Int,
        text: Text,
    ) {
        val entries = buildList {
            add(createEntry(TextDisplayEntityAccessor.getText(), text))
        }
        val displayRef = "$displayId:$lineIndex"
        updateEntityMetadata(player, hologramName, displayRef, entries)
    }

    private fun updateEntityMetadata(
        player: ServerPlayerEntity,
        hologramName: String,
        displayRef: String,
        metadata: List<DataTracker.SerializedEntry<*>>,
    ) = safeCall {
        val entityId = entityIds[player.uuid]?.get(hologramName)?.get(displayRef) ?: return@safeCall
        player.networkHandler.sendPacket(EntityTrackerUpdateS2CPacket(entityId, metadata))
    }

    private fun buildDisplayMetadata(
        displayData: DisplayData,
        hologram: HologramData,
        line: HologramData.DisplayLine,
        player: ServerPlayerEntity,
    ): List<DataTracker.SerializedEntry<*>> = safeCall(default = emptyList()) {
        buildList {
            val display = displayData.type
            val commonProps = commonDisplayProperties(displayData, hologram, line)
            if (display !is EntityDisplay) addAll(commonProps)

            when (display) {
                is TextDisplay -> addAll(textDisplayProperties(display, player))
                is ItemDisplay -> addAll(itemDisplayProperties(display))
                is BlockDisplay -> addAll(blockDisplayProperties(display))
                is EntityDisplay -> addAll(entityDisplayProperties(display))
            }
        }
    } ?: emptyList()

    private fun commonDisplayProperties(
        displayData: DisplayData,
        hologram: HologramData,
        line: HologramData.DisplayLine,
    ): List<DataTracker.SerializedEntry<*>> = buildList {
        val display = displayData.type

        val scale = display.scale ?: hologram.scale
        add(createEntry(DisplayEntityAccessor.getScale(), Vector3f(scale.x, scale.y, scale.z)))

        val billboardOrdinal = (display.billboardMode ?: hologram.billboardMode).ordinal.toByte()
        add(createEntry(DisplayEntityAccessor.getBillboard(), billboardOrdinal))

        val rawLeftRotation = display.leftRotation ?: hologram.leftRotation
        val leftRotation = if (rawLeftRotation != null) {
            rawLeftRotation
        } else {
            val rotation = display.rotation ?: hologram.rotation
            Quaternionf()
                .rotateY(toRadians(rotation.y))
                .rotateX(toRadians(rotation.x))
                .rotateZ(toRadians(rotation.z))
        }
        add(createEntry(DisplayEntityAccessor.getLeftRotation(), leftRotation))

        val rightRotation = display.rightRotation ?: hologram.rightRotation
        if (rightRotation != null) {
            add(createEntry(DisplayEntityAccessor.getRightRotation(), rightRotation))
        }

        val isFromApi = HoloDisplaysAPIInternal.getDisplay(line.name) != null
        val translation = calculateTranslation(display, line.offset, scale, isFromApi)
        add(createEntry(DisplayEntityAccessor.getTranslation(), translation))

    }

    private fun textDisplayProperties(
        display: TextDisplay,
        player: ServerPlayerEntity,
    ): List<DataTracker.SerializedEntry<*>> = safeCall(default = emptyList()) {
        buildList {
            val processedText = TickHandler.processText(display.getText(), player)
            add(createEntry(TextDisplayEntityAccessor.getText(), processedText))

            display.lineWidth?.also { add(createEntry(TextDisplayEntityAccessor.getLineWidth(), it)) }

            display.backgroundColor
                ?.takeIf { it.matches(hexColorPattern) }
                ?.also { bgColor ->
                    val finalColor = bgColor.take(2).toInt(16).shl(24) or bgColor.substring(2).toInt(16)
                    add(createEntry(TextDisplayEntityAccessor.getBackground(), finalColor))
                }

            display.textOpacity?.also {
                val opacity = ((it.coerceIn(1, 100) / 100.0) * 255).toInt().toByte()
                add(createEntry(TextDisplayEntityAccessor.getTextOpacity(), opacity))
            }

            var flags: Byte = 0
            if (display.shadow == true) flags = flags or TextDisplayEntityAccessor.getShadowFlag()
            if (display.seeThrough == true) flags = flags or TextDisplayEntityAccessor.getSeeThroughFlag()
            if (display.backgroundColor == null) flags = flags or TextDisplayEntityAccessor.getDefaultBackgroundFlag()
            when (display.alignment) {
                TextDisplay.TextAlignment.LEFT -> flags = flags or TextDisplayEntityAccessor.getLeftAlignmentFlag()
                TextDisplay.TextAlignment.RIGHT -> flags = flags or TextDisplayEntityAccessor.getRightAlignmentFlag()
                else -> {}
            }
            add(createEntry(TextDisplayEntityAccessor.getTextDisplayFlags(), flags))
        }
    } ?: emptyList()

    private fun itemDisplayProperties(display: ItemDisplay): List<DataTracker.SerializedEntry<*>> = safeCall(default = emptyList()) {
        buildList {
            val item = Identifier.tryParse(display.id)?.let(Registries.ITEM::get)
                ?: throw DisplayException("Invalid item identifier: ${display.id}")

            val itemStack = ItemStack(item)
            display.customModelData?.also { cmd ->
                itemStack.set(
                    DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(/*? if <=1.21.3 {*/cmd/*?}*//*? if >1.21.3 {*//*listOf(cmd.toFloat()), listOf(), listOf(), listOf()*//*?}*/)
                )
            }

            add(createEntry(ItemDisplayEntityAccessor.getItem(), itemStack))

            val displayType = itemDisplayTypeMap[display.itemDisplayType.lowercase()] ?: 7.toByte()
            add(createEntry(ItemDisplayEntityAccessor.getItemDisplay(), displayType))
        }
    } ?: emptyList()

    private fun blockDisplayProperties(display: BlockDisplay): List<DataTracker.SerializedEntry<*>> = safeCall(default = emptyList()) {
        buildList {
            val block = Identifier.tryParse(display.id)?.let(Registries.BLOCK::get)
                ?: throw DisplayException("Invalid block identifier: ${display.id}")

            var blockState = block.defaultState
            if (display.properties.isNotEmpty()) {
                val stateManager = block.stateManager
                display.properties.forEach { (key, value) ->
                    val property = stateManager.getProperty(key)
                    if (property != null) {
                        blockState = withProperty(blockState, property, value)
                    }
                }
            }
            add(createEntry(BlockDisplayEntityAccessor.getBlockState(), blockState))
        }
    } ?: emptyList()

    private fun <T : Comparable<T>> withProperty(state: BlockState, property: Property<T>, valueString: String): BlockState {
        return property.parse(valueString).map { value -> state.with(property, value) }.orElse(state)
    }

    private fun entityDisplayProperties(display: EntityDisplay): List<DataTracker.SerializedEntry<*>> = buildList {
        display.glow?.also { glow ->
            add(createEntry(EntityAccessor.getFlags(), if (glow) (1 shl 6).toByte() else 0))
        }

        display.pose?.also {
            add(createEntry(EntityAccessor.getPose(), it))
        }
    }
}