package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class amethystProjectile extends MagicalProjectile {
    public amethystProjectile(EntityType<? extends MagicalProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void spawnImpactParticles() {
        level().addParticle(ParticleTypes.END_ROD,
                getX(), getY(), getZ(), 0, 0, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
