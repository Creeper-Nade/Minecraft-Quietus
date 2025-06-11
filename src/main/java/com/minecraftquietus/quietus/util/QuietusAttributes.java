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
            20d,
            // Min and max values.
            0d,
            10000d
    ));

    public static final Holder<Attribute> MANA_REGEN_BONUS = QUIETUS_ATTRIBUTES.register("mana_regen_bonus", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.quietus.mana_regen_bonus",
            // The default value.
            0d,
            // Min and max values.
            -10000d,
            10000d
    ));
}
