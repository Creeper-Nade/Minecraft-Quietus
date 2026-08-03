package com.quietus.mixin;

import com.quietus.util.RangedAmmoCurios;
import com.quietus.util.QuietusGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(
            method = "getProjectile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;"
            ),
            cancellable = true
    )
    private void useCuriosAmmoBeforeInventory(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        ProjectileWeaponItem projectileWeapon = (ProjectileWeaponItem) weapon.getItem();
        RangedAmmoCurios.findAmmo(player)
                .map(result -> result.stack())
                // Treat the Curios ammo slot like a held/offhand ammo source. This matters for
                // crossbows: vanilla accepts rockets as held ammo but not from the inventory scan.
                .filter(projectileWeapon.getSupportedHeldProjectiles(weapon))
                .map(ammo -> CommonHooks.getProjectile(player, weapon, ammo))
                .ifPresent(cir::setReturnValue);
    }

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

        if (level.getGameRules().get(QuietusGameRules.PLAYER_FRAGMENT_ON_DEATH))
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
        if (level.getGameRules().get(QuietusGameRules.PLAYER_FRAGMENT_ON_DEATH)) {
            cir.setReturnValue(0); // No experience drops if ghost mechanic is active
        }
    }

}
