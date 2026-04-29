package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.tags.QuietusTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public class ProjectileMixin {
    @Inject(
            method = "canHitEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCanHitEntity(Entity target, CallbackInfoReturnable<Boolean> cir) {
        // check if target has immunity tag
        if (target.getType().getTags().toList().contains(QuietusTags.Entity.IMMUNE_PROJECTILE_FROM_MOB)) {
            Entity owner = ((Projectile) (Object) this).getOwner();

            // if owner is not player, prevent hitting
            if (!(owner instanceof Player)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
