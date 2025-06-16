package com.minecraftquietus.quietus.client.model.equipments;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.minecraftquietus.quietus.item.equipment.AmethystArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class AmethystArmorModel extends GeoModel<AmethystArmorItem> {


    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "item/armor/amethyst_armor");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/equipment/gecko/amethyst_armor_full.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AmethystArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "amethyst_armor.animation");
    }
}