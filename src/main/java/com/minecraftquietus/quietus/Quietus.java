package com.minecraftquietus.quietus;


import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.event.QuietusCommonEvents;
import com.minecraftquietus.quietus.event.QuietusIModBusEvent;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.QuietusAttributes;
import com.minecraftquietus.quietus.util.mana.ManaComponent;
import com.minecraftquietus.quietus.util.mana.ManaHudOverlay;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
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
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.minecraftquietus.quietus.item.WeatheringIronArmorItem;
import com.minecraftquietus.quietus.item.WeatheringIronItems;
import com.minecraftquietus.quietus.item.WeatheringItem;
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



    //MANA codec
    /*private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
    private static final Codec<ManaComponent> MANA_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("mana").forGetter(ManaComponent::getMana),
                    Codec.INT.fieldOf("maxMana").forGetter(ManaComponent::getMaxMana)
            ).apply(instance, ManaComponent::new)
    );
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<ManaComponent>> MANA =
            ATTACHMENTS.register("mana", () ->
                    AttachmentType.builder(() -> new ManaComponent())
                            .serialize(MANA_CODEC)
                            .build()
            );*/





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
        modEventBus.register(ConfigHandler.class);
        NeoForge.EVENT_BUS.register(Ore_Vision.class);
        modEventBus.addListener(this::registerPipeline);

        NeoForge.EVENT_BUS.register(QuietusCommonEvents.class);
        modEventBus.register(QuietusIModBusEvent.class);
        NeoForge.EVENT_BUS.addListener(QuietusCommonEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(QuietusCommonEvents::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(QuietusCommonEvents::onBlockPlace);
        //NeoForge.EVENT_BUS.addListener(QuietusEvents::onWorldRenderLast);

// Register our mana attachment
        //ATTACHMENTS.register("mana_component", () -> ManaComponent.MANA_ATTACHMENT);
        QuietusAttachments.ATTACHMENTS.register(modEventBus);
        QuietusAttributes.QUIETUS_ATTRIBUTES.register(modEventBus);
        //NeoForge.EVENT_BUS.addListener(QuietusIModBusEvent::PayloadHandlerRegistration);

        // Register client-side HUD
        NeoForge.EVENT_BUS.addListener(ManaHudOverlay::onRenderGui);

        /*NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (!event.getEntity().level().isClientSide()) return;
            event.getEntity().getData(ManaComponent.ATTACHMENT).tick(event.getEntity());
        });*/

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
        event.enqueueWork(() -> {
            // Register copper items in the OXIDATION_MAP of WeatheringCopperItems
            WeatheringCopperItems.registerWeathering(QuietusItems.COPPER_BOOTS.get(), QuietusItems.EXPOSED_COPPER_BOOTS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.EXPOSED_COPPER_BOOTS.get(), QuietusItems.WEATHERED_COPPER_BOOTS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.WEATHERED_COPPER_BOOTS.get(), QuietusItems.OXIDIZED_COPPER_BOOTS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.COPPER_LEGGINGS.get(), QuietusItems.EXPOSED_COPPER_LEGGINGS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.EXPOSED_COPPER_LEGGINGS.get(), QuietusItems.WEATHERED_COPPER_LEGGINGS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.WEATHERED_COPPER_LEGGINGS.get(), QuietusItems.OXIDIZED_COPPER_LEGGINGS.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.COPPER_CHESTPLATE.get(), QuietusItems.EXPOSED_COPPER_CHESTPLATE.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.EXPOSED_COPPER_CHESTPLATE.get(), QuietusItems.WEATHERED_COPPER_CHESTPLATE.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.WEATHERED_COPPER_CHESTPLATE.get(), QuietusItems.OXIDIZED_COPPER_CHESTPLATE.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.COPPER_HELMET.get(), QuietusItems.EXPOSED_COPPER_HELMET.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.EXPOSED_COPPER_HELMET.get(), QuietusItems.WEATHERED_COPPER_HELMET.get());
            WeatheringCopperItems.registerWeathering(QuietusItems.WEATHERED_COPPER_HELMET.get(), QuietusItems.OXIDIZED_COPPER_HELMET.get());
            // Register iron items in the OXIDATION_MAP of WeatheringIronItems
            WeatheringIronItems.registerWeathering(Items.IRON_BOOTS, QuietusItems.EXPOSED_IRON_BOOTS.get());
            WeatheringIronItems.registerWeathering(QuietusItems.EXPOSED_IRON_BOOTS.get(), QuietusItems.WEATHERED_IRON_BOOTS.get());
            WeatheringIronItems.registerWeathering(QuietusItems.WEATHERED_IRON_BOOTS.get(), QuietusItems.OXIDIZED_IRON_BOOTS.get());
            WeatheringIronItems.registerWeathering(Items.IRON_LEGGINGS, QuietusItems.EXPOSED_IRON_LEGGINGS.get());
            WeatheringIronItems.registerWeathering(QuietusItems.EXPOSED_IRON_LEGGINGS.get(), QuietusItems.WEATHERED_IRON_LEGGINGS.get());
            WeatheringIronItems.registerWeathering(QuietusItems.WEATHERED_IRON_LEGGINGS.get(), QuietusItems.OXIDIZED_IRON_LEGGINGS.get());
            WeatheringIronItems.registerWeathering(Items.IRON_CHESTPLATE, QuietusItems.EXPOSED_IRON_CHESTPLATE.get());
            WeatheringIronItems.registerWeathering(QuietusItems.EXPOSED_IRON_CHESTPLATE.get(), QuietusItems.WEATHERED_IRON_CHESTPLATE.get());
            WeatheringIronItems.registerWeathering(QuietusItems.WEATHERED_IRON_CHESTPLATE.get(), QuietusItems.OXIDIZED_IRON_CHESTPLATE.get());
            WeatheringIronItems.registerWeathering(Items.IRON_HELMET, QuietusItems.EXPOSED_IRON_HELMET.get());
            WeatheringIronItems.registerWeathering(QuietusItems.EXPOSED_IRON_HELMET.get(), QuietusItems.WEATHERED_IRON_HELMET.get());
            WeatheringIronItems.registerWeathering(QuietusItems.WEATHERED_IRON_HELMET.get(), QuietusItems.OXIDIZED_IRON_HELMET.get());
            // Register vanilla iron items into ExtraWeatheringItem, so they will weather and is used in checking weathering process by other WeatheringItem: 
            /* Arguments: 
             *  the Item object, int array of possible WeatherStates(picked randomly from the array), float of oxidation chance, the Class<?> enumeration of WeatherState class this item should have (just take from existing classes as shown below)
            */
            Class<?> class_weathering_iron_armor_item_enum = WeatheringIronItems.IronWeatherState.class;
            WeatheringItem.registerExtraWeatheringItem(Items.IRON_BOOTS, new int[]{0}, WeatheringIronArmorItem.OXIDATION_CHANCE * 0.9f, class_weathering_iron_armor_item_enum);
            WeatheringItem.registerExtraWeatheringItem(Items.IRON_LEGGINGS, new int[]{0}, WeatheringIronArmorItem.OXIDATION_CHANCE * 0.9f, class_weathering_iron_armor_item_enum);
            WeatheringItem.registerExtraWeatheringItem(Items.IRON_CHESTPLATE, new int[]{0}, WeatheringIronArmorItem.OXIDATION_CHANCE * 0.9f, class_weathering_iron_armor_item_enum);
            WeatheringItem.registerExtraWeatheringItem(Items.IRON_HELMET, new int[]{0}, WeatheringIronArmorItem.OXIDATION_CHANCE * 0.9f, class_weathering_iron_armor_item_enum);
        });
    }

    // Creative mode tabs
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



    //for handling the spelunker render configuration
    public static class ConfigHandler {
        private static int cachedRange=7;

        @SubscribeEvent
        public static void onConfigLoading(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() == Config.CLIENT_SPEC) {
                // Initialize cached values when the config is first loaded
                cachedRange = Config.CLIENT.range.get();
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() == Config.CLIENT_SPEC) {
                // Update cached values when the config is reloaded (e.g., via /reload)
                cachedRange = Config.CLIENT.range.get();
            }
        }

        // Safe getters for cached values
        public static int getRange(LocalPlayer player) {
            if (!player.hasEffect(QuietusEffects.SPELUNKING_EFFECT)) return 0;

            // Amplifier is 0 for level I, 1 for level II, etc.
            int amplifier = player.getEffect(QuietusEffects.SPELUNKING_EFFECT).getAmplifier();
            int rangePerLevel = 3; // Additional blocks per level

            return cachedRange + (amplifier * rangePerLevel);
        }

    } 
}
