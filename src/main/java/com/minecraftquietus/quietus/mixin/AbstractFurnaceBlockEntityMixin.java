package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.util.ItemStackUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
        RegistryAccess registryAccess,
        @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe,
        SingleRecipeInput recipeInput,
        NonNullList<ItemStack> items,
        int maxStackSize,
        CallbackInfoReturnable<Boolean> cir,
        ItemStack itemstack, // Input stack (slot 0)
        ItemStack itemstack1, // Output stack (assembled from recipe)
        ItemStack itemstack2  // Current output slot (slot 2)
    ) {
        // Only modify decay if components exist
        if (itemstack1.has(QuietusComponents.CAN_DECAY.get()) && 
            itemstack.has(QuietusComponents.CAN_DECAY.get())) {
            float new_fraction = Math.min(1.0f, 
                itemstack.get(QuietusComponents.CAN_DECAY.get())
                    .getDecayFraction(itemstack.get(QuietusComponents.DECAY.get())) + 0.2f
            );
            itemstack1.set(
                QuietusComponents.DECAY.get(),
                (int)Math.floor((1-new_fraction) * itemstack1.get(QuietusComponents.CAN_DECAY.get()).maxDecay())
            );
        }
    }
    @Inject(
        method = "burn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private static void burnOutputMergeDecay(
        RegistryAccess registryAccess,
        @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe,
        SingleRecipeInput recipeInput,
        NonNullList<ItemStack> items,
        int maxStackSize,
        CallbackInfoReturnable<Boolean> cir,
        ItemStack itemstack, // stored reactant stack
        ItemStack itemstack1, // result stack from recipe of reactant stack
        ItemStack itemstack2 // stored result stack
    ) {
        if (!itemstack2.isEmpty() && !ItemStack.isSameItemSameComponents(itemstack2, itemstack1) && // after if vanilla comparison failed, try modded check regardless of decay component
            ItemStackUtil.isSameItemSameComponentsExceptDecay(itemstack2, itemstack1)) {
            itemstack2.grow(itemstack1.getCount());
            if (itemstack1.has(QuietusComponents.CAN_DECAY.get()) && itemstack2.has(QuietusComponents.CAN_DECAY.get())) {
                itemstack2.set(
                    QuietusComponents.DECAY.get(), 
                    (int)(Math.round(
                        (double)(itemstack2.getOrDefault(QuietusComponents.DECAY.get(), 0)*itemstack2.getCount() + itemstack1.getOrDefault(QuietusComponents.DECAY.get(), 0)*itemstack1.getCount()) 
                        / (double)(itemstack2.getCount() + itemstack1.getCount())))
                ); // stack sizes do not need to be checked; neoforge has fixed respecting max stack size in canBurn method
            }
            /* reactant (itemstack) will be then removed by 1 in vanilla code */
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

    @Redirect(
        method = "setItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    private static boolean redirectSetItemComparison(ItemStack itemstack, ItemStack stack) {
        return ItemStackUtil.isSameItemSameComponentsExceptDecay(itemstack, stack);
    }
}