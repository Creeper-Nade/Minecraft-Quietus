package com.minecraftquietus.quietus.item.equipment;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.EnumMap;

import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.common.Tags;

public class QuietusArmorMaterials {
    public static final ArmorMaterial COPPER = new ArmorMaterial(
        // Durability
        // ArmorType have different unit durabilities that the multiplier is applied to:
        // - HELMET: 11
        // - CHESTPLATE: 16
        // - LEGGINGS: 15
        // - BOOTS: 13
        // - BODY: 16
        10,
        // Determines the defense value (or the number of half-armors on the bar).
        // Based on ArmorType.
        Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.BOOTS, 2);
            map.put(ArmorType.LEGGINGS, 4);
            map.put(ArmorType.CHESTPLATE, 6);
            map.put(ArmorType.HELMET, 2);
            map.put(ArmorType.BODY, 4);
        }),
        // Determines the enchantability of the armor. This represents how good the enchantments on this armor will be.
        // Gold uses 25; we put copper slightly below that.
        5,
        // Determines the sound played when equipping this armor.
        // This is wrapped with a Holder.
        SoundEvents.ARMOR_EQUIP_GENERIC,
         // Returns the toughness value of the armor. The toughness value is an additional value included in
        // damage calculation, for more information, refer to the Minecraft Wiki's article on armor mechanics:
        // https://minecraft.wiki/w/Armor#Armor_toughness
        // Only diamond and netherite have values greater than 0 here, so we just return 0.
        0F,
        // Returns the knockback resistance value of the armor. While wearing this armor, the player is
        // immune to knockback to some degree. If the player has a total knockback resistance value of 1 or greater
        // from all armor pieces combined, they will not take any knockback at all.
        // Only netherite has values greater than 0 here, so we just return 0.
        0F,
        // The tag that determines what items can repair this armor.
        Tags.Items.INGOTS_COPPER,
        // The resource key of the EquipmentClientInfo JSON discussed below
        // Points to assets/examplemod/equipment/copper.json
        ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MODID, "copper"))
    );
}
