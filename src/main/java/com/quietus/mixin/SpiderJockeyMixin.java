package com.quietus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import com.quietus.world.threat.SpawnDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Spider.class)
public abstract class SpiderJockeyMixin {
    @ModifyExpressionValue(
            method = "finalizeSpawn",
            at = @At(value = "CONSTANT", args = "intValue=100", ordinal = 0))
    private int quietus$increaseSpiderJockeyChance(int vanillaBound) {
        Spider spider = (Spider) (Object) this;
        if (spider.getSpawnType() != EntitySpawnReason.NATURAL
                || !(spider.level() instanceof ServerLevel level)) {
            return vanillaBound;
        }

        SpawnDiagnostics.recordJockeyRoll(SpawnDiagnostics.JockeyType.SPIDER);
        return WorldThreatSystem.spiderJockeyRollBound(WorldThreatData.get(level).getThreat());
    }

    /** Selects the rider independently, allowing modded skeleton variants to remain spider jockeys. */
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void quietus$selectSpiderJockeyVariant(
            ServerLevelAccessor levelAccessor,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData groupData,
            CallbackInfoReturnable<SpawnGroupData> callback) {
        Spider spider = (Spider) (Object) this;
        if (spawnReason != EntitySpawnReason.NATURAL
                || !(spider.getFirstPassenger() instanceof Skeleton vanillaRider)) {
            return;
        }

        ServerLevel level = levelAccessor.getLevel();
        WorldThreatData threat = WorldThreatData.get(level);
        EntityType<?> selectedType = WorldThreatSystem.selectSkeletonVariant(
                threat.getStage(), threat.getThreat(), level.getRandom());
        if (selectedType == EntityType.SKELETON) {
            return;
        }

        Entity created = selectedType.create(level, EntitySpawnReason.JOCKEY);
        if (!(created instanceof Mob replacement)) {
            return;
        }

        replacement.snapTo(spider.getX(), spider.getY(), spider.getZ(), spider.getYRot(), 0.0F);
        EventHooks.finalizeMobSpawn(
                replacement, levelAccessor, difficulty, EntitySpawnReason.JOCKEY, null);
        if (replacement.isSpawnCancelled()) {
            replacement.discard();
            return;
        }

        vanillaRider.stopRiding();
        if (replacement.startRiding(spider, false, false)) {
            vanillaRider.discard();
        } else {
            replacement.discard();
            vanillaRider.startRiding(spider, false, false);
        }
    }
}
