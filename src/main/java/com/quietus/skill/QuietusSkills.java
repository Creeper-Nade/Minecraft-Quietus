package com.quietus.skill;

import java.util.function.Supplier;

import com.quietus.core.QuietusRegistries;
import com.quietus.core.skill.Skill;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.quietus.Quietus.MODID;

public class QuietusSkills {

    public static final DeferredRegister<Skill> REGISTRAR = DeferredRegister.create(QuietusRegistries.SKILL_REGISTRY, MODID);

    /* Skill tree: occult */
    public static final Supplier<Skill> MANA_REGEN_MULT_BONUS = registerSkill("mana_regen_mult_bonus", Skill.Type.DOUBLE, Double.MAX_VALUE, "skill.quietus.default_percentage.template");
    public static final Supplier<Skill> MANA_MAX_MULT_BONUS = registerSkill("mana_max_mult_bonus", Skill.Type.DOUBLE, Double.MAX_VALUE, "skill.quietus.default_percentage.template");
    public static final Supplier<Skill> MAGIC_DAMAGE_MULT_BONUS = registerSkill("magic_damage_mult_bonus", Skill.Type.DOUBLE, Double.MAX_VALUE, "skill.quietus.default_percentage.template");

    /* Skill tree: strength */
    public static final Supplier<Skill> ATTACK_SPEED_MULT_BONUS = registerSkill("attack_speed_mult_bonus", Skill.Type.DOUBLE, Double.MAX_VALUE, "skill.quietus.default_percentage.template");
    
    /* Skill tree: technique */

    /* Skill tree: vitality */
    public static final Supplier<Skill> DEBUFF_MITIGATION = registerSkill("debuff_mitigation", Skill.Type.DOUBLE, 0.5, "skill.quietus.default_percentage.template");
    public static final Supplier<Skill> DAMAGE_SUPRESSION = registerSkill("damage_supression", Skill.Type.DOUBLE, Double.MAX_VALUE, "skill.quietus.default.template");

    private static Supplier<Skill> registerSkill(String name) {
        return REGISTRAR.register(name, () -> new Skill(ResourceKey.create(QuietusRegistries.SKILL_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MODID, name)), Integer.MAX_VALUE));
    }
    private static Supplier<Skill> registerSkill(String name, Number maxLevel) {
        return REGISTRAR.register(name, () -> new Skill(ResourceKey.create(QuietusRegistries.SKILL_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MODID, name)), maxLevel.intValue()));
    }
    private static Supplier<Skill> registerSkill(String name, Skill.Type type, Number maxLevel, String displayTemplate) {
        return REGISTRAR.register(name, () -> new Skill(ResourceKey.create(QuietusRegistries.SKILL_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MODID, name)), type, maxLevel, displayTemplate));
    }

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
