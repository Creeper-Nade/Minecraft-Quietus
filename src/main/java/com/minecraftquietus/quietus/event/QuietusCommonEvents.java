package com.minecraftquietus.quietus.event;

import com.minecraftquietus.quietus.core.DeathRevamp.GhostDeathScreen;
import com.minecraftquietus.quietus.effects.QuietusEffects;
import com.minecraftquietus.quietus.effects.spelunker.Ore_Vision;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.equipment.RetaliatesOnDamaged;
import com.minecraftquietus.quietus.item.tool.AmmoProjectileWeaponItem;
import com.minecraftquietus.quietus.potion.QuietusPotions;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.QuietusGameRules;
import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.util.mana.ManaUtil;
import com.minecraftquietus.quietus.tags.QuietusTags;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

import java.util.HashMap;
import java.util.Map;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;


@EventBusSubscriber(modid = MODID)
public class QuietusCommonEvents {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickItem event) {
        if (!event.getItemStack().has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            Player player = event.getEntity();
            ItemStack itemstack = event.getItemStack();
            
            boolean flag = false;
            if (event.getSide() == LogicalSide.CLIENT) { // Client side
                int current_mana = ClientPayloadHandler.getInstance().GetManaFromPack();
                int current_max_mana = ClientPayloadHandler.getInstance().GetMaxManaFromPack();
                //LOGGER.info("Expected mana consumption on use: "+mana_consume + " | Player's mana: " + ClientPayloadHandler.getInstance().GetManaFromPack());
                int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(current_mana, current_max_mana,itemstack,player.level());
                flag = current_mana >= mana_consume;
                if (!flag) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    player.stopUsingItem();
                    event.setCanceled(true);
                } else {
                }
            } else {
                int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(ManaUtil.getMana(player), ManaUtil.getMaxMana(player),itemstack,player.level());
                //LOGGER.info("Expected mana consumption on use: "+mana_consume + " | Player's mana: " + Mana.getMana(player));
                flag = ManaUtil.getMana(player) >= mana_consume;
                if (!flag) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    player.stopUsingItem();
                    event.setCanceled(true);}
                else {
                    if (itemstack.getItem().getUseDuration(itemstack, player) == 0 ) {
                        ManaUtil.get(player).consumeMana(mana_consume, player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        ItemStack itemstack = event.getItem();
        LivingEntity entity = event.getEntity();
        if (!itemstack.has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            if (!entity.level().isClientSide()) {
                int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(ManaUtil.getMana(entity), ManaUtil.getMaxMana(entity),itemstack,entity.level());
                if (itemstack.getItem() instanceof BowItem || itemstack.getItem() instanceof AmmoProjectileWeaponItem) {
                    ManaUtil.get(entity).consumeMana(mana_consume, entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        ItemStack itemstack = event.getItem();
        //LOGGER.info("Tick! "+event.getDuration() + " | " + itemstack.getUseDuration(event.getEntity()));
        LivingEntity entity = event.getEntity();
        if (!itemstack.has(QuietusComponents.USES_MANA.get())) { // Item does not use mana
            return;
        } else {
            if (!entity.level().isClientSide()) { // Server side
                int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(ManaUtil.getMana(entity), ManaUtil.getMaxMana(entity),itemstack,entity.level());
                if (ManaUtil.getMana(entity) < mana_consume) {
                    entity.stopUsingItem();
                    event.setCanceled(true);
                }
                if (event.getDuration() == 1) { // about to finish item
                    ManaUtil.get(entity).consumeMana(mana_consume, entity);
                }
            } else { // Client side
                if (entity instanceof Player) {
                    int current_mana = ClientPayloadHandler.getInstance().GetManaFromPack();
                    int current_max_mana = ClientPayloadHandler.getInstance().GetMaxManaFromPack();
                    int mana_consume = itemstack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(current_mana, current_max_mana,itemstack,entity.level());
                    if (current_mana < mana_consume) {
                        entity.stopUsingItem();
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            //System.out.println(serverPlayer);
            PlayerData.ManapackToPlayer(serverPlayer);
        } 
    }

    @SubscribeEvent
    public static void registerBrewingRecipe(RegisterBrewingRecipesEvent event)
    {
        PotionBrewing.Builder builder=event.getBuilder();

        builder.addMix(Potions.THICK, Items.GLOW_BERRIES, QuietusPotions.SPELUNKING);
        builder.addMix(QuietusPotions.SPELUNKING, Items.REDSTONE, QuietusPotions.LONG_SPELUNKING); // longer duration of potion of spelunking
        builder.addMix(QuietusPotions.SPELUNKING, Items.GLOW_INK_SAC, QuietusPotions.STRONG_SPELUNKING);
//placeholder ingredients for instant mana potion, will be changed to a custom ingredient in the future
        builder.addMix(Potions.AWKWARD, Items.QUARTZ, QuietusPotions.LESSER_INSTANT_MANA);
    }

    @SubscribeEvent
    public static void onEnchantment(PlayerEnchantItemEvent event)
    {
        if(event.getEnchantedItem().is(QuietusTags.Items.MAGIC_ENCHANTABLE))
        {
            event.getEnchantedItem().set(DataComponents.DAMAGE,0);
        }
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(Tags.Blocks.ORES)) {
            Ore_Vision.RemoveSingleBlock(event);
        }
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getState().is(Tags.Blocks.ORES)) {
            Ore_Vision.AddSingleBlock(event);
        }

    }

    /* @SubscribeEvent
    public static void onArmorHurt(ArmorHurtEvent event) {

        LivingEntity entity = event.getEntity();
        Map<EquipmentSlot, ArmorEntry> armorEntryMap = event.getArmorMap();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!armorEntryMap.containsKey(slot)) continue; // skip slots without armor
            ItemStack itemstack = armorEntryMap.get(slot).armorItemStack;
            float damage = event.getNewDamage(slot);
            // Worn armor only
            if (slot == EquipmentSlot.FEET || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.HEAD) {
                
                if (itemstack.getItem() instanceof RetaliatesOnDamaged retaliatingItem) {
                    damage = retaliatingItem.onArmorHurt(damage, armorEntryMap, slot, entity);
                }
            }

            event.setNewDamage(slot, damage); // update damage to event
        }
    } */
    @SubscribeEvent
    public static void onEntityHurtPost(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        if (event.getReduction(DamageContainer.Reduction.ARMOR) > 0.0f && event.getReduction(DamageContainer.Reduction.INVULNERABILITY) == 0.0f) { // armor reducted damage
            Map<EquipmentSlot, ItemStack> armorMap = new HashMap<>(EquipmentSlot.values().length);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot == EquipmentSlot.FEET || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.HEAD)
                    armorMap.put(slot, entity.getItemBySlot(slot));
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemstack = armorMap.getOrDefault(slot, ItemStack.EMPTY);
                if (itemstack.getItem() instanceof RetaliatesOnDamaged retaliatingItem) {
                    retaliatingItem.onArmorHurt(damage, armorMap, slot, entity);
                }
            }
        }
    }


    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(player==null) return;

        if (Minecraft.getInstance().level != null && player.hasEffect(QuietusEffects.SPELUNKING_EFFECT)) {
            Ore_Vision.IfPlayerMoved(player);
        }


    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        //if (event.getEntity().level().isClientSide()) return;
        else if (entity instanceof LivingEntity living_entity) {
            event.getEntity().getData(QuietusAttachments.MANA_ATTACHMENT).tick(living_entity);

        }
        /* if (entity instanceof Arrow arrow && arrow.level() instanceof ServerLevel) {
            Vec3 pos = arrow.position();
                //System.out.println(pos.x+ " | "+ pos.y + " | "+pos.z);
                System.out.println(arrow.getDeltaMovement().y);
        } */
    }
/* Have to separate player tick and entity tick or else mana will reset everytime player dies or joins the world
   This is because on the first entity tick, the max mana attribute is not modified, which equals to 20 (the default max mana); when mana exceeds max mana, mana value will be set the same as max mana
   wrting something like "if (entity instanceof LocalPlayer) return;" inside onEntityTick will not work, but onPlayerTick somehow doesn't have this problem, so let's use it for now.
 */
    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event)
    {
        Player player=event.getEntity();
        //if (event.getEntity().level().isClientSide()) return;
        if(player instanceof ServerPlayer serverPlayer) {
            event.getEntity().getData(QuietusAttachments.MANA_ATTACHMENT).tick(serverPlayer);
            //ManaHudOverlay.SetTick(serverPlayer);
            GameRules gameRules= serverPlayer.serverLevel().getGameRules();

            //Placeholder method for enabling/disabling death screen in relation to the ghost mode, might be changed in the future
            LocalPlayer localPlayer= Minecraft.getInstance().player;
        if(gameRules.getBoolean(QuietusGameRules.GHOST_MODE_ENABLED) &&localPlayer!=null)
        {
            if (localPlayer.shouldShowDeathScreen()) localPlayer.setShowDeathScreen(false);
        }
        else if(!gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN) &&localPlayer!=null)
        {
            if (!localPlayer.shouldShowDeathScreen()) localPlayer.setShowDeathScreen(true);
        }


        }
    }

    /* @SubscribeEvent
    public static void onProjectileLand(ProjectileImpactEvent event) {
        Entity projectile = event.getProjectile();
        Vec3 pos = projectile.position();
        if (projectile instanceof AbstractArrow) {
            System.out.println("land: "+pos.x+ " | "+ pos.y + " | "+pos.z);
        }
    } */


}
