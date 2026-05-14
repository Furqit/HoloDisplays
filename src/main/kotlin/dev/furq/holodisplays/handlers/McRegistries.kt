package dev.furq.holodisplays.handlers

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
//?} else {
/*import net.minecraft.resources.ResourceLocation
*///?}

//~ if >=1.21.11 'ResourceLocation' -> 'Identifier'
typealias McId = Identifier

internal object McRegistries {

    fun parseId(raw: String?): McId? =
        //~ if >=1.21.11 'ResourceLocation' -> 'Identifier'
        raw?.let { Identifier.tryParse(it) }

    fun itemExists(id: McId?) = exists(BuiltInRegistries.ITEM, id)
    fun blockExists(id: McId?) = exists(BuiltInRegistries.BLOCK, id)
    fun entityTypeExists(id: McId?) = exists(BuiltInRegistries.ENTITY_TYPE, id)

    private fun <T : Any> exists(registry: Registry<T>, id: McId?): Boolean {
        if (id == null) return false
        //? if >=1.21.11 {
        return registry.containsKey(ResourceKey.create(registry.key(), id))
        //?} else {
        /*return registry.containsKey(id)
        *///?}
    }

    fun getItemOrThrow(id: String) = getOrThrow(BuiltInRegistries.ITEM, id, "item")
    fun getBlockOrThrow(id: String) = getOrThrow(BuiltInRegistries.BLOCK, id, "block")
    fun getEntityTypeOrThrow(id: String) = getOrThrow(BuiltInRegistries.ENTITY_TYPE, id, "entity")

    private fun <T : Any> getOrThrow(registry: Registry<T>, displayId: String, typeName: String): T {
        val id = parseId(displayId) ?: throw DisplayException("Invalid $typeName identifier: $displayId")

        //? if >=1.21.11 {
        return registry.getValue(ResourceKey.create(registry.key(), id)) ?: throw DisplayException("Unknown $typeName: $displayId")
        //?} else if >=1.21.2 {
        /*return registry.get(id).orElseThrow { DisplayException("Unknown $typeName: $displayId") }.value()
        *///?} else {
        /*return if (registry.containsKey(id)) registry.get(id)!! else throw DisplayException("Unknown $typeName: $displayId")
        
        *///?}
    }

    fun defaultBlockState(block: Block): BlockState = block.defaultBlockState()
    fun stateDefinition(block: Block) = block.getStateDefinition()
}