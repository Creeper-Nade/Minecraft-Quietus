package com.quietus.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Locale;

public final class WorldThreatCommands {
    private WorldThreatCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quietus")
                .then(Commands.literal("threat")
                        .executes(context -> show(context.getSource()))
                        .then(Commands.literal("get")
                                .executes(context -> getThreat(context.getSource())))
                        .then(Commands.literal("set")
                                .requires(WorldThreatCommands::canManage)
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(
                                                WorldThreatData.MIN_THREAT, WorldThreatData.MAX_THREAT))
                                        .executes(context -> setThreat(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "value")))))
                        .then(Commands.literal("stage")
                                .then(Commands.literal("get")
                                        .executes(context -> getStage(context.getSource())))
                                .then(Commands.literal("set")
                                        .requires(WorldThreatCommands::canManage)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(
                                                        WorldThreatData.MIN_STAGE, WorldThreatData.MAX_STAGE))
                                                .executes(context -> setStage(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "value")))))
                        )
                        .then(Commands.literal("volatility")
                                .then(Commands.literal("get")
                                        .executes(context -> getVolatility(context.getSource())))
                                .then(Commands.literal("set")
                                        .requires(WorldThreatCommands::canManage)
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(2.0D, 50.0D))
                                                .executes(context -> setVolatility(
                                                        context.getSource(),
                                                        DoubleArgumentType.getDouble(context, "value")))))
                        )
                ));
    }

    private static boolean canManage(CommandSourceStack source) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS));
    }

    private static int show(CommandSourceStack source) {
        WorldThreatData data = WorldThreatData.get(source.getServer());
        source.sendSuccess(() -> Component.translatable(
                "commands.quietus.threat.query",
                format(data.getThreat()), data.getStage(), format(data.getVolatility())), false);
        return (int) Math.round(data.getThreat());
    }

    private static int setThreat(CommandSourceStack source, double value) {
        WorldThreatData data = WorldThreatData.get(source.getServer());
        data.setThreat(value);
        source.sendSuccess(() -> Component.translatable("commands.quietus.threat.set", format(data.getThreat())), true);
        return (int) Math.round(data.getThreat());
    }

    private static int getThreat(CommandSourceStack source) {
        double value = WorldThreatData.get(source.getServer()).getThreat();
        source.sendSuccess(() -> Component.translatable("commands.quietus.threat.get", format(value)), false);
        return (int) Math.round(value);
    }

    private static int setStage(CommandSourceStack source, int value) {
        WorldThreatData data = WorldThreatData.get(source.getServer());
        data.setStage(value);
        for (var level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    WorldThreatSystem.applyStageHealth(mob, value, false);
                }
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.quietus.threat.stage.set", value), true);
        return value;
    }

    private static int getStage(CommandSourceStack source) {
        int value = WorldThreatData.get(source.getServer()).getStage();
        source.sendSuccess(() -> Component.translatable("commands.quietus.threat.stage.get", value), false);
        return value;
    }

    private static int setVolatility(CommandSourceStack source, double value) {
        WorldThreatData data = WorldThreatData.get(source.getServer());
        data.setVolatility(value);
        source.sendSuccess(() -> Component.translatable(
                "commands.quietus.threat.volatility.set", format(data.getVolatility())), true);
        return (int) Math.round(data.getVolatility());
    }

    private static int getVolatility(CommandSourceStack source) {
        double value = WorldThreatData.get(source.getServer()).getVolatility();
        source.sendSuccess(() -> Component.translatable("commands.quietus.threat.volatility.get", format(value)), false);
        return (int) Math.round(value);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
