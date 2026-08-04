package com.quietus.mixin;

import com.quietus.item.QuietusItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents automated composting from destroying containers that cannot be returned to a hopper. */
@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
public abstract class ComposterInputContainerMixin {
    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void quietus$keepMoldContainerInHopper(
            int slot,
            ItemStack stack,
            @Nullable Direction direction,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (stack.is(QuietusItems.MOLD_BUCKET.get()) || stack.is(QuietusItems.MOLD_BOWL.get())) {
            callback.setReturnValue(false);
        }
    }
}
