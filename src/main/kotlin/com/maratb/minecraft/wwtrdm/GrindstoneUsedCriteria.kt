package com.maratb.minecraft.wwtrdm

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.item.ItemStack
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.EntityPredicate.Extended
import net.minecraft.predicate.entity.LocationPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object GrindstoneUsedCriteria : AbstractCriterion<GrindstoneUsedCriteria.Conditions>() {
    val ID = Identifier(WWTRDM.MOD_ID, "grindstone_used")

    fun trigger(player: ServerPlayerEntity, stack: ItemStack) {
        trigger(player) { conditions -> conditions.test(stack) }
    }

    class Conditions(player: Extended,
                     private val itemPredicate: ItemPredicate) : AbstractCriterionConditions(ID, player) {
        fun test(stack: ItemStack): Boolean = itemPredicate.test(stack)
    }

    override fun getId(): Identifier = ID

    override fun conditionsFromJson(
        obj: JsonObject?,
        playerPredicate: Extended?,
        predicateDeserializer: AdvancementEntityPredicateDeserializer?
    ): Conditions {
        val itemPredicate = ItemPredicate.fromJson(obj!!.get("anyItem"))
        return Conditions(playerPredicate!!, itemPredicate)
    }
}