package com.minecraftquietus.quietus.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.minecraftquietus.quietus.item.component.UsesMana;
import com.minecraftquietus.quietus.item.equipment.QuietusArmorMaterial;
import com.minecraftquietus.quietus.item.property.SoundAsset;
import com.minecraftquietus.quietus.item.property.QuietusProjectileProperty;
import com.minecraftquietus.quietus.item.tool.QuietusToolMaterial;
import com.minecraftquietus.quietus.item.property.ProjectileWeaponProperty;
import com.minecraftquietus.quietus.util.QuietusAttributes;
import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;

import static com.minecraftquietus.quietus.item.QuietusItems.BASE_MAX_MANA_ID;
import static com.minecraftquietus.quietus.item.QuietusItems.BASE_MANA_REGEN_BONUS_ID;

public class QuietusItemProperties extends Item.Properties {

    //public List<SoundAsset> sounds = new ArrayList<SoundAsset>();
    public HashMap<String,SoundAsset> sounds = new HashMap<>();
    public QuietusItemProperties addSound(String key, SoundEvent soundEvent, SoundSource soundSource) {
        this.sounds.put(key, SoundAsset.builder().event(soundEvent).source(soundSource).build());
        return this;
    }

    
    public Item.Properties tool(QuietusToolMaterial material, TagKey<Block> mineableBlocks, float attackDamage, float attackSpeed, float disableBlockingForSeconds) {
        return material.applyToolProperties(this, mineableBlocks, attackDamage, attackSpeed, disableBlockingForSeconds);
    }

    public Item.Properties pickaxe(QuietusToolMaterial material, float attackDamage, float attackSpeed) {
        return this.tool(material, BlockTags.MINEABLE_WITH_PICKAXE, attackDamage, attackSpeed, 0.0F);
    }

    public Item.Properties axe(QuietusToolMaterial material, float attackDamage, float attackSpeed) {
        return this.tool(material, BlockTags.MINEABLE_WITH_AXE, attackDamage, attackSpeed, 5.0F);
    }

    public Item.Properties hoe(QuietusToolMaterial material, float attackDamage, float attackSpeed) {
        return this.tool(material, BlockTags.MINEABLE_WITH_HOE, attackDamage, attackSpeed, 0.0F);
    }

    public Item.Properties shovel(QuietusToolMaterial material, float attackDamage, float attackSpeed) {
        return this.tool(material, BlockTags.MINEABLE_WITH_SHOVEL, attackDamage, attackSpeed, 0.0F);
    }

    public Item.Properties sword(QuietusToolMaterial material, float attackDamage, float attackSpeed) {
        return material.applySwordProperties(this, attackDamage, attackSpeed);
    }
    public Item.Properties quietusHumanoidArmor(QuietusArmorMaterial material, ArmorType type) {
        return this.durability(type.getDurability(material.durability()))
            .attributes(material.createAttributes(type))
            .enchantable(material.enchantmentValue())
            .component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).build()
            )
            .repairable(material.repairIngredient());
    }

    public ProjectileWeaponProperty projectileWeaponProperty;
    public QuietusItemProperties weaponProperty(int projectilePerShot, TriFunction<Float,Integer,RandomSource,Float> xRotOffsetCalc, TriFunction<Float,Integer,RandomSource,Float> yRotOffsetCalc, float shootVelocity, float shootInaccuracy, Predicate<ItemStack> supportedProjectiles, int useDuration, int powerDuration, int attackRange) {
        this.projectileWeaponProperty = ProjectileWeaponProperty.builder()
            .projectilesPerShot(projectilePerShot)
            .xRotOffsetFunc(xRotOffsetCalc)
            .yRotOffsetFunc(yRotOffsetCalc)
            .shootVelocity(shootVelocity)
            .shootInaccuracy(shootInaccuracy)
            .supportedProjectiles(supportedProjectiles)
            .useDuration(useDuration)
            .powerDuration(powerDuration)
            .attackRange(attackRange).build();
        return this;
    }

    public HashMap<Integer,QuietusProjectileProperty> projectileProperties = new HashMap<>();
    public QuietusItemProperties addProjectile(int key, float damage, double critChance, Function<Float,Float> func, float knockback, float gravity, int persistanceTicks, EntityType<? extends Projectile> projectileType) {
        this.projectileProperties.put(key, QuietusProjectileProperty.builder()
            .damage(damage)
            .critChance(critChance)
            .critOperation(func)
            .knockback(knockback)
            .gravity(gravity)
            .persistanceTicks(persistanceTicks)
            .projectileType(projectileType)
            .build()
        );
        return this;
    }
    public QuietusItemProperties addProjectileCritChance(int key, double critChance) {
        this.projectileProperties.put(key, QuietusProjectileProperty.builder()
            .critChance(critChance)
            .build()
        );
        return this;
    }

    

    public QuietusItemProperties manaUse(int value, UsesMana.Operation operation, int minAmount) {
        this.component(QuietusComponents.USES_MANA.get(), new UsesMana.Builder().amount(value).operation(operation).minAmount(minAmount).build());
        return this;
    }

    public QuietusItemProperties maxManaAttributes(int value, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        this.attributes(
            ItemAttributeModifiers.builder()
                .add(QuietusAttributes.MAX_MANA.getDelegate(), new AttributeModifier(BASE_MAX_MANA_ID, value, operation), slot)
            .build());
        return (QuietusItemProperties)this;
    }

    public QuietusItemProperties manaRegenBonusAttributes(int value, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        this.attributes(
            ItemAttributeModifiers.builder()
                .add(QuietusAttributes.MAX_MANA.getDelegate(), new AttributeModifier(BASE_MANA_REGEN_BONUS_ID, value, operation), slot)
            .build());
        return (QuietusItemProperties)this;
    }
   
}