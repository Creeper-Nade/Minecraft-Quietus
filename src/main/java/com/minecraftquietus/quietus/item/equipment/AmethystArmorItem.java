package com.minecraftquietus.quietus.item.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.entity.projectiles.magic.AmethystShardProjectile;
import com.minecraftquietus.quietus.item.property.WeaponProjectileProperty;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent.ArmorEntry;

import com.minecraftquietus.quietus.util.sound.EntitySoundSource;

public class AmethystArmorItem extends Item implements RetaliatesOnDamaged {

    public AmethystArmorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onArmorHurt(float damage, Map<EquipmentSlot, ItemStack> armorMap, EquipmentSlot slot, LivingEntity wearer) {
        Vec3 pos;
        List<ItemStack> list = new ArrayList<>();
        armorMap.forEach((equipmentSlot,armorItem) -> {
            if (armorItem.getItem() instanceof AmethystArmorItem) list.add(armorItem);
        });
        boolean full_set_bonus = list.size() >= 4;
        switch (slot) {
            case FEET:
                pos = wearer.position().add(0.00d,0.25d,0.00d);
                break;
            case LEGS:
                pos = wearer.position().add(0.00d, 0.50d,0.00d);
                break;
            case CHEST:
                pos = wearer.getEyePosition().add(0.00d, -0.625d,0.00d);
                break;
            case HEAD:
                pos = wearer.getEyePosition();
                break;
            case null:
                return;
            default: // should not be otherwise. This armor does nothing otherwise
                return;
        }
        if (wearer.level() instanceof ServerLevel level) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),pos.x, pos.y,pos.z, 10, 0,0,0,0.5);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_CLUSTER_BREAK, EntitySoundSource.of(wearer), 1.0F, 1.0F);
            RandomSource random = wearer.getRandom();
            int projectilesAmount = random.nextInt(3)+2;
            if (full_set_bonus) projectilesAmount += 1; // every armor piece adds by 1 when full set bonus active
            for (int i = 0; i < projectilesAmount; i++) {
                Vec3 posNew = new Vec3(pos.x+random.nextDouble()*0.50d-0.25d, pos.y+random.nextDouble()*0.30d-0.15d, pos.z+random.nextDouble()*0.50d-0.25d);
                AmethystShardProjectile projectile = new AmethystShardProjectile(QuietusProjectiles.AMETHYST_PROJECTILE.get(), level);
                projectile.configure(WeaponProjectileProperty.builder()
                    .damage(2.0f)
                    .critChance(0.0d)
                    .critOperation((dmg)->dmg)
                    .knockback(0.2f)
                    .gravity(0.05f)
                    .persistanceTicks(200)
                    .projectileType(QuietusProjectiles.AMETHYST_PROJECTILE.get())
                    .build());
                float yRot = random.nextFloat()*360 - 180.0f;
                float xRot = random.nextFloat()*110 - 45.0f;
                projectile.snapTo(posNew, yRot, xRot);
                projectile.shootFromRotation(wearer, xRot, yRot, 0.0f, 0.5f, 0.0f);
                projectile.setOwner(wearer);
                level.addFreshEntity(projectile);
            }
            
        }
    }
    
}
