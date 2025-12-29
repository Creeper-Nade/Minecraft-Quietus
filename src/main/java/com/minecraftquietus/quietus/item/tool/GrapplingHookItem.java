package com.minecraftquietus.quietus.item.tool;

import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.entity.projectiles.misc.grapples.ChainGrapplingHookProjectile;
import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.item.property.GrapplingHookProperty;
import com.minecraftquietus.quietus.item.property.QuietusProjectileProperty;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class GrapplingHookItem extends QuietusProjectileWeaponItem {
    private final GrapplingHookProperty grapplingHookProperty;

    public GrapplingHookItem(Item.Properties properties) {
        super(properties);

        if (properties instanceof QuietusItemProperties quietusProps) {
            this.grapplingHookProperty = quietusProps.getGrapplingHookProperty();
        } else {
            // Default grappling hook properties
            this.grapplingHookProperty = GrapplingHookProperty.builder()
                    .maxRange(100.0F)
                    .pullStrength(0.1F)
                    .frictionMultiplier(0.99F)
                    .build();
        }
    }

    @Override
    public InteractionResult InteractionAction(Player player, Level level, ItemStack itemstack) {
        // Check if player already has an active hook
        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
        if (attachment.hasActiveHook()) {
            // Retrieve existing hook
            retrieveHookForPlayer(player);
        }

        return super.InteractionAction(player,level,itemstack);
    }

    @Override
    protected QuietusProjectile createProjectileWithKey(int key, Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        QuietusProjectile projectile=super.createProjectileWithKey(key, level, shooter, weapon, ammo, isCrit);
        if(projectile instanceof GrapplingHookProjectile hook)
        {
            // Configure projectile with both grappling hook and projectile properties
            hook.setGrapplingHookProperty(grapplingHookProperty);
            // Store hook reference in attachment
            GrapplingHookAttachment attachment = shooter.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            attachment.setHookEntityId(hook.getId());
        }
        return projectile;
    }

    public static void retrieveHookForPlayer(Player player) {
        Level level = player.level();
        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);

        if (attachment.hasActiveHook()) {
            var entity = level.getEntity(attachment.getHookEntityId());
            if (entity instanceof GrapplingHookProjectile hook && !level.isClientSide()) {
                hook.discard();
                attachment.clear();
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                    1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
    }
}