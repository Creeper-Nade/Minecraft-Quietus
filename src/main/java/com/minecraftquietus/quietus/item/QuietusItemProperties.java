package com.minecraftquietus.quietus.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.minecraftquietus.quietus.item.component.UsesMana;
import com.minecraftquietus.quietus.item.property.SoundAsset;
import com.minecraftquietus.quietus.item.property.WeaponProjectileProperty;
import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class QuietusItemProperties extends Item.Properties {

    //public List<SoundAsset> sounds = new ArrayList<SoundAsset>();
    public HashMap<String,SoundAsset> sounds = new HashMap<>();
    public QuietusItemProperties addSound(String key, SoundEvent soundEvent, SoundSource soundSource) {
        this.sounds.put(key, new SoundAsset.Builder().event(soundEvent).source(soundSource).build());
        return this;
    }

    public int projectilesPerShot;
    public QuietusItemProperties projectilesPerShot(int value) {
        if (value < 0) throw new IllegalArgumentException("QuietusItemProperties: projectiles per shot must ≥ 0");
        this.projectilesPerShot = value;
        return this;
    }

    public TriFunction<Float,Integer,RandomSource,Float>[] rotOffsetCalc;
    @SuppressWarnings("unchecked")
    public QuietusItemProperties rotOffsetCalc(TriFunction<Float,Integer,RandomSource,Float> funcX, TriFunction<Float,Integer,RandomSource,Float> funcY) {
        this.rotOffsetCalc = new TriFunction[] {funcX,funcY};
        return this;
    }

    public Predicate<ItemStack> supportedProjectiles;
    public QuietusItemProperties supportedProjectiles(Predicate<ItemStack> predicate) {
        this.supportedProjectiles = predicate;
        return this;
    }

    public int attackRange;
    public QuietusItemProperties projectileRange(int value) {
        this.attackRange = value;
        return this;
    }

    public float shootVelocity;
    public QuietusItemProperties shootVelocity(float value) {
        this.shootVelocity = value;
        return this;
    }

    public WeaponProjectileProperty projectileProperties;
    public QuietusItemProperties projectileProperties(float damage, double critChance, float knockback, float gravity, int persistanceTicks) {
        this.projectileProperties = new WeaponProjectileProperty.Builder()
            .damage(damage)
            .critChance(critChance)
            .knockback(knockback)
            .gravity(gravity)
            .persistanceTicks(persistanceTicks)
            .build();
        return this;
    }
    public QuietusItemProperties projectileCritChance(double critChance) {
        this.projectileProperties = new WeaponProjectileProperty.Builder()
            .critChance(critChance)
            .build();
        return this;
    }

    

    public QuietusItemProperties manaUse(int value, UsesMana.Operation operation, int minAmount) {
        this.component(QuietusComponents.USES_MANA.get(), new UsesMana.Builder().amount(value).operation(operation).minAmount(minAmount).build());
        return this;
    }
   
}