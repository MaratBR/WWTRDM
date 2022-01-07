package com.maratb.minecraft.wwtrdm.mixins;

import com.maratb.minecraft.wwtrdm.GrindstoneUsedCriteria;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

@Mixin(GrindstoneScreenHandler.class)
public class DisenchantingEnchantedGoldenAppleMixin {
    @Shadow
    @Final
    @Mutable
    private Inventory result;

    @Shadow
    @Final
    @Mutable
    Inventory input;

    @Inject(at = @At("RETURN"), method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
    private void initGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        GrindstoneScreenHandler h = (GrindstoneScreenHandler) ((Object)this);
        Slot slot0 = h.getSlot(0);
        h.slots.set(0, new Slot(input, slot0.getIndex(), slot0.x, slot0.y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return slot0.canInsert(stack) || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
            }
        });
        h.slots.get(0).id = 0;

        Slot slot1 = h.getSlot(1);
        h.slots.set(1, new Slot(input, slot1.getIndex(), slot1.x, slot1.y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return slot1.canInsert(stack) || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
            }
        });
        h.slots.get(1).id = 1;

        Slot slot2 = h.getSlot(2);
        h.slots.set(2, new Slot(result, slot2.getIndex(), slot2.x, slot2.y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return slot2.canInsert(stack) || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
            }

            // not changed
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                final int appleItemStackIndex =
                        input.getStack(0).getItem() == Items.ENCHANTED_GOLDEN_APPLE ? 0 :
                        input.getStack(1).getItem() == Items.ENCHANTED_GOLDEN_APPLE ? 1 :
                        -1;

                context.run((world, pos) -> {
                    if (world instanceof ServerWorld) {
                        ExperienceOrbEntity.spawn((ServerWorld)world, Vec3d.ofCenter(pos), this.getExperience(world));
                        GrindstoneUsedCriteria.INSTANCE.trigger(
                                (ServerPlayerEntity) player,
                                input.getStack(appleItemStackIndex != -1 ? appleItemStackIndex : 0));
                    }

                    world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, pos, 0);
                });


                if (appleItemStackIndex != -1) {
                    if (input.getStack(appleItemStackIndex).getCount() == 1) {
                        input.setStack(appleItemStackIndex, ItemStack.EMPTY);
                    } else {
                        input.setStack(appleItemStackIndex, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, input.getStack(appleItemStackIndex).getCount() - 1));
                    }
                } else {
                    input.setStack(0, ItemStack.EMPTY);
                    input.setStack(1, ItemStack.EMPTY);
                }
            }

            // not changed
            private int getExperience(World world) {
                int ix = 0;
                int i = ix + this.getExperience(h.getSlot(0).getStack());
                i += this.getExperience(h.getSlot(1).getStack());
                if (i > 0) {
                    int j = (int)Math.ceil((double)i / 2.0D);
                    return j + world.random.nextInt(j);
                } else {
                    return 0;
                }
            }

            private int getExperience(ItemStack stack) {

                if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                    return 620;
                }

                // the following is the same code from original class
                int i = 0;
                Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);

                for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    Integer integer = entry.getValue();
                    if (!enchantment.isCursed()) {
                        i += enchantment.getMinPower(integer);
                    }
                }

                return i;
            }
        });
        h.slots.get(2).id = 2;
    }

    @Inject(at = @At("HEAD"), method = "updateResult()V", cancellable = true)
    private void updateResult(CallbackInfo ci) {
        GrindstoneScreenHandler h = (GrindstoneScreenHandler) ((Object)this);

        ItemStack is0 = input.getStack(0);
        ItemStack is1 = input.getStack(1);

        boolean onlyEnchantedGoldenApple =
                is0.getItem() == Items.ENCHANTED_GOLDEN_APPLE && is1.isEmpty() ||
                is1.getItem() == Items.ENCHANTED_GOLDEN_APPLE && is0.isEmpty() ||
                is0.getItem() == Items.ENCHANTED_GOLDEN_APPLE && is1.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
        if (onlyEnchantedGoldenApple) {
            this.result.setStack(0, new ItemStack(Items.GOLDEN_APPLE, 1));
            h.sendContentUpdates();
            ci.cancel();
        }

    }
}
