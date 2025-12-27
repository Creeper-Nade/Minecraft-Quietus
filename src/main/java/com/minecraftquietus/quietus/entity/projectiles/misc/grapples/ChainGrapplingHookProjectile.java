package com.minecraftquietus.quietus.entity.projectiles.misc.grapples;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.item.QuietusItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChainGrapplingHookProjectile extends GrapplingHookProjectile {
    public ChainGrapplingHookProjectile(EntityType<? extends GrapplingHookProjectile> type, Level level) {
        super(type, level);
    }

    public ChainGrapplingHookProjectile(Level level, Player player) {
        this(QuietusProjectiles.CHAIN_GRAPPLING_HOOK_PROJECTILE.get(), level);
        this.setOwner(player);
    }
}
