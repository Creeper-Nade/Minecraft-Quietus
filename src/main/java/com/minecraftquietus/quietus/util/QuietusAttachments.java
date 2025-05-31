package com.minecraftquietus.quietus.util;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.core.ManaComponent;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = ATTACHMENTS.register("mana_component", () -> AttachmentType.serializable(ManaComponent::new).copyOnDeath().build());
}
