package com.minecraftquietus.quietus.util;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.core.mana.ManaComponent;
import com.minecraftquietus.quietus.core.skill.SkillComponent;
import com.minecraftquietus.server.commands.SkillCommands;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRAR =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = REGISTRAR.register("mana", () -> AttachmentType.serializable(ManaComponent::new).copyOnDeath().build());

    public static final Supplier<AttachmentType<SkillComponent>> SKILL_ATTACHMENT = REGISTRAR.register("player_skills", () -> AttachmentType.serializable(SkillComponent::new).copyOnDeath().build());
}
