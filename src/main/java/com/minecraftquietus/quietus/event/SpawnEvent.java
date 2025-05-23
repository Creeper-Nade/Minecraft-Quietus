package com.minecraftquietus.quietus.event;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;

import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;

import org.slf4j.Logger;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
public class SpawnEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        Entity entity = event.getEntity();
        ServerLevelAccessor levelAccessor = event.getLevel();
        Level level = levelAccessor.getLevel();
        RandomSource random = level.random;
        Vec3 pos = new Vec3(event.getX(), event.getY(), event.getZ());
        Vec3i pos_i = new Vec3i((int)event.getX(), (int)event.getY(), (int)event.getZ());
        EntitySpawnReason spawnReason = event.getSpawnType();
        
        /* TODO: maybe serialize maxExtraZombies a codec (named maxExtraSpawn global for every EntityType)? */
        if (spawnReason == EntitySpawnReason.NATURAL) { // ONLY natural spawning interferred
            if (entity.getType() == EntityType.ZOMBIE) { // zombies
                float random_magic_number = random.nextFloat();
                int maxExtraZombies = 3;
                int extra_amount = random.nextInt(0, maxExtraZombies+1);
                if (random_magic_number < (float)(1f/(float)(maxExtraZombies-extra_amount+1))) { // greater extra spawns increases chance of spawning allowed inversely proportional, vice versa; plus one to avoid division by zero
                    for (int i = 0; i < extra_amount; i++) {
                        Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
                        Optional<Vec3> spawn_position = checkValidSpawnLocationWithVanillaOffset(zombie.getType(), levelAccessor, spawnReason, pos_i, random);
                        if (spawn_position.isPresent()) {
                            zombie.setPos(spawn_position.get());
                            level.addFreshEntity(zombie);
                        }
                    }
                }
                else if (random_magic_number >= (float)(0.5f+extra_amount/2/maxExtraZombies)) {
                    Optional<? extends Entity> found_entity_wrapped = findRandomEntityInDistance(50.0d, (LivingEntity)entity, ((LivingEntity)entity).getClass(), level, random);
                    if (found_entity_wrapped.isPresent()) {
                        Vec3 found_entity_position = found_entity_wrapped.get().position();
                        Vec3i found_entity_position_i = new Vec3i((int)found_entity_position.x, (int)found_entity_position.y, (int)found_entity_position.z);
                        Optional<Vec3> target_position = checkValidSpawnLocationWithVanillaOffset(entity.getType(), levelAccessor, spawnReason, found_entity_position_i, random);
                        if (target_position.isPresent()) {
                            entity.snapTo(target_position.get());
                        }
                    }
                }
                else {
                    event.setSpawnCancelled(true);
                }
            }
            if (entity.getType() == EntityType.HUSK) { // husks
                float random_magic_number = random.nextFloat();
                int maxExtraZombies = 3;
                int extra_amount = random.nextInt(0, maxExtraZombies+1);
                if (random_magic_number < (float)(1f/(float)(maxExtraZombies-extra_amount+1))) { // greater extra spawns increases chance of spawning allowed inversely proportional, vice versa; plus one to avoid division by zero
                    for (int i = 0; i < extra_amount; i++) {
                        Husk zombie = new Husk(EntityType.HUSK, level);
                        Optional<Vec3> spawn_position = checkValidSpawnLocationWithVanillaOffset(zombie.getType(), levelAccessor, spawnReason, pos_i, random);
                        if (spawn_position.isPresent()) {
                            zombie.setPos(spawn_position.get());
                            level.addFreshEntity(zombie);
                        }
                    }
                }
                else if (random_magic_number >= (float)(0.5f+extra_amount/2/maxExtraZombies)) {
                    Optional<? extends Entity> found_entity_wrapped = findRandomEntityInDistance(20.0d, (LivingEntity)entity, ((LivingEntity)entity).getClass(), level, random);
                    if (found_entity_wrapped.isPresent()) {
                        Vec3 found_entity_position = found_entity_wrapped.get().position();
                        Vec3i found_entity_position_i = new Vec3i((int)found_entity_position.x, (int)found_entity_position.y, (int)found_entity_position.z);
                        Optional<Vec3> target_position = checkValidSpawnLocationWithVanillaOffset(entity.getType(), levelAccessor, spawnReason, found_entity_position_i, random);
                        if (target_position.isPresent()) {
                            entity.snapTo(target_position.get());
                        } else {
                            event.setSpawnCancelled(true);
                        }
                    } else {
                        event.setSpawnCancelled(true);
                    }
                }
                else {
                    event.setSpawnCancelled(true);
                }
            }
            if (entity.getType() == EntityType.ZOMBIE_VILLAGER) { // zombie villagers
                float random_magic_number = random.nextFloat();
                int maxExtraZombies = 3;
                int extra_amount = random.nextInt(0, maxExtraZombies+1);
                if (random_magic_number < (float)(1f/(float)(maxExtraZombies-extra_amount+1))) { // greater extra spawns increases chance of spawning allowed inversely proportional, vice versa; plus one to avoid division by zero
                    for (int i = 0; i < extra_amount; i++) {
                        ZombieVillager zombie = new ZombieVillager(EntityType.ZOMBIE_VILLAGER, level);
                        Optional<Vec3> spawn_position = checkValidSpawnLocationWithVanillaOffset(zombie.getType(), levelAccessor, spawnReason, pos_i, random);
                        if (spawn_position.isPresent()) {
                            zombie.setPos(spawn_position.get());
                            level.addFreshEntity(zombie);
                        }
                    }
                }
                else if (random_magic_number >= (float)(0.5f+extra_amount/2/maxExtraZombies)) {
                    Optional<? extends Entity> found_entity_wrapped = findRandomEntityInDistance(20.0d, (LivingEntity)entity, ((LivingEntity)entity).getClass(), level, random);
                    if (found_entity_wrapped.isPresent()) {
                        Vec3 found_entity_position = found_entity_wrapped.get().position();
                        Vec3i found_entity_position_i = new Vec3i((int)found_entity_position.x, (int)found_entity_position.y, (int)found_entity_position.z);
                        Optional<Vec3> target_position = checkValidSpawnLocationWithVanillaOffset(entity.getType(), levelAccessor, spawnReason, found_entity_position_i, random);
                        if (target_position.isPresent()) {
                            entity.snapTo(target_position.get());
                        } else {
                            event.setSpawnCancelled(true);
                        }
                    } else {
                        event.setSpawnCancelled(true);
                    }
                }
                else {
                    event.setSpawnCancelled(true);
                }
            }
        }
    }
    /**
     * 
     * @param distance max absolute distance between found entity and the source entity
     * @param sourceEntity source {@link LivingEntity}
     * @param mobClass target {@link LivingEntity} {@link Class}
     * @param level the specified {@link Level} 
     * @param random a {@link RandomSource}
     * @return the found {@link Entity} if found any, else empty wrapper
     */
    private static Optional<Entity> findRandomEntityInDistance(double distance, LivingEntity sourceEntity, Class<? extends LivingEntity> mobClass, Level level, RandomSource random) {
        if (level.getServer() == null) {return Optional.empty();}
        if (level.getServer().getLevel(level.dimension()) == null) {return Optional.empty();}
        List<? extends Entity> entities_in_distance = level.getServer().getLevel(level.dimension()).getNearbyEntities(mobClass, TargetingConditions.forNonCombat(), sourceEntity, new AABB(sourceEntity.position().x-distance, sourceEntity.position().y-distance, sourceEntity.position().z-distance, sourceEntity.position().x+distance, sourceEntity.position().y+distance, sourceEntity.position().z+distance));
        if (entities_in_distance.isEmpty()) {return Optional.empty();}
        List<Entity> entities_in_absolute_distance = new ArrayList<>();
        for (Entity entity : entities_in_distance) {
            Vec3 entity_position = entity.position();
            if (entity_position.distanceToSqr(entity_position) <= distance*distance) {
                entities_in_absolute_distance.add(entity);
            }
        }
        Entity foundEntity = entities_in_absolute_distance.get(random.nextInt(0,entities_in_absolute_distance.size()));
        return Optional.of(foundEntity);
    }
    /**
     * Finds a position after altering X and Z values by maximum of a specified offset. <br>
     * This runs 3 trials, likewise {@link NaturalSpawner#spawnCategoryForPosition}
     * 
     * @param offset the integer value of maximum offset
     * @param entityType the {@link EntityType} to check for as registered in spawning rules
     * @param entitySpawnReason the {@link EntitySpawnReason} used for part of predicate to check spawn location validity
     * @param level the {@link Level} where is being checked
     * @param pos the {@link Vec3i} center position of offset within this level
     * @param random a {@link RandomSource} random source
     * @return the found {@link Vec3} position if a position is found, wrapped nullable. If no valid spawn position is found within three trials, return empty wrapper.
     */
    private static Optional<Vec3> checkValidSpawnLocationWithOffset(int offset, EntityType<? extends Entity> entityType, ServerLevelAccessor level, EntitySpawnReason entitySpawnReason, Vec3i pos, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            BlockPos.MutableBlockPos blockPos$mutable = new BlockPos.MutableBlockPos();
            int x_pos = pos.getX() + random.nextInt(offset) - random.nextInt(offset);
            int y_pos = pos.getY();
            int z_pos = pos.getZ() + random.nextInt(offset) - random.nextInt(offset);
            double actual_x_pos = x_pos + 0.5d;
            double actual_z_pos = z_pos + 0.5d;
            blockPos$mutable.set(x_pos, y_pos, z_pos);
            if (SpawnPlacements.checkSpawnRules(entityType, level, entitySpawnReason, blockPos$mutable.immutable(), random)
             && level.noCollision(entityType.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))
             && isRightDistanceToPlayerAndSpawnPoint(blockPos$mutable.getCenter(), (Level)level)) {
                return Optional.of(new Vec3(actual_x_pos, (double)y_pos, actual_z_pos));
            } else {
                continue;
            }
        }
        return Optional.empty();
    }
    private static Optional<Vec3> checkValidSpawnLocationWithVanillaOffset(EntityType<? extends Entity> entityType, ServerLevelAccessor level, EntitySpawnReason entitySpawnReason, Vec3i pos, RandomSource random) {
        return checkValidSpawnLocationWithOffset(6, entityType, level, entitySpawnReason, pos, random);
    }
    private static boolean isRightDistanceToPlayerAndSpawnPoint(Vec3 position, Level level) {
        Player nearestPlayer = level.getNearestPlayer(position.x, position.y, position.z, -1.0d, false);
        if (nearestPlayer != null) {
            if (nearestPlayer.distanceToSqr(position) < (double)(24d*24d)
             || level.getSharedSpawnPos().getCenter().distanceToSqr(position) < (double)(24d*24d)) {
                return false;
            } else 
            if (nearestPlayer.distanceToSqr(position) > 128*128) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
