package com.quietus.server.commands;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.quietus.core.QuietusRegistries;
import com.quietus.core.skill.Skill;
import com.quietus.util.SkillUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class SkillCommands {

    private static final DynamicCommandExceptionType ERROR_INVALID_SKILL = new DynamicCommandExceptionType(
        arg1 -> Component.translatableEscape("skill.skillNotFound", arg1)
    );
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(arg1 -> (Component)arg1);
    private static final DynamicCommandExceptionType ERROR_INVALID_NUMBER = new DynamicCommandExceptionType(
        arg1 -> Component.translatableEscape("commands.skill.error.invalid_number", arg1)
    );
    private static final DynamicCommandExceptionType ERROR_WRONG_NUMBER_TYPE = new DynamicCommandExceptionType(
        arg1 -> (Component)arg1
    );

    private record ParsedNumber(Number value, Skill.Type type) {}

    private static ParsedNumber parseAndValidateNumber(String input, Skill skill) throws CommandSyntaxException {
        if (input == null || input.isEmpty()) {
            throw ERROR_INVALID_NUMBER.create(input);
        }
        String str = input.trim();
        ParsedNumber parsed;
        if (str.endsWith("f") || str.endsWith("F")) {
            try {
                float f = Float.parseFloat(str.substring(0, str.length() - 1));
                if (f < 0) throw ERROR_INVALID_NUMBER.create(str);
                parsed = new ParsedNumber(f, Skill.Type.FLOAT);
            } catch (NumberFormatException e) {
                throw ERROR_INVALID_NUMBER.create(str);
            }
        } else if (str.endsWith("d") || str.endsWith("D")) {
            try {
                double d = Double.parseDouble(str.substring(0, str.length() - 1));
                if (d < 0) throw ERROR_INVALID_NUMBER.create(str);
                parsed = new ParsedNumber(d, Skill.Type.DOUBLE);
            } catch (NumberFormatException e) {
                throw ERROR_INVALID_NUMBER.create(str);
            }
        } else if (str.contains(".")) {
            try {
                double d = Double.parseDouble(str);
                if (d < 0) throw ERROR_INVALID_NUMBER.create(str);
                parsed = new ParsedNumber(d, Skill.Type.DOUBLE);
            } catch (NumberFormatException e) {
                throw ERROR_INVALID_NUMBER.create(str);
            }
        } else {
            try {
                int i = Integer.parseInt(str);
                if (i < 0) throw ERROR_INVALID_NUMBER.create(str);
                parsed = new ParsedNumber(i, Skill.Type.INT);
            } catch (NumberFormatException e) {
                throw ERROR_INVALID_NUMBER.create(str);
            }
        }

        if (parsed.type() != skill.getType()) {
            throw ERROR_WRONG_NUMBER_TYPE.create(
                Component.translatable("commands.skill.error.wrong_number_type", Component.translatable(skill.getDisplayId()), skill.getType().name(), parsed.type().name())
            );
        }
        return parsed;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
          Commands.literal("skill")
            .requires(stack -> stack.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
            .then(
              Commands.literal("add")
              .then(
                Commands.argument("targets", EntityArgument.players())
                .then(
                  Commands.argument("skill", ResourceKeyArgument.key(QuietusRegistries.SKILL_REGISTRY_KEY))
                  .then(
                    Commands.argument("amount", StringArgumentType.word())
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> {
                        Skill skill = context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value();
                        ParsedNumber amount = parseAndValidateNumber(StringArgumentType.getString(context, "amount"), skill);
                        return perform(
                          (CommandSourceStack)context.getSource(), 
                          EntityArgument.getPlayers(context, "targets"), 
                          Action.ADD, 
                          skill,
                          amount.value(),
                          StringArgumentType.getString(context, "source")
                        );
                      })
                    )
                  )
                )
              )
            )
            .then(
              Commands.literal("remove")
              .then(
                Commands.argument("targets", EntityArgument.players())
                .then(
                  Commands.argument("skill", ResourceKeyArgument.key(QuietusRegistries.SKILL_REGISTRY_KEY))
                  .then(
                    Commands.argument("amount", StringArgumentType.word())
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> {
                        Skill skill = context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value();
                        ParsedNumber amount = parseAndValidateNumber(StringArgumentType.getString(context, "amount"), skill);
                        return perform(
                          (CommandSourceStack)context.getSource(), 
                          EntityArgument.getPlayers(context, "targets"), 
                          Action.REMOVE, 
                          skill,
                          amount.value(),
                          StringArgumentType.getString(context, "source")
                        );
                      })
                    )
                  )
                )
              )
            )
            .then(
              Commands.literal("get")
              .then(
                Commands.argument("target", EntityArgument.player())
                .then(
                  Commands.argument("skill", ResourceKeyArgument.key(QuietusRegistries.SKILL_REGISTRY_KEY))
                  .executes(context -> perform(
                    (CommandSourceStack)context.getSource(), 
                    EntityArgument.getPlayer(context, "target"), 
                    Action.GET, 
                    context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value()
                  ))
                  .then(
                    Commands.argument("source", StringArgumentType.string())
                    .executes(context -> perform(
                      (CommandSourceStack)context.getSource(), 
                      EntityArgument.getPlayer(context, "target"), 
                      Action.GET, 
                      context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value(),
                      StringArgumentType.getString(context, "source")
                    ))
                  )
                )
              )
            )
            .then(
              Commands.literal("set")
              .then(
                Commands.argument("targets", EntityArgument.players())
                .then(
                  Commands.argument("skill", ResourceKeyArgument.key(QuietusRegistries.SKILL_REGISTRY_KEY))
                  .then(
                    Commands.argument("value", StringArgumentType.word())
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> {
                        Skill skill = QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL));
                        ParsedNumber value = parseAndValidateNumber(StringArgumentType.getString(context, "value"), skill);
                        return perform(
                          (CommandSourceStack)context.getSource(), 
                          EntityArgument.getPlayers(context, "targets"), 
                          Action.SET, 
                          skill, 
                          value.value(),
                          StringArgumentType.getString(context, "source")
                        );
                      })
                    )
                  )
                )
              )
            )
        );
    }

    private static int perform(CommandSourceStack sourceStack, ServerPlayer player, SkillCommands.Action action, Skill skill) throws CommandSyntaxException {
        return perform(sourceStack, (Collection<ServerPlayer>)List.of(player), action, skill, 0, null);
    }
    private static int perform(CommandSourceStack sourceStack, ServerPlayer player, SkillCommands.Action action, Skill skill, String source) throws CommandSyntaxException {
        return perform(sourceStack, (Collection<ServerPlayer>)List.of(player), action, skill, 0, source);
    }
    private static int perform(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillCommands.Action action, Skill skill, Number amount, @Nullable String source) throws CommandSyntaxException {
        int count = 0;
        int player_amount = players.size();
        ServerPlayer first_player = (ServerPlayer)players.toArray()[0];

        if (action == Action.GET) {
          final Number get_result = action.perform(first_player, skill, player_amount, source);
          if (source == null) {
            Map<String, Number> sourceLevels = SkillUtil.getSkillSourceLevels(first_player, skill);
            MutableComponent sourcesComponent = Component.empty();
            boolean first = true;
            for (Map.Entry<String, Number> entry : sourceLevels.entrySet()) {
              if (!first) {
                sourcesComponent.append(", ");
              }
              sourcesComponent.append(Component.translatable(action.getKey()+".total.source_entry", entry.getValue(), entry.getKey()));
              first = false;
            }
            final Component finalSources = sourcesComponent;
            sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".total.success", first_player.getName(), get_result, Component.translatable(skill.getDisplayId()), finalSources), true);
          } else {
            final String getSource = source;
            sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", first_player.getName(), get_result, Component.translatable(skill.getDisplayId()), getSource), true);
          }
          return get_result.intValue();
        }
        Number skill_level_prev = 0;
        Number skill_level_now = 0;
        for (ServerPlayer player : players) {
            skill_level_prev = SkillUtil.getSkillLevel(player, skill, source);
            skill_level_now = action.perform(player, skill, amount, source);
            if (!skill_level_now.equals(skill_level_prev))
                count += 1;
        }
        final int final_count = count;
        final double result_diff;
        if (action == Action.SET) {
            result_diff = skill_level_now.doubleValue();
        } else if (action == Action.REMOVE) {
            result_diff = skill_level_prev.doubleValue() - skill_level_now.doubleValue();
        } else {
            result_diff = skill_level_now.doubleValue() - skill_level_prev.doubleValue();
        }
        final Number result_value = switch (skill.getType()) {
            case INT -> (int) Math.round(result_diff);
            case FLOAT -> (float) result_diff;
            case DOUBLE -> result_diff;
        };
        if (count == 0) {
            if (player_amount == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create(
                    Component.translatable(
                        action.getKey() + ".single.failure",
                        Component.translatable(skill.getDisplayId()),
                        first_player.getName()
                    )
                );
            } else {
                throw ERROR_NO_ACTION_PERFORMED.create(
                    Component.translatable(
                        action.getKey() + ".multiple.failure",
                        Component.translatable(skill.getDisplayId()),
                        player_amount
                    )
                );
            }
        } else {
            final String actionSource = source;
            if (player_amount == 1) {
                sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", result_value, Component.translatable(skill.getDisplayId()), first_player.getName(), actionSource), true);
            } else {
                sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".multiple.success", amount, Component.translatable(skill.getDisplayId()), final_count, actionSource), true);
            }
        }
        return count;
    }

    static enum Action {
        ADD("add") {
            protected Number perform(ServerPlayer player, Skill skill, Number amount, String source) {
                SkillUtil.addSkillLevel(player, skill, amount, source);
                return SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        REMOVE("remove") {
            protected Number perform(ServerPlayer player, Skill skill, Number amount, String source) {
                SkillUtil.addSkillLevel(player, skill, -amount.doubleValue(), source);
                return SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        GET("get") {
            protected Number perform(ServerPlayer player, Skill skill, Number amount, @Nullable String source) {
                return Objects.isNull(source) ? 
                    SkillUtil.getTotalSkillLevel(player, skill)
                    : SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        SET("set") {
            protected Number perform(ServerPlayer player, Skill skill, Number amount, String source) {
                SkillUtil.setSkillLevel(player, skill, amount, source);
                return SkillUtil.getSkillLevel(player, skill, source);
            }
        };

        private final String key;

        Action(final String key) {
            this.key = "commands.skill." + key;
        }

        protected String getKey() {
            return this.key;
        }

        protected abstract Number perform(ServerPlayer player, Skill skill, Number amount, String source);

    }
}
