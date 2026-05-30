package com.quietus.mixin;

import com.quietus.util.QuietusGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(
            method = "dropEquipment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"
            ),
            cancellable = true
    )
    private void preventInventoryDrop(ServerLevel level, CallbackInfo ci) {
        //Player player = (Player) (Object) this;

        if (level.getGameRules().get(QuietusGameRules.FRAGMENT_SPAWNING))
        {
            ci.cancel(); // Prevent the dropAll() call
        }
    }

    @Inject(
            method = "getBaseExperienceReward",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true)
    private void modifyExperienceReward(ServerLevel level, CallbackInfoReturnable<Integer> cir) {
        if (level.getGameRules().get(QuietusGameRules.FRAGMENT_SPAWNING)) {
            cir.setReturnValue(0); // No experience drops if ghost mechanic is active
        }
    }

}
