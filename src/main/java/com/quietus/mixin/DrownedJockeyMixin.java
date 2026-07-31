package com.quietus.mixin;

import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import com.quietus.world.threat.SpawnDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Drowned.class)
public abstract class DrownedJockeyMixin {
    @ModifyConstant(
            method = "finalizeSpawn",
            constant = @Constant(floatValue = 0.50F),
            expect = 1)
    private float quietus$increaseZombieNautilusJockeyChance(float vanillaChance) {
        Drowned drowned = (Drowned) (Object) this;
        if (drowned.getSpawnType() != EntitySpawnReason.NATURAL
                || !(drowned.level() instanceof ServerLevel level)) {
            return vanillaChance;
        }

        SpawnDiagnostics.recordJockeyRoll(SpawnDiagnostics.JockeyType.ZOMBIE_NAUTILUS);
        return WorldThreatSystem.zombieNautilusJockeyChance(WorldThreatData.get(level).getThreat());
    }
}
