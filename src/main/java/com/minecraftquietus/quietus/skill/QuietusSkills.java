package com.minecraftquietus.quietus.skill;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.core.skill.Skill;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusSkills {

    public static final DeferredRegister<Skill> REGISTRAR = DeferredRegister.create(QuietusRegistries.SKILL_REGISTRY, MODID);

    public static final Supplier<Skill> EXAMPLE_SKILL = REGISTRAR.register(
        "example_skill",
        () -> new Skill(ResourceKey.create(QuietusRegistries.SKILL_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(MODID, "example_skill")),5)
    );

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
