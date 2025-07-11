package com.minecraftquietus.quietus.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionStackMixin {

    @ModifyVariable(
            method = "<init>",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static Item.Properties modifyStackSize(Item.Properties properties) {
        // Set the stack size to 16 for potion items
        return properties.stacksTo(16);
    }
}
