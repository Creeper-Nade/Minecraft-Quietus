package com.minecraftquietus.quietus;


import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileSmallRenderer;
import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.core.DeathRevamp.GhostDeath;
import com.minecraftquietus.quietus.core.DeathRevamp.GhostMovementHandler;
import com.minecraftquietus.quietus.data.DataGenerator;
import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.enchantment.QuietusEnchantmentComponent;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.QuietusAttributes;
import com.minecraftquietus.quietus.util.QuietusGameRules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
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

import com.minecraftquietus.commands.QuietusCommands;
import com.minecraftquietus.quietus.block.QuietusBlocks;
import com.minecraftquietus.quietus.effects.QuietusMobEffects;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.potion.QuietusPotions;
import com.minecraftquietus.quietus.skill.QuietusSkills;
import com.minecraftquietus.quietus.entity.QuietusEntityTypes;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.event_listener.QuietusCommonEvents;
import com.minecraftquietus.quietus.event_listener.QuietusIModBusEvent;
import com.minecraftquietus.quietus.event_listener.SpawnEvent;
import com.minecraftquietus.quietus.client.hud.ManaHudOverlay;
import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileRenderer;


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
        
        // register registries
        modEventBus.addListener(QuietusRegistries::registerRegistries);

        QuietusItems.init();

        // Registries
        QuietusItems.register(modEventBus);
        QuietusComponents.register(modEventBus);
        QuietusEnchantmentComponent.register(modEventBus);
        QuietusBlocks.register(modEventBus);
        QuietusSounds.register(modEventBus);
        QuietusPotions.register(modEventBus);
        QuietusMobEffects.register(modEventBus);
        QuietusEntityTypes.register(modEventBus);
        QuietusProjectiles.register(modEventBus);
        QuietusSkills.register(modEventBus);

        // register commands
        NeoForge.EVENT_BUS.addListener(QuietusCommands::registerCommands);

        // register renderers
        modEventBus.register(ConfigHandler.class);
        NeoForge.EVENT_BUS.register(Ore_Vision.class);
        modEventBus.addListener(this::registerPipeline);

        NeoForge.EVENT_BUS.register(QuietusCommonEvents.class);
       // NeoForge.EVENT_BUS.register(PlayerDeathHandler.class);
        NeoForge.EVENT_BUS.register(GhostMovementHandler.class);
        NeoForge.EVENT_BUS.register(GhostDeath.class);
        // CreeperNade: The SpawnEvent class is never registered, so I'm adding it here for you 👀
        NeoForge.EVENT_BUS.register(SpawnEvent.class);
        //NeoForge.EVENT_BUS.register(PlayerDeathHandler.class);
        modEventBus.register(QuietusIModBusEvent.class);
        modEventBus.register(DataGenerator.class);

// Register our mana attachment
        //ATTACHMENTS.register("mana_component", () -> ManaComponent.MANA_ATTACHMENT);
        QuietusAttachments.REGISTRAR.register(modEventBus);
        QuietusAttributes.QUIETUS_ATTRIBUTES.register(modEventBus);
        //NeoForge.EVENT_BUS.addListener(QuietusIModBusEvent::PayloadHandlerRegistration);

        // Register client-side HUD
        NeoForge.EVENT_BUS.addListener(ManaHudOverlay::onRenderGui);
        NeoForge.EVENT_BUS.addListener(ManaHudOverlay::onWorldJoin);
        NeoForge.EVENT_BUS.addListener(ManaHudOverlay::onPlayerClone);

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
            QuietusItems.registerWeatheringMappings();
            QuietusGameRules.Init();

        });
    }

    // Creative mode tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        QuietusItems.addCreativeTabItems(event, event.getTabKey());
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
            LOGGER.info("    ########    ");
            LOGGER.info("  ##        ##  ");
            LOGGER.info(" #    oo     # ");
            LOGGER.info("#            #");
            LOGGER.info("#   ------   #");
            LOGGER.info(" #  \\__/  # ");
            LOGGER.info("  ##        ##  ");
            LOGGER.info("    ########    ");
            EntityRenderers.register(QuietusProjectiles.AMETHYST_PROJECTILE.get(), AmethystProjectileRenderer::new);
            EntityRenderers.register(QuietusProjectiles.SMALL_AMETHYST_PROJECTILE.get(), AmethystProjectileSmallRenderer::new);
        }
    }

    private void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(Ore_Vision.LINES_NO_DEPTH);
        event.registerPipeline(GhostDeath.ghostPipeline);
        //event.registerPipeline(QuietusEmissiveLayer.FIXED_EMISSIVE);
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
            if (!player.hasEffect(QuietusMobEffects.SPELUNKING_EFFECT)) return 0;

            // Amplifier is 0 for level I, 1 for level II, etc.
            int amplifier = player.getEffect(QuietusMobEffects.SPELUNKING_EFFECT).getAmplifier();
            int rangePerLevel = 3; // Additional blocks per level

            return cachedRange + (amplifier * rangePerLevel);
        }

    } 
}
