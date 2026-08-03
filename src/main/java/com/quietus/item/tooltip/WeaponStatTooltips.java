package com.quietus.item.tooltip;

import com.quietus.combat.ProjectileVolleyBalance;
import com.quietus.enchantment.QuietusEnchantmentComponent;
import com.quietus.item.QuietusComponents;
import com.quietus.item.component.UsesMana;
import com.quietus.item.property.QuietusProjectileProperty;
import com.quietus.item.tool.AmmoProjectileWeaponItem;
import com.quietus.item.tool.GrapplingHookItem;
import com.quietus.item.tool.QuietusProjectileWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.neoforge.common.config.NeoForgeCommonConfig;

import java.util.List;
import java.util.function.Consumer;

/** Builds combat stat lines from the same item components and enchantment effects used by gameplay. */
public final class WeaponStatTooltips {
    private WeaponStatTooltips() {
    }

    /** Replaces vanilla melee stat lines when enchantment effects change their effective values. */
    public static void updateMeleeStats(ItemStack stack, Player player, List<Component> tooltip) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double playerDamage = player == null ? 1.0D : player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        double playerSpeed = player == null ? 4.0D : player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        double baseDamage = modifiers.compute(Attributes.ATTACK_DAMAGE, playerDamage, EquipmentSlot.MAINHAND);
        double baseSpeed = modifiers.compute(Attributes.ATTACK_SPEED, playerSpeed, EquipmentSlot.MAINHAND);
        float enchantedDamage = applyDamageEnchantments(stack,
                (float) applyAttributeEnchantments(stack, Attributes.ATTACK_DAMAGE, baseDamage), false);
        double enchantedSpeed = applyAttributeEnchantments(stack, Attributes.ATTACK_SPEED, baseSpeed);

        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (Math.abs(enchantedDamage - baseDamage) >= 0.0001F
                    && containsTranslation(line, Attributes.ATTACK_DAMAGE.value().getDescriptionId())) {
                tooltip.set(i, rewriteAttributeValue(
                        line, baseDamage, enchantedDamage, Attributes.ATTACK_DAMAGE.value().getDescriptionId()));
            } else if (Math.abs(enchantedSpeed - baseSpeed) >= 0.0001D
                    && containsTranslation(line, Attributes.ATTACK_SPEED.value().getDescriptionId())) {
                tooltip.set(i, rewriteAttributeValue(
                        line, baseSpeed, enchantedSpeed, Attributes.ATTACK_SPEED.value().getDescriptionId()));
            }
        }
    }

    public static boolean isProjectileWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof QuietusProjectileWeaponItem
                    && !(stack.getItem() instanceof GrapplingHookItem);
    }

    public static void appendProjectileStats(ItemStack stack, Player player, TooltipFlag flag,
                                             Consumer<Component> builder) {
        ProjectileStats stats = projectileStats(stack, player);
        if (stats == null) {
            return;
        }

        boolean showCalculation = flag.isAdvanced()
                && NeoForgeCommonConfig.INSTANCE.attributeAdvancedTooltipDebugInfo.get();
        double playerDamage = player == null ? 1.0D : player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        double playerSpeed = player == null ? 4.0D : player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        builder.accept(CommonComponents.EMPTY);
        builder.accept(Component.translatable("tooltip.quietus.weapon.when_fired").withStyle(ChatFormatting.GRAY));
        builder.accept(statLine(
                stats.damageKey, playerDamage, stats.baseDamage, stats.damage, showCalculation));
        builder.accept(statLine("tooltip.quietus.weapon.attack_speed",
                playerSpeed, stats.baseAttackSpeed, stats.attackSpeed, showCalculation));
        if (stats.manaCost >= 0 && stats.baseManaCost >= 0) {
            builder.accept(statLine("tooltip.quietus.weapon.mana_cost",
                    stats.baseManaCost, stats.baseManaCost, stats.manaCost, showCalculation));
        }
    }

    public static void insertProjectileStatsBeforeDurability(ItemStack stack, Player player, TooltipFlag flag,
                                                              List<Component> tooltip) {
        List<Component> stats = new java.util.ArrayList<>();
        appendProjectileStats(stack, player, flag, stats::add);
        if (stats.isEmpty()) {
            return;
        }

        int insertionIndex = footerIndex(stack, tooltip);
        tooltip.addAll(insertionIndex, stats);
    }

    public static void appendDurabilityIfMissing(ItemStack stack, List<Component> tooltip) {
        if (!isProjectileWeapon(stack) || !stack.isDamageableItem()
                || tooltip.stream().anyMatch(line -> containsTranslation(line, "item.durability"))) {
            return;
        }
        tooltip.add(footerIndex(stack, tooltip), Component.translatable(
                "item.durability", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()
        ));
    }

    public static void insertBeforeFooter(ItemStack stack, List<Component> tooltip, Component line) {
        tooltip.add(footerIndex(stack, tooltip), line);
    }

    public static void insertTranslatedLinesBeforeFooter(ItemStack stack, List<Component> tooltip,
                                                         String prefix, int lineCount, Object... arguments) {
        int insertionIndex = footerIndex(stack, tooltip);
        for (int line = 1; line <= lineCount; line++) {
            tooltip.add(insertionIndex++, Component.translatable(prefix + line, arguments));
        }
    }

    private static ProjectileStats projectileStats(ItemStack stack, Player player) {
        if (stack.getItem() instanceof GrapplingHookItem) {
            return null;
        }
        if (stack.getItem() instanceof QuietusProjectileWeaponItem weapon) {
            int projectiles = weapon.getProjectilesPerShot();
            int chargeTicks = customChargeTicks(stack, weapon);
            double rawDamage;
            String damageKey;
            if (weapon instanceof AmmoProjectileWeaponItem) {
                rawDamage = Math.ceil(2.0D * weapon.getShootVelocity());
                damageKey = "tooltip.quietus.weapon.arrow_damage";
            } else {
                QuietusProjectileProperty projectile = weapon.getProjectileProperty(0);
                if (projectile == null || !projectile.isCustom()) {
                    return null;
                }
                rawDamage = projectile.damage();
                damageKey = "tooltip.quietus.weapon.magic_damage";
            }
            double volleyScale = ProjectileVolleyBalance.damageScale(projectiles);
            double baseDamage = rawDamage * volleyScale;
            double damage = applyDamageEnchantments(stack, (float) rawDamage, true) * volleyScale;
            int baseManaCost = baseManaCost(stack);
            return new ProjectileStats(
                    baseDamage,
                    damage,
                    attacksPerSecond(chargeTicks),
                    attacksPerSecond(chargeTicks),
                    baseManaCost,
                    manaCost(stack),
                    damageKey
            );
        }

        if (stack.getItem() instanceof BowItem) {
            int projectiles = enchantedProjectileCount(stack, 1);
            double baseDamage = 6.0D;
            double damage = applyDamageEnchantments(stack, 6.0F, true)
                    * ProjectileVolleyBalance.damageScale(projectiles);
            return new ProjectileStats(baseDamage, damage, 1.0D, 1.0D, -1, -1,
                    "tooltip.quietus.weapon.arrow_damage");
        }

        if (stack.getItem() instanceof CrossbowItem) {
            int chargeTicks = player == null ? fallbackCrossbowChargeTicks(stack)
                    : CrossbowItem.getChargeDuration(stack, player);
            int projectiles = enchantedProjectileCount(stack, 1);
            double baseAttackSpeed = attacksPerSecond(25);
            double baseDamage = 7.0D;
            double damage = applyDamageEnchantments(stack, 7.0F, true)
                    * ProjectileVolleyBalance.damageScale(projectiles);
            return new ProjectileStats(baseDamage, damage, baseAttackSpeed, attacksPerSecond(chargeTicks), -1, -1,
                    "tooltip.quietus.weapon.arrow_damage");
        }
        return null;
    }

    private static int customChargeTicks(ItemStack stack, QuietusProjectileWeaponItem weapon) {
        int ticks = weapon.getConfiguredPowerDuration() >= 0
                ? weapon.getConfiguredPowerDuration()
                : Math.max(0, weapon.getConfiguredUseDuration());
        if (ticks == 0) {
            UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
            return cooldown == null ? 0 : cooldown.ticks();
        }
        return ticks;
    }

    private static int fallbackCrossbowChargeTicks(ItemStack stack) {
        final float[] seconds = {1.25F};
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, level) -> {
            EnchantmentValueEffect effect = enchantment.value().effects()
                    .get(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME);
            if (effect != null) {
                seconds[0] = effect.process(level, tooltipRandom(), seconds[0]);
            }
        });
        return Math.max(0, (int) Math.floor(seconds[0] * 20.0F));
    }

    private static int enchantedProjectileCount(ItemStack stack, int baseCount) {
        float[] count = {baseCount};
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, level) -> {
            for (ConditionalEffect<EnchantmentValueEffect> effect
                    : enchantment.value().getEffects(EnchantmentEffectComponents.PROJECTILE_COUNT)) {
                count[0] = effect.effect().process(level, tooltipRandom(), count[0]);
            }
        });
        return Math.max(1, (int) count[0]);
    }

    private static float applyDamageEnchantments(ItemStack stack, float baseDamage, boolean projectileWeapon) {
        float[] damage = {baseDamage};
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, level) -> {
            for (ConditionalEffect<EnchantmentValueEffect> effect
                    : enchantment.value().getEffects(EnchantmentEffectComponents.DAMAGE)) {
                // Target-specific melee effects (for example Smite) are not a single true weapon stat.
                // Projectile conditions describe the projectile that this weapon always creates, so include them.
                if (projectileWeapon || effect.requirements().isEmpty()) {
                    damage[0] = effect.effect().process(level, tooltipRandom(), damage[0]);
                }
            }
        });
        return damage[0];
    }

    private static double applyAttributeEnchantments(ItemStack stack, net.minecraft.core.Holder<Attribute> attribute,
                                                     double baseValue) {
        double[] value = {baseValue};
        EnchantmentHelper.forEachModifier(stack, EquipmentSlot.MAINHAND, (effectAttribute, modifier) -> {
            if (effectAttribute.equals(attribute)) {
                value[0] = applyModifier(value[0], baseValue, modifier);
            }
        });
        return value[0];
    }

    private static double applyModifier(double value, double baseValue, AttributeModifier modifier) {
        return switch (modifier.operation()) {
            case ADD_VALUE -> value + modifier.amount();
            case ADD_MULTIPLIED_BASE -> value + baseValue * modifier.amount();
            case ADD_MULTIPLIED_TOTAL -> value * (1.0D + modifier.amount());
        };
    }

    private static int manaCost(ItemStack stack) {
        UsesMana usesMana = stack.get(QuietusComponents.USES_MANA.get());
        if (usesMana == null || usesMana.operation() != UsesMana.Operation.ADD_VALUE) {
            return -1;
        }
        float[] reduction = {0.0F};
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, level) -> {
            for (ConditionalEffect<EnchantmentValueEffect> effect
                    : enchantment.value().getEffects(QuietusEnchantmentComponent.MANA_COST_REDUCTION.get())) {
                if (effect.requirements().isEmpty()) {
                    reduction[0] = effect.effect().process(level, tooltipRandom(), reduction[0]);
                }
            }
        });
        return Math.max(usesMana.minAmount(), Math.round(usesMana.amount() * (1.0F - reduction[0])));
    }

    private static int baseManaCost(ItemStack stack) {
        UsesMana usesMana = stack.get(QuietusComponents.USES_MANA.get());
        return usesMana != null && usesMana.operation() == UsesMana.Operation.ADD_VALUE
                ? Math.max(usesMana.minAmount(), usesMana.amount())
                : -1;
    }

    private static double attacksPerSecond(int ticks) {
        return ticks <= 0 ? 20.0D : 20.0D / ticks;
    }

    private static Component statLine(String key, double entityBase, double baseValue,
                                      double finalValue, boolean showCalculation) {
        MutableComponent line = Component.translatable(key, format(finalValue))
                .withStyle(ChatFormatting.DARK_GREEN);
        if (showCalculation) {
            line.append(CommonComponents.SPACE).append(calculationBracket(entityBase, baseValue, finalValue));
        }
        return line;
    }

    private static String format(double value) {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value);
    }

    /** Changes only vanilla's final numeric argument, retaining styles, siblings and optional breakdown text. */
    private static Component rewriteAttributeValue(Component component, double baseValue,
                                                   double finalValue, String attributeKey) {
        MutableComponent rewritten;
        if (component.getContents() instanceof TranslatableContents translatable) {
            Object[] arguments = translatable.getArgs().clone();
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] instanceof Component child) {
                    arguments[i] = rewriteAttributeValue(child, baseValue, finalValue, attributeKey);
                }
            }
            if (translatable.getKey().startsWith("attribute.modifier.equals.")
                    && containsTranslation(component, attributeKey)
                    && arguments.length > 0) {
                arguments[0] = Component.literal(format(finalValue));
            } else if (translatable.getKey().equals("neoforge.attribute.debug.base")
                    && arguments.length > 1) {
                arguments[1] = appendCalculationTerm(arguments[1], finalValue - baseValue);
            }
            rewritten = MutableComponent.create(new TranslatableContents(
                    translatable.getKey(), translatable.getFallback(), arguments));
        } else {
            rewritten = MutableComponent.create(component.getContents());
        }
        rewritten.setStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            rewritten.append(rewriteAttributeValue(sibling, baseValue, finalValue, attributeKey));
        }
        return rewritten;
    }

    private static Component calculationBracket(double entityBase, double baseValue, double finalValue) {
        return Component.translatable(
                "neoforge.attribute.debug.base",
                format(entityBase),
                signedCalculationTerm(baseValue - entityBase)
                        + signedCalculationTerm(finalValue - baseValue)
        ).withStyle(ChatFormatting.GRAY);
    }

    private static int footerIndex(ItemStack stack, List<Component> tooltip) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (containsTranslation(line, "item.durability")
                    || containsTranslation(line, "item.components")
                    || line.getString().equals(itemId)) {
                return i;
            }
        }
        return tooltip.size();
    }

    private static Object appendCalculationTerm(Object existing, double adjustment) {
        if (Math.abs(adjustment) < 0.0001D) {
            return existing;
        }
        String term = signedCalculationTerm(adjustment);
        if (existing instanceof Component component) {
            return component.copy().append(term);
        }
        return String.valueOf(existing) + term;
    }

    private static String signedCalculationTerm(double adjustment) {
        if (Math.abs(adjustment) < 0.0001D) {
            return "";
        }
        return (adjustment > 0.0D ? " + " : " - ") + format(Math.abs(adjustment));
    }

    private static RandomSource tooltipRandom() {
        // Enchantment effects accept randomness even when their value is deterministic. A fixed seed
        // keeps any data-pack-provided random value from flickering every time the tooltip is rebuilt.
        return RandomSource.create(0L);
    }

    private static boolean containsTranslation(Component component, String key) {
        if (component.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().equals(key)) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (containsTranslation(sibling, key)) {
                return true;
            }
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            for (Object argument : translatable.getArgs()) {
                if (argument instanceof Component child && containsTranslation(child, key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record ProjectileStats(double baseDamage, double damage,
                                   double baseAttackSpeed, double attackSpeed,
                                   int baseManaCost, int manaCost, String damageKey) {
    }
}
