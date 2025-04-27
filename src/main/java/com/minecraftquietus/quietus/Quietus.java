package com.minecraftquietus.quietus;


import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.event.QuietusEvents;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.minecraftquietus.quietus.block.QuietusBlocks;
import com.minecraftquietus.quietus.effects.QuietusEffects;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.potion.QuietusPotions;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Quietus.MODID)
public class Quietus
{
    // Define mod id
    public static final String MODID = "quietus";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "quietus" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Creates a creative tab with the id "quietus:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.quietus")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> QuietusItems.HARDENED_FUR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(QuietusItems.HARDENED_FUR.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Quietus(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        //ITEMS.register(modEventBus);
        QuietusItems.register(modEventBus);
        QuietusBlocks.register(modEventBus);
        QuietusPotions.register(modEventBus);
        QuietusEffects.register(modEventBus);
        //register renderer
        /*if (!FMLEnvironment.dist.isClient()) {
            return;
        }

        ModLoadingContext.get().getActiveContainer()
                .registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);*/
        modEventBus.register(ConfigHandler.class);
        NeoForge.EVENT_BUS.register(Ore_Vision.class);
        modEventBus.addListener(this::registerPipeline);
        NeoForge.EVENT_BUS.addListener(QuietusEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(QuietusEvents::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(QuietusEvents::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(QuietusEvents::onWorldRenderLast);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Quietus) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(QuietusItems.EXAMPLE_BLOCK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(QuietusItems.HARDENED_FUR);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(QuietusItems.COPPER_HELMET);
            event.accept(QuietusItems.COPPER_CHESTPLATE);
            event.accept(QuietusItems.COPPER_LEGGINGS);
            event.accept(QuietusItems.COPPER_BOOTS);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    private void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(Ore_Vision.LINES_NO_DEPTH);
    }


    public static class ConfigHandler {
        private static int cachedRange=10;
        private static int cachedOreColor=0x00FF00;

        @SubscribeEvent
        public static void onConfigLoading(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() == Config.CLIENT_SPEC) {
                // Initialize cached values when the config is first loaded
                cachedRange = Config.CLIENT.range.get();
                cachedOreColor = Config.CLIENT.oreColor.get();
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() == Config.CLIENT_SPEC) {
                // Update cached values when the config is reloaded (e.g., via /reload)
                cachedRange = Config.CLIENT.range.get();
                cachedOreColor = Config.CLIENT.oreColor.get();
            }
        }

        // Safe getters for cached values
        public static int getRange() { return cachedRange; }
        public static int getOreColor() { return cachedOreColor; }

    } 
}
