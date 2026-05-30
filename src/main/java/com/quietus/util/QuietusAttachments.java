package com.quietus.util;

import com.quietus.core.GrapplingHookAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import com.quietus.core.mana.ManaComponent;
import com.quietus.core.skill.SkillComponent;

import static com.quietus.Quietus.MODID;

public class QuietusAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRAR =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<ManaComponent>> MANA_ATTACHMENT = REGISTRAR.register("mana", () -> AttachmentType.serializable(ManaComponent::new).copyOnDeath().build());

    public static final Supplier<AttachmentType<SkillComponent>> SKILL_ATTACHMENT = REGISTRAR.register("player_skills", () -> AttachmentType.serializable(SkillComponent::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<GrapplingHookAttachment>> GRAPPLE_ATTACHMENT = REGISTRAR.register("grapple", () -> AttachmentType.serializable(GrapplingHookAttachment::new).copyOnDeath().build());
}
