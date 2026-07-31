package com.quietus.event_listener;

import static com.quietus.Quietus.MODID;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = MODID)
public final class SpawnEvent {
    private static final int MIN_EXTRA_ZOMBIES = 2;
    private static final int MAX_EXTRA_ZOMBIES = 3;
    private static final int HERD_RADIUS = 6;
    private static final int HORIZONTAL_ATTEMPTS_PER_MEMBER = 12;
    private static final int[] VERTICAL_OFFSETS = {0, 1, -1, 2, -2, 3, -3};

    /** Prevents the members created here from recursively creating their own herds. */
    private static final ThreadLocal<Boolean> SPAWNING_HERD_MEMBER = ThreadLocal.withInitial(() -> false);

    private SpawnEvent() {
    }

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        if (SPAWNING_HERD_MEMBER.get() || event.getSpawnType() != EntitySpawnReason.NATURAL) {
            return;
        }

        EntityType<?> entityType = event.getEntity().getType();
        if (entityType != EntityType.ZOMBIE
                && entityType != EntityType.HUSK
                && entityType != EntityType.DROWNED
                && entityType != EntityType.ZOMBIE_VILLAGER) {
            return;
        }

        ServerLevelAccessor levelAccessor = event.getLevel();
        Level level = levelAccessor.getLevel();
        RandomSource random = level.getRandom();
        BlockPos origin = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        int extraCount = random.nextInt(MIN_EXTRA_ZOMBIES, MAX_EXTRA_ZOMBIES + 1);
        SpawnGroupData spawnData = event.getSpawnData();

        SPAWNING_HERD_MEMBER.set(true);
        try {
            for (int i = 0; i < extraCount; i++) {
                Optional<BlockPos> spawnPosition = findSpawnPosition(entityType, levelAccessor, origin, random);
                if (spawnPosition.isEmpty()) {
                    continue;
                }

                Entity createdEntity = entityType.create(level, EntitySpawnReason.NATURAL);
                if (!(createdEntity instanceof Mob herdMember)) {
                    continue;
                }

                BlockPos position = spawnPosition.get();
                herdMember.snapTo(
                        position.getX() + 0.5D,
                        position.getY(),
                        position.getZ() + 0.5D,
                        random.nextFloat() * 360.0F,
                        0.0F);
                SpawnGroupData updatedSpawnData = EventHooks.finalizeMobSpawn(
                        herdMember,
                        levelAccessor,
                        levelAccessor.getCurrentDifficultyAt(position),
                        EntitySpawnReason.NATURAL,
                        spawnData);
                if (herdMember.isSpawnCancelled()) {
                    herdMember.discard();
                    continue;
                }

                spawnData = updatedSpawnData;
                level.addFreshEntity(herdMember);
            }
        } finally {
            SPAWNING_HERD_MEMBER.set(false);
        }

        // Keep vanilla's group initialization in sync with the added herd members.
        event.setSpawnData(spawnData);
    }

    private static Optional<BlockPos> findSpawnPosition(
            EntityType<?> entityType,
            ServerLevelAccessor level,
            BlockPos origin,
            RandomSource random) {
        for (int attempt = 0; attempt < HORIZONTAL_ATTEMPTS_PER_MEMBER; attempt++) {
            int offsetX = random.nextInt(-HERD_RADIUS, HERD_RADIUS + 1);
            int offsetZ = random.nextInt(-HERD_RADIUS, HERD_RADIUS + 1);
            if (offsetX == 0 && offsetZ == 0) {
                continue;
            }

            for (int offsetY : VERTICAL_OFFSETS) {
                BlockPos candidate = origin.offset(offsetX, offsetY, offsetZ);
                if (SpawnPlacements.checkSpawnRules(
                                entityType, level, EntitySpawnReason.NATURAL, candidate, random)
                        && level.noCollision(entityType.getSpawnAABB(
                                candidate.getX() + 0.5D,
                                candidate.getY(),
                                candidate.getZ() + 0.5D))) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }
}
