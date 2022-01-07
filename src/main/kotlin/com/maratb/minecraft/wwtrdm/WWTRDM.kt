package com.maratb.minecraft.wwtrdm
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.advancement.CriterionRegistry
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity

@Suppress("UNUSED")
object WWTRDM: ModInitializer {
    const val MOD_ID = "wwtrdm"

    override fun onInitialize() {
        CriterionRegistry.register(GrindstoneUsedCriteria)
    }
}