package com.minecraftquietus.server.commands;

import java.util.Collection;
import java.util.List;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.core.skill.Skill;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SkillCommands {

    private static final DynamicCommandExceptionType ERROR_INVALID_SKILL = new DynamicCommandExceptionType(
        s -> Component.translatableEscape("skill.skillNotFound", s)
    );
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(s -> (Component)s);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("skill")
              .requires(thing -> thing.hasPermission(2))
              .then(
                Commands.literal("add")
                .then(
                  Commands.argument("targets", EntityArgument.players())
                  .then(
                    Commands.argument("skill", ResourceKeyArgument.key(QuietusRegistries.SKILL_REGISTRY_KEY))
                    .then(
                      Commands.argument("amount", IntegerArgumentType.integer(0))
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.ADD, 
                        QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)), 
                        IntegerArgumentType.getInteger(context, "amount")
                      ))
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
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.REMOVE, 
                        QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)), 
                        IntegerArgumentType.getInteger(context, "amount")
                      ))
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
                      QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL))
                    ))
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
                      .executes(context -> perform(
                        (CommandSourceStack)context.getSource(), 
                        EntityArgument.getPlayers(context, "targets"), 
                        Action.SET, 
                        QuietusRegistries.SKILL_REGISTRY.getValue(ResourceKeyArgument.getRegistryKey(context, "skill", QuietusRegistries.SKILL_REGISTRY_KEY, ERROR_INVALID_SKILL)), 
                        IntegerArgumentType.getInteger(context, "value")
                      ))
                    )
                  )
                )
              )
        );
    }

    private static int perform(CommandSourceStack source, ServerPlayer player, SkillCommands.Action action, Skill skill) throws CommandSyntaxException {
        return perform(source, (Collection<ServerPlayer>)List.of(player), action, skill, 0);
    }
    private static int perform(CommandSourceStack source, Collection<ServerPlayer> players, SkillCommands.Action action, Skill skill, int amount) throws CommandSyntaxException {
        int count = 0;
        int player_amount = players.size();
        ServerPlayer first_player = (ServerPlayer)players.toArray()[0];

        int skill_level_prev = 0;
        int skill_level_now = 0;
        for (ServerPlayer player : players) {
            skill_level_prev = SkillUtil.getSkillLevel(player, skill);
            skill_level_now = action.perform(player, skill, amount);
            if (skill_level_now != skill_level_prev)
                count += 1;
        }
        final int final_count = count;
        final int result_value;
        if (action == Action.SET || action == Action.GET) {
            result_value = skill_level_now;
        } else if (action == Action.REMOVE) {
            result_value = skill_level_prev - skill_level_now;
        } else {
            result_value = skill_level_now - skill_level_prev;
        }
        if (action == Action.GET) {
            source.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", first_player.getName(), result_value, Component.translatable(skill.getDescriptionId())), true);
        } else {
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
                    source.sendSuccess(() -> Component.translatable(action.getKey()+".single.success", result_value, Component.translatable(skill.getDescriptionId()), first_player.getName()), true);
                } else {
                    source.sendSuccess(() -> Component.translatable(action.getKey()+".multiple.success", amount, Component.translatable(skill.getDescriptionId()), final_count), true);
                }
            }
        }
        if (action == Action.GET) return result_value;
        return count;
    }

    static enum Action {
        ADD("add") {
            protected int perform(ServerPlayer player, Skill skill, int amount) {
                SkillUtil.addSkillLevel(player, skill, amount);
                return SkillUtil.getSkillLevel(player, skill);
            }
        },
        REMOVE("remove") {
            protected int perform(ServerPlayer player, Skill skill, int amount) {
                SkillUtil.addSkillLevel(player, skill, -amount);
                return SkillUtil.getSkillLevel(player, skill);
            }
        },
        GET("get") {
            protected int perform(ServerPlayer player, Skill skill, int amount) {
                return SkillUtil.getSkillLevel(player, skill);
            }
        },
        SET("set") {
            protected int perform(ServerPlayer player, Skill skill, int amount) {
                SkillUtil.setSkillLevel(player, skill, amount);
                return SkillUtil.getSkillLevel(player, skill);
            }
        };

        private final String key;

        Action(final String key) {
            this.key = "commands.skill." + key;
        }

        protected String getKey() {
            return this.key;
        }

        protected abstract int perform(ServerPlayer player, Skill skill, int amount);

    }
}
