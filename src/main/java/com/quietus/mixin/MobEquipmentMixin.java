package com.quietus.mixin;

import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Mob.class)
public abstract class MobEquipmentMixin {
    @ModifyConstant(
            method = "populateDefaultEquipmentSlots",
            constant = @Constant(floatValue = 0.15F))
    private float quietus$increaseHostileArmorChance(float vanillaCoefficient) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Enemy) || !(mob.level() instanceof ServerLevel level)) {
            return vanillaCoefficient;
        }

        return WorldThreatSystem.armorChanceCoefficient(
                vanillaCoefficient, WorldThreatData.get(level).getThreat());
    }
}
