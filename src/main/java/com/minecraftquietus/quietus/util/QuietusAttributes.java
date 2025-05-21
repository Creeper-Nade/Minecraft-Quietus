package com.minecraftquietus.quietus.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusAttributes {

    public static final DeferredRegister<Attribute> QUIETUS_ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, MODID);

    public static final Holder<Attribute> MAX_MANA = QUIETUS_ATTRIBUTES.register("max_mana", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.quietus.max_mana",
            // The default value.
            20,
            // Min and max values.
            0,
            10000
    ));

    public static final Holder<Attribute> MANA_REGEN_CD = QUIETUS_ATTRIBUTES.register("mana_regen_cd", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.quietus.mana_regen_cd",
            // The default value.
            5,
            // Min and max values.
            -10000,
            10000
    ));
}
