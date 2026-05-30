package com.quietus.entity.projectiles.misc.grapples;

import com.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChainGrapplingHookProjectile extends GrapplingHookProjectile {
    public ChainGrapplingHookProjectile(EntityType<? extends GrapplingHookProjectile> type, Level level) {
        super(type, level);
    }
}
