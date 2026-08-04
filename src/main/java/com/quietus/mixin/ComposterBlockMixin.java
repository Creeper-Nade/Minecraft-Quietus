package com.quietus.mixin;

import com.quietus.item.QuietusItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {
    @Redirect(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void quietus$consumeMoldAndReturnContainer(
            ItemStack stack,
            int amount,
            @Nullable LivingEntity owner,
            ItemStack usedStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        ItemStack remainder;
        if (stack.is(QuietusItems.MOLD_BUCKET.get())) {
            remainder = new ItemStack(Items.BUCKET);
        } else if (stack.is(QuietusItems.MOLD_BOWL.get())) {
            remainder = new ItemStack(Items.BOWL);
        } else {
            stack.consume(amount, owner);
            return;
        }

        if (player.hasInfiniteMaterials()) {
            return;
        }

        stack.shrink(amount);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, remainder);
        } else if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }
    }
}
