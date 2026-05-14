package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.*
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.mixin.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.phys.Vec3
import org.joml.Math.toRadians
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import kotlin.experimental.or

object PacketHandler {
    private const val INITIAL_ENTITY_ID = -1
    private var nextEntityId = INITIAL_ENTITY_ID
    private val recycledIds = mutableSetOf<Int>()
    private val entityIds = mutableMapOf<UUID, Object2IntOpenHashMap<String>>()
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

    private fun <T : Any> createEntry(trackedData: EntityDataAccessor<T>, value: T): SynchedEntityData.DataValue<*> =
        SynchedEntityData.DataValue.create(trackedData, value)

    private fun calculateTranslation(display: BaseDisplay, offset: Vector3f, scale: Vector3f, isFromApi: Boolean): Vector3f = Vector3f(offset).apply {
        if (display is BlockDisplay && !isFromApi) add(-0.5f * scale.x, -0.5f * scale.y, -0.5f * scale.z)
    }

    private fun getCompositeKey(hologramName: String, displayRef: String): String = "$hologramName/$displayRef"

    fun resetEntityTracking() {
        entityIds.clear()
        nextEntityId = INITIAL_ENTITY_ID
        recycledIds.clear()
    }

    fun spawnDisplayEntity(
        player: ServerPlayer,
        hologramName: String,
        line: HologramData.DisplayLine,
        displayData: DisplayData,
        position: Vector3f,
        lineIndex: Int,
        hologram: HologramData,
        packetConsumer: (Packet<*>) -> Unit = { player.connection.send(it) }
    ) = safeCall {
        val entityId = getNextEntityId()
        val displayRef = "${line.name}:$lineIndex"
        val compositeKey = getCompositeKey(hologramName, displayRef)

        entityIds.getOrPut(player.uuid) { Object2IntOpenHashMap() }
            .put(compositeKey, entityId)

        val display = displayData.type
        if (display is EntityDisplay) {
            val translation = line.offset
            val effectivePos = position.add(translation)
            val rotation = display.rotation ?: hologram.rotation

            packetConsumer(createSpawnPacket(entityId, effectivePos, display, rotation.x, rotation.y, rotation.y.toDouble()))
        } else {
            packetConsumer(createSpawnPacket(entityId, position, displayData.type))
        }
        sendDisplayMetadata(player, entityId, displayData, hologram, line, packetConsumer)
    }

    fun destroyDisplayEntity(player: ServerPlayer, hologramName: String) {
        val playerEntities = entityIds[player.uuid] ?: return
        val prefix = "$hologramName/"
        val iterator = playerEntities.object2IntEntrySet().iterator()
        val idsToDestroy = IntArrayList()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.startsWith(prefix)) {
                idsToDestroy.add(entry.intValue)
                iterator.remove()
            }
        }

        if (!idsToDestroy.isEmpty) {
            player.connection.send(ClientboundRemoveEntitiesPacket(idsToDestroy))
            recycledIds.addAll(idsToDestroy)
        }

        if (playerEntities.isEmpty()) {
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
    ): ClientboundAddEntityPacket = safeCall {
        val entityType = when (display) {
            is TextDisplay -> EntityType.TEXT_DISPLAY
            is ItemDisplay -> EntityType.ITEM_DISPLAY
            is BlockDisplay -> EntityType.BLOCK_DISPLAY
            is EntityDisplay -> McRegistries.getEntityTypeOrThrow(display.id)
            else -> throw DisplayException("Unknown display type")
        }

        val pos = Vec3(position)
        ClientboundAddEntityPacket(
            entityId, UUID.randomUUID(), pos.x, pos.y, pos.z,
            pitch, yaw, entityType, 0, Vec3.ZERO, headYaw
        )
    } ?: throw DisplayException("Failed to create spawn packet")

    private fun sendDisplayMetadata(
        player: ServerPlayer,
        entityId: Int,
        displayData: DisplayData,
        hologram: HologramData,
        line: HologramData.DisplayLine,
        packetConsumer: (Packet<*>) -> Unit
    ) = safeCall {
        val entries = buildDisplayMetadata(displayData, hologram, line, player)
        packetConsumer(ClientboundSetEntityDataPacket(entityId, entries))

        if (displayData.type is EntityDisplay) {
            val scaleAttr = AttributeInstance(Attributes.SCALE) { }
            scaleAttr.baseValue = displayData.type.scale?.x()?.toDouble() ?: 1.0
            val scalePacket = ClientboundUpdateAttributesPacket(entityId, listOf(scaleAttr))
            packetConsumer(scalePacket)
        }
    }

    fun updateDisplayMetadata(
        player: ServerPlayer,
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
        player: ServerPlayer,
        hologramName: String,
        displayId: String,
        lineIndex: Int,
        text: Component,
    ) {
        val entries = buildList {
            add(createEntry(TextDisplayEntityAccessor.getText(), text))
        }
        val displayRef = "$displayId:$lineIndex"
        updateEntityMetadata(player, hologramName, displayRef, entries)
    }

    private fun updateEntityMetadata(
        player: ServerPlayer,
        hologramName: String,
        displayRef: String,
        metadata: List<SynchedEntityData.DataValue<*>>,
    ) = safeCall {
        val compositeKey = getCompositeKey(hologramName, displayRef)
        val entityId = entityIds[player.uuid]?.getInt(compositeKey) ?: return@safeCall
        if (!entityIds[player.uuid]!!.containsKey(compositeKey)) return@safeCall

        player.connection.send(ClientboundSetEntityDataPacket(entityId, metadata))
    }

    private fun buildDisplayMetadata(
        displayData: DisplayData,
        hologram: HologramData,
        line: HologramData.DisplayLine,
        player: ServerPlayer,
    ): List<SynchedEntityData.DataValue<*>> = safeCall(default = emptyList()) {
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
    ): List<SynchedEntityData.DataValue<*>> = buildList {
        val display = displayData.type

        val scale = display.scale ?: hologram.scale
        add(createEntry(DisplayAccessor.getScale(), Vector3f(scale.x, scale.y, scale.z)))

        val billboardOrdinal = (display.billboardMode ?: hologram.billboardMode).ordinal.toByte()
        add(createEntry(DisplayAccessor.getBillboard(), billboardOrdinal))

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
        add(createEntry(DisplayAccessor.getLeftRotation(), leftRotation))

        val rightRotation = display.rightRotation ?: hologram.rightRotation
        if (rightRotation != null) {
            add(createEntry(DisplayAccessor.getRightRotation(), rightRotation))
        }

        val isFromApi = HoloDisplaysAPIInternal.getDisplay(line.name) != null
        val translation = calculateTranslation(display, line.offset, scale, isFromApi)
        add(createEntry(DisplayAccessor.getTranslation(), translation))

    }

    private fun textDisplayProperties(
        display: TextDisplay,
        player: ServerPlayer,
    ): List<SynchedEntityData.DataValue<*>> = safeCall(default = emptyList()) {
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

    private fun itemDisplayProperties(display: ItemDisplay): List<SynchedEntityData.DataValue<*>> = safeCall(default = emptyList()) {
        buildList {
            val item = McRegistries.getItemOrThrow(display.id)

            val itemStack = ItemStack(item, 1)
            display.customModelData?.also { cmd ->
                itemStack.set(
                    DataComponents.CUSTOM_MODEL_DATA,
                    //~ if >1.21.3 'cmd' -> 'listOf(cmd.toFloat()), listOf(), listOf(), listOf()'
                    CustomModelData(listOf(cmd.toFloat()), listOf(), listOf(), listOf())
                )
            }

            add(createEntry(ItemDisplayEntityAccessor.getItem(), itemStack))

            val displayType = itemDisplayTypeMap[display.itemDisplayType.lowercase()] ?: 7.toByte()
            add(createEntry(ItemDisplayEntityAccessor.getItemDisplay(), displayType))
        }
    } ?: emptyList()

    private fun blockDisplayProperties(display: BlockDisplay): List<SynchedEntityData.DataValue<*>> = safeCall(default = emptyList()) {
        buildList {
            val block = McRegistries.getBlockOrThrow(display.id)

            var blockState = McRegistries.defaultBlockState(block)
            if (display.properties.isNotEmpty()) {
                val stateDefinition = McRegistries.stateDefinition(block)
                display.properties.forEach { (key, value) ->
                    val property = stateDefinition.getProperty(key)
                    if (property != null) {
                        blockState = BlockStateUtil.withParsedProperty(blockState, property, value)
                    }
                }
            }
            add(createEntry(BlockDisplayEntityAccessor.getBlockState(), blockState))
        }
    } ?: emptyList()

    private fun entityDisplayProperties(display: EntityDisplay): List<SynchedEntityData.DataValue<*>> = buildList {
        display.glow?.also { glow ->
            add(createEntry(EntityAccessor.getFlags(), if (glow) (1 shl 6).toByte() else 0))
        }

        display.pose?.also {
            add(createEntry(EntityAccessor.getPose(), it))
        }
    }
}