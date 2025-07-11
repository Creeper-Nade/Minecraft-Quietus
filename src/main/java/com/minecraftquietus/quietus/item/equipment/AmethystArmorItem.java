package com.minecraftquietus.quietus.item.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.minecraftquietus.quietus.client.model.equipments.AmethystArmorRenderer;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.entity.projectiles.magic.SmallAmethystShardProjectile;
import com.minecraftquietus.quietus.item.property.QuietusProjectileProperty;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class AmethystArmorItem extends Item implements RetaliatesOnDamaged, GeoItem {

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    
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
        if (wearer.level() instanceof ServerLevel level && full_set_bonus) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState()),pos.x, pos.y,pos.z, 10, -1,0.5,1,0.5);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, EntitySoundSource.of(wearer), 0.5F, 1.0F);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.CHAIN_BREAK, EntitySoundSource.of(wearer), 0.5F, 1.0F);
            RandomSource random = wearer.getRandom();
            int projectilesAmount = random.nextInt(3)+2;
            if (full_set_bonus) projectilesAmount += 2; // every armor piece adds by 2 when full set bonus active
            for (int i = 0; i < projectilesAmount; i++) {
                Vec3 posNew = new Vec3(pos.x+random.nextDouble()*0.50d-0.25d, pos.y+random.nextDouble()*0.30d-0.15d, pos.z+random.nextDouble()*0.50d-0.25d);
                SmallAmethystShardProjectile projectile = new SmallAmethystShardProjectile(QuietusProjectiles.SMALL_AMETHYST_PROJECTILE.get(), level);
                projectile.configure(QuietusProjectileProperty.builder()
                    .damage(4.0f)
                    .critChance(0.0d)
                    .critOperation((dmg)->dmg)
                    .knockback(0.2f)
                    .gravity(0.05f)
                    .persistanceTicks(200)
                    .projectileType(QuietusProjectiles.SMALL_AMETHYST_PROJECTILE.get())
                    .build(), armorMap.get(slot));
                float yRot = random.nextFloat()*360 - 180.0f;
                float xRot = random.nextFloat()*110 - 45.0f;
                projectile.snapTo(posNew, yRot, xRot);
                projectile.shootFromRotation(wearer, xRot, yRot, 0.0f, 0.5f, 0.0f);
                projectile.setOwner(wearer);
                level.addFreshEntity(projectile);
            }
        }
    }

    //geckolib related stuffs
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AmethystArmorRenderer renderer;

            @Override
            public <S extends HumanoidRenderState> GeoArmorRenderer<?, ?> getGeoArmorRenderer(@Nullable S livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, EquipmentClientInfo.LayerType type, @Nullable HumanoidModel<S> original) {
                if(this.renderer == null)
                    this.renderer = new AmethystArmorRenderer();

                return this.renderer;
            }
        });
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
    {
        controllerRegistrar.add(new AnimationController<GeoAnimatable>(animTest -> PlayState.STOP));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
