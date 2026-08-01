package com.quietus.mixin;

import com.quietus.entity.monster.Paraboler;
import com.quietus.entity.projectiles.QuietusProjectile;
import com.quietus.tags.QuietusTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void keepBallisticProjectilesInSync(CallbackInfo ci) {
        Projectile projectile = (Projectile) (Object) this;
        if (!projectile.level().isClientSide()
                && (projectile instanceof QuietusProjectile || projectile.getOwner() instanceof Paraboler)) {
            // Curved flight magnifies small client prediction differences. Request
            // a movement update each tick so corrections cannot accumulate into a
            // visible vertical snap.
            projectile.needsSync = true;
        }
    }

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
