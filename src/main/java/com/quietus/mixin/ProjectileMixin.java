package com.quietus.mixin;

import com.quietus.combat.VolleyBalancedProjectile;
import com.quietus.entity.monster.Paraboler;
import com.quietus.entity.projectiles.QuietusProjectile;
import com.quietus.tags.QuietusTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin implements VolleyBalancedProjectile {
    @Unique
    private static final String QUIETUS_VOLLEY_DAMAGE_SCALE_TAG = "QuietusVolleyDamageScale";
    @Unique
    private static final String QUIETUS_VOLLEY_KNOCKBACK_SCALE_TAG = "QuietusVolleyKnockbackScale";
    @Unique
    private static final String QUIETUS_VOLLEY_SIZE_TAG = "QuietusVolleySize";

    @Unique
    private float quietus$volleyDamageScale = 1.0F;
    @Unique
    private float quietus$volleyKnockbackScale = 1.0F;
    @Unique
    private int quietus$volleySize = 1;

    @Override
    public float quietus$getVolleyDamageScale() {
        return this.quietus$volleyDamageScale;
    }

    @Override
    public void quietus$setVolleyDamageScale(float scale) {
        this.quietus$volleyDamageScale = scale;
    }

    @Override
    public float quietus$getVolleyKnockbackScale() {
        return this.quietus$volleyKnockbackScale;
    }

    @Override
    public void quietus$setVolleyKnockbackScale(float scale) {
        this.quietus$volleyKnockbackScale = scale;
    }

    @Override
    public int quietus$getVolleySize() {
        return this.quietus$volleySize;
    }

    @Override
    public void quietus$setVolleySize(int size) {
        this.quietus$volleySize = size;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void quietus$saveVolleyBalance(ValueOutput output, CallbackInfo ci) {
        if (this.quietus$volleySize > 1) {
            output.putInt(QUIETUS_VOLLEY_SIZE_TAG, this.quietus$volleySize);
            output.putFloat(QUIETUS_VOLLEY_DAMAGE_SCALE_TAG, this.quietus$volleyDamageScale);
            output.putFloat(QUIETUS_VOLLEY_KNOCKBACK_SCALE_TAG, this.quietus$volleyKnockbackScale);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void quietus$loadVolleyBalance(ValueInput input, CallbackInfo ci) {
        this.quietus$volleySize = Math.max(1, input.getIntOr(QUIETUS_VOLLEY_SIZE_TAG, 1));
        this.quietus$volleyDamageScale = input.getFloatOr(QUIETUS_VOLLEY_DAMAGE_SCALE_TAG, 1.0F);
        this.quietus$volleyKnockbackScale = input.getFloatOr(QUIETUS_VOLLEY_KNOCKBACK_SCALE_TAG, 1.0F);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void quietus$copyVolleyBalance(Entity oldEntity, CallbackInfo ci) {
        if (oldEntity instanceof VolleyBalancedProjectile oldProjectile) {
            this.quietus$volleySize = oldProjectile.quietus$getVolleySize();
            this.quietus$volleyDamageScale = oldProjectile.quietus$getVolleyDamageScale();
            this.quietus$volleyKnockbackScale = oldProjectile.quietus$getVolleyKnockbackScale();
        }
    }

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
