package com.minecraftquietus.quietus.util;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.core.ManaComponent;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRAR =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = REGISTRAR.register("mana_component", () -> AttachmentType.serializable(() -> new ManaComponent(null)).copyOnDeath().build());
    //public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = REGISTRAR.register("mana_component", () -> AttachmentType.serializable(ManaComponent::new).copyOnDeath().build());
    /*public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = 
        REGISTRAR.register("mana_component", () -> 
            AttachmentType.<ManaComponent>builder(holder -> new ManaComponent((Entity) holder))
                .serialize(INBTSerializable::serializeNBT)
                .deserialize((holder, provider, tag) -> {
                    ManaComponent component = new ManaComponent((Player) holder);
                    component.deserializeNBT(provider, tag);
                    return component;
                })
                .copyOnDeath()
                .build()
        );*/
     /*public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = 
        REGISTRAR.register("mana_component", () -> 
            AttachmentType.<ManaComponent>builder(holder -> new ManaComponent((LivingEntity) holder))
                .serialize((ManaComponent component, HolderLookup.Provider provider) -> component.serializeNBT(provider))
                .deserialize((Object holder, HolderLookup.Provider provider, CompoundTag tag) -> {
                    ManaComponent component = new ManaComponent((LivingEntity) holder);
                    component.deserializeNBT(provider, (CompoundTag)tag);
                    return component;
                })
                .copyOnDeath()
                .build()
        );*/
}
