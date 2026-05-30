package com.quietus.mixin;

import com.quietus.item.QuietusComponents;
import com.quietus.util.ItemStackUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {


    @SuppressWarnings("null")
    @Inject(
        method = "burn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", // Targets first isEmpty() check
            ordinal = 0 // First occurrence in the method
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void modifyDecayBeforeBurn(
            NonNullList<ItemStack> items, ItemStack inputItemStack, ItemStack result, CallbackInfo ci, ItemStack resultItemStack
    ) {
        // Only modify decay if components exist
        if (items.get(1).has(QuietusComponents.CAN_DECAY.get()) &&
            inputItemStack.has(QuietusComponents.CAN_DECAY.get())) {
            float new_fraction = Math.min(1.0f, 
                inputItemStack.get(QuietusComponents.CAN_DECAY.get())
                    .getDecayFraction(inputItemStack.get(QuietusComponents.DECAY.get())) + 0.2f
            );
            items.get(1).set(
                QuietusComponents.DECAY.get(),
                (int)Math.floor((1-new_fraction) * items.get(1).get(QuietusComponents.CAN_DECAY.get()).maxDecay())
            );
        }
    }
    /**
     * Updates:
     * 1. Vanilla 'burn' no longer contains an equality check; it assumes canBurn passed [cite: 379-381].
     * 2. We inject before 'grow' to merge decay components.
     */
    @Inject(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void burnOutputMergeDecay(
            NonNullList<ItemStack> items, ItemStack inputItemStack, ItemStack result, CallbackInfo ci, ItemStack resultItemStack
    ) {
        // If they are the same item but differ in components (like decay), merge them manually
        if (!ItemStack.isSameItemSameComponents(resultItemStack, result) &&
                ItemStackUtil.isSameItemSameComponentsExceptDecay(resultItemStack, result)) {

            if (result.has(QuietusComponents.CAN_DECAY.get()) && resultItemStack.has(QuietusComponents.CAN_DECAY.get())) {
                int totalCount = resultItemStack.getCount() + result.getCount();

                long avgDecay = Math.round(
                        ((double)resultItemStack.getOrDefault(QuietusComponents.DECAY.get(), 0) * resultItemStack.getCount() +
                                (double)result.getOrDefault(QuietusComponents.DECAY.get(), 0) * result.getCount())
                                / (double)totalCount
                );

                resultItemStack.set(QuietusComponents.DECAY.get(), (int)avgDecay);
            }
        }
    }

    @Redirect(
        method = "canBurn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    private static boolean redirectCanBurnComparison(ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.isSameItemSameComponentsExceptDecay(stack1, stack2);
    }

    /**
     * Updates:
     * 1. Target the 3-parameter overload used by Neoforge.
     */
    @Redirect(
            method = "setItem(ILnet/minecraft/world/item/ItemStack;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean redirectSetItemComparison(ItemStack oldStack, ItemStack newStack) {
        return ItemStackUtil.isSameItemSameComponentsExceptDecay(oldStack, newStack);
    }
}