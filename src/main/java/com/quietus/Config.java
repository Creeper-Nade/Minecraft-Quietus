package com.quietus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Quietus.MODID)
public class Config
{

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    public static final Loot LOOT = new Loot(BUILDER);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.getValue(Identifier.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static class Loot {
        public final ModConfigSpec.DoubleValue trialChamberVaultChance;
        public final ModConfigSpec.DoubleValue ominousTrialChamberVaultChance;
        public final ModConfigSpec.DoubleValue mineshaftMinecartChance;
        public final ModConfigSpec.DoubleValue monsterRoomChestChance;
        public final ModConfigSpec.DoubleValue strongholdLibraryChance;
        public final ModConfigSpec.DoubleValue strongholdCorridorChance;

        private Loot(ModConfigSpec.Builder builder) {
            builder.push("loot");
            builder.comment(
                    "Chance for an Amethyst Upgrade Smithing Template to be added to each generated reward.",
                    "Values range from 0.0 (disabled) to 1.0 (guaranteed).",
                    "Run /reload after changing these values so the loot tables are rebuilt."
            );

            trialChamberVaultChance = chance(builder, "trialChamberVaultChance", 0.14,
                    "Normal Trial Chamber vault reward chance.");
            ominousTrialChamberVaultChance = chance(builder, "ominousTrialChamberVaultChance", 0.25,
                    "Ominous Trial Chamber vault reward chance.");
            mineshaftMinecartChance = chance(builder, "mineshaftMinecartChance", 0.07,
                    "Abandoned mineshaft chest minecart chance.");
            monsterRoomChestChance = chance(builder, "monsterRoomChestChance", 0.03,
                    "Underground monster room (dungeon) chest chance.");
            strongholdLibraryChance = chance(builder, "strongholdLibraryChance", 1.0,
                    "Stronghold library chest chance.");
            strongholdCorridorChance = chance(builder, "strongholdCorridorChance", 0.1,
                    "Stronghold corridor chest chance.");

            builder.pop();
        }

        private static ModConfigSpec.DoubleValue chance(ModConfigSpec.Builder builder, String name,
                                                         double defaultValue, String comment) {
            return builder.comment(comment).defineInRange(name, defaultValue, 0.0, 1.0);
        }
    }


    //config for spelunker render
    public static class Client {
        public final ModConfigSpec.IntValue range;
        public final ModConfigSpec.ConfigValue<Integer> oreColor;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("OreVision");
            range = builder
                    .comment("Maximum scan range (blocks)")
                    .defineInRange("range", 7, 7, 64);
            oreColor = builder
                    .comment("Outline color (hex format, e.g., 0x00FF00 for green)")
                    .define("oreColor", 0x00FF00);
            builder.pop();
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }
}
