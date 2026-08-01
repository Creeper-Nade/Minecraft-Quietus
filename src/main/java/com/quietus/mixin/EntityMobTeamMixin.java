package com.quietus.mixin;

import com.quietus.entity.team.MobTeamManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMobTeamMixin {
    @Inject(method = "considersEntityAsAlly", at = @At("HEAD"), cancellable = true)
    private void quietus$considerMobTeamAsAllies(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (MobTeamManager.INSTANCE.areAllies((Entity) (Object) this, other)) {
            cir.setReturnValue(true);
        }
    }
}
