package com.quietus.server.commands;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.quietus.core.QuietusRegistries;
import com.quietus.core.skill.Skill;
import com.quietus.util.SkillUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class SkillCommands {

    private static final DynamicCommandExceptionType ERROR_INVALID_SKILL = new DynamicCommandExceptionType(
        arg1 -> Component.translatableEscape("skill.skillNotFound", arg1)
    );
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(arg1 -> (Component)arg1);

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
                    Commands.argument("amount", IntegerArgumentType.integer(0))
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.ADD, 
                        context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value(),
                        IntegerArgumentType.getInteger(context, "amount"),
                        StringArgumentType.getString(context, "source")
                      ))
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
                    Commands.argument("amount", IntegerArgumentType.integer(0))
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.REMOVE, 
                        context.getSource().registryAccess().lookupOrThrow(QuietusRegistries.SKILL_REGISTRY_KEY).getOrThrow(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)).value(),
                        IntegerArgumentType.getInteger(context, "amount"),
                        StringArgumentType.getString(context, "source")
                      ))
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
                    Commands.argument("value", IntegerArgumentType.integer(0))
                    .then(
                      Commands.argument("source", StringArgumentType.string())
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.SET, 
                        QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)), 
                        IntegerArgumentType.getInteger(context, "value"),
                        StringArgumentType.getString(context, "source")
                      ))
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
    private static int perform(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillCommands.Action action, Skill skill, int amount, @Nullable String source) throws CommandSyntaxException {
        int count = 0;
        int player_amount = players.size();
        ServerPlayer first_player = (ServerPlayer)players.toArray()[0];

        if (action == Action.GET) {
          final int get_result = action.perform(first_player, skill, player_amount, source);
          sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", first_player.getName(), get_result, Component.translatable(skill.getDescriptionId())), true);
          return get_result;
        }
        int skill_level_prev = 0;
        int skill_level_now = 0;
        for (ServerPlayer player : players) {
            skill_level_prev = SkillUtil.getSkillLevel(player, skill, source);
            skill_level_now = action.perform(player, skill, amount, source);
            if (skill_level_now != skill_level_prev)
                count += 1;
        }
        final int final_count = count;
        final int result_value;
        if (action == Action.SET) {
            result_value = skill_level_now;
        } else if (action == Action.REMOVE) {
            result_value = skill_level_prev - skill_level_now;
        } else {
            result_value = skill_level_now - skill_level_prev;
        }
        if (count == 0) {
            if (player_amount == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create(
                    Component.translatable(
                        action.getKey() + ".single.failure",
                        Component.translatable(skill.getDescriptionId()),
                        first_player.getName()
                    )
                );
            } else {
                throw ERROR_NO_ACTION_PERFORMED.create(
                    Component.translatable(
                        action.getKey() + ".multiple.failure",
                        Component.translatable(skill.getDescriptionId()),
                        player_amount
                    )
                );
            }
        } else {
            if (player_amount == 1) {
                sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", result_value, Component.translatable(skill.getDescriptionId()), first_player.getName()), true);
            } else {
                sourceStack.sendSuccess(() -> Component.translatable(action.getKey()+".multiple.success", amount, Component.translatable(skill.getDescriptionId()), final_count), true);
            }
        }
        if (action == Action.GET) return result_value;
        return count;
    }

    static enum Action {
        ADD("add") {
            protected int perform(ServerPlayer player, Skill skill, int amount, String source) {
                SkillUtil.addSkillLevel(player, skill, amount, source);
                return SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        REMOVE("remove") {
            protected int perform(ServerPlayer player, Skill skill, int amount, String source) {
                SkillUtil.addSkillLevel(player, skill, -amount, source);
                return SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        GET("get") {
            protected int perform(ServerPlayer player, Skill skill, int amount, @Nullable String source) {
                return Objects.isNull(source) ? 
                    SkillUtil.getTotalSkillLevel(player, skill)
                    : SkillUtil.getSkillLevel(player, skill, source);
            }
        },
        SET("set") {
            protected int perform(ServerPlayer player, Skill skill, int amount, String source) {
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

        protected abstract int perform(ServerPlayer player, Skill skill, int amount, String source);

    }
}
