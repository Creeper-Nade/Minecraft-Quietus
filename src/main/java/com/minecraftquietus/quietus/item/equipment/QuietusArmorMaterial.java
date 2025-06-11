package com.minecraftquietus.quietus.item.equipment;

import java.util.Map;

import com.minecraftquietus.quietus.util.QuietusAttributes;
import com.minecraftquietus.quietus.util.attribute.AttributeModifierValue;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;

public record QuietusArmorMaterial(
    int durability,
    Map<ArmorType, AttributeModifierValue> defense,
    Map<ArmorType, AttributeModifierValue> maxMana,
    Map<ArmorType, AttributeModifierValue> manaRegenBonus,
    int enchantmentValue,
    Holder<SoundEvent> equipSound,
    float toughness,
    float knockbackResistance,
    TagKey<Item> repairIngredient,
    ResourceKey<EquipmentAsset> assetId
) {
    public ItemAttributeModifiers createAttributes(ArmorType armorType) {
        int _defense = this.defense.getOrDefault(armorType, AttributeModifierValue.NONE).value();
        AttributeModifier.Operation _defense_operation = this.defense.getOrDefault(armorType, AttributeModifierValue.NONE).operation();
        int _max_mana = this.maxMana.getOrDefault(armorType, AttributeModifierValue.NONE).value();
        AttributeModifier.Operation _max_mana_operation = this.maxMana.getOrDefault(armorType, AttributeModifierValue.NONE).operation();
        int _mana_regen_bonus = this.manaRegenBonus.getOrDefault(armorType, AttributeModifierValue.NONE).value();
        AttributeModifier.Operation _mana_regen_bonus_operation = this.maxMana.getOrDefault(armorType, AttributeModifierValue.NONE).operation();

        EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(armorType.getSlot());
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("armor." + armorType.getName());
        
        ItemAttributeModifiers.Builder itemattributemodifiers$builder = ItemAttributeModifiers.builder();
        itemattributemodifiers$builder.add(
            Attributes.ARMOR, new AttributeModifier(resourcelocation, _defense, _defense_operation), equipmentslotgroup
        );
        itemattributemodifiers$builder.add(
            QuietusAttributes.MAX_MANA.getDelegate(), new AttributeModifier(resourcelocation, _max_mana, _max_mana_operation), equipmentslotgroup
        );
        itemattributemodifiers$builder.add(
            QuietusAttributes.MANA_REGEN_BONUS.getDelegate(), new AttributeModifier(resourcelocation, _mana_regen_bonus, _mana_regen_bonus_operation), equipmentslotgroup
        );
        itemattributemodifiers$builder.add(
            Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourcelocation, this.toughness, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
        );
        if (this.knockbackResistance > 0.0F) {
            itemattributemodifiers$builder.add(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(resourcelocation, this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE),
                equipmentslotgroup
            );
        }

        return itemattributemodifiers$builder.build();
    }
}
