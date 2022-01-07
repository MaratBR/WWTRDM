package com.maratb.minecraft.wwtrdm.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {
    @Shadow
    @Final
    @Mutable
    private List<DamageRecord> recentDamage;

    @Inject(at = @At("HEAD"), method = "getDeathMessage")
    private void getDeathMessage(CallbackInfoReturnable<Text> cir) {
        if (!recentDamage.isEmpty()) {
            DamageRecord damageRecord2 = this.recentDamage.get(this.recentDamage.size() - 1);
            if (damageRecord2.getDamageSource() == DamageSource.OUT_OF_WORLD) {
                Entity entity = damageRecord2.getDamageSource().getAttacker();
                if (entity instanceof EnderDragonEntity && entity.world.getDimension().hasEnderDragonFight()) {
                    cir.setReturnValue(new TranslatableText("death.attack.ender_dragon.player_met_their_end"));
                }
            }
        }
    }
}
