package com.minecraftquietus.quietus.client.model.equipments;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.minecraftquietus.quietus.item.equipment.AmethystArmorItem;
import net.minecraft.resources.Identifier;


import static com.minecraftquietus.quietus.Quietus.MODID;

public class AmethystArmorModel extends GeoModel<AmethystArmorItem> {


    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "item/armor/amethyst_armor");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/equipment/gecko/amethyst_armor_full.png");
    }

    @Override
    public Identifier getAnimationResource(AmethystArmorItem animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "amethyst_armor.animation");
    }
}