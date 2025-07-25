package com.minecraftquietus.quietus.item;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.util.QuietusAttributes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.block.QuietusBlocks.EXAMPLE_BLOCK;

import com.minecraftquietus.quietus.item.component.UsesMana;
import com.minecraftquietus.quietus.item.equipment.AmethystArmorItem;
import com.minecraftquietus.quietus.item.equipment.QuietusArmorMaterials;
import com.minecraftquietus.quietus.item.tool.AmmoProjectileWeaponItem;
import com.minecraftquietus.quietus.item.tool.CopperAxeItem;
import com.minecraftquietus.quietus.item.tool.CopperHoeItem;
import com.minecraftquietus.quietus.item.tool.CopperShovelItem;
import com.minecraftquietus.quietus.item.tool.QuietusProjectileWeaponItem;
import com.minecraftquietus.quietus.item.tool.QuietusToolMaterial;

import java.util.function.Consumer;


public class QuietusItems {


    public static void init() {
        QuietusFoods.init();
        QuietusConsumables.init();
    }

    /**
     * Attribute Modifiers ResourceLocation
     */
    public static final ResourceLocation BASE_MAX_MANA_ID = ResourceLocation.fromNamespaceAndPath(MODID, "base_max_mana");
    public static final ResourceLocation BASE_MANA_REGEN_BONUS_ID = ResourceLocation.fromNamespaceAndPath(MODID, "base_mana_regen_bonus");


    // Create a Deferred Register to hold Items which will all be registered under the "quietus" namespace
    public static final DeferredRegister.Items REGISTRAR = DeferredRegister.createItems(MODID);
    
    //#region MISCELLANEOUS
    public static final DeferredItem<Item> HARDENED_FUR = REGISTRAR.registerItem("hardened_fur",Item::new,new Item.Properties().attributes(
        ItemAttributeModifiers.builder()
            .add(QuietusAttributes.MAX_MANA.getDelegate(), new AttributeModifier(BASE_MANA_REGEN_BONUS_ID, 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()
    ));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = REGISTRAR.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    public static final DeferredItem<Item> EXAMPLE_ITEM = REGISTRAR.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    //#endregion

    //#region FOODS
    public static final DeferredItem<Item> MOLD = REGISTRAR.registerItem("mold", Item::new, new Item.Properties().food(QuietusFoods.MOLD, QuietusConsumables.MOLD));
    public static final DeferredItem<Item> MOLD_BUCKET = REGISTRAR.registerItem("mold_bucket", Item::new, new Item.Properties().craftRemainder(Items.BUCKET).usingConvertsTo(Items.BUCKET).food(QuietusFoods.MOLD_BUCKET, QuietusConsumables.MOLD_BUCKET));
    public static final DeferredItem<Item> MOLD_BOWL = REGISTRAR.registerItem("mold_bowl", Item::new, new Item.Properties().craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL).food(QuietusFoods.MOLD_BOWL, QuietusConsumables.MOLD_BOWL));
    public static final DeferredItem<Item> YOGHURT_BUCKET = REGISTRAR.registerItem("yoghurt_bucket", properties -> new Item(new QuietusItemProperties().canDecay(192, new ItemStack(MOLD_BUCKET.get())).craftRemainder(Items.BUCKET).usingConvertsTo(Items.BUCKET).food(QuietusFoods.YOGHURT_BUCKET, QuietusConsumables.YOGHURT_BUCKET).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "yoghurt_bucket")))));
    public static final DeferredItem<Item> CHEESE_BUCKET = REGISTRAR.registerItem("cheese_bucket", properties -> new Item(new QuietusItemProperties().canDecay(192, new ItemStack(MOLD_BUCKET.get())).craftRemainder(Items.BUCKET).usingConvertsTo(Items.BUCKET).food(QuietusFoods.CHEESE_BUCKET, QuietusConsumables.CHEESE_BUCKET).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "cheese_bucket")))));
    //#endregion

    //#region EQUIPMENTS
        /**
         * Note: (as of Neoforge 21.5.75)
         * handling ArmorMaterials as argument within properties of items in register seems to cause java.lang.ExceptionInInitializerError. 
         * Instead, modify the second argument (the item creation factory) for item (instead of Item::new) and put the property with desired ArmorMaterial as argument, and add otherwise properties in the third argument class Item.Properties.
         */
        // copper armor & variants<-((normal),exposed,weathered,oxidized)
        public static final DeferredItem<Item> COPPER_BOOTS = registerCopperArmor("copper_boots", WeatheringCopperItems.WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> COPPER_LEGGINGS = registerCopperArmor("copper_leggings", WeatheringCopperItems.WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> COPPER_CHESTPLATE = registerCopperArmor("copper_chestplate", WeatheringCopperItems.WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> COPPER_HELMET = registerCopperArmor("copper_helmet", WeatheringCopperItems.WeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> EXPOSED_COPPER_BOOTS = registerCopperArmor("exposed_copper_boots", WeatheringCopperItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> EXPOSED_COPPER_LEGGINGS = registerCopperArmor("exposed_copper_leggings", WeatheringCopperItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> EXPOSED_COPPER_CHESTPLATE = registerCopperArmor("exposed_copper_chestplate", WeatheringCopperItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> EXPOSED_COPPER_HELMET = registerCopperArmor("exposed_copper_helmet", WeatheringCopperItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> WEATHERED_COPPER_BOOTS = registerCopperArmor("weathered_copper_boots", WeatheringCopperItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> WEATHERED_COPPER_LEGGINGS = registerCopperArmor("weathered_copper_leggings", WeatheringCopperItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> WEATHERED_COPPER_CHESTPLATE = registerCopperArmor("weathered_copper_chestplate", WeatheringCopperItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> WEATHERED_COPPER_HELMET = registerCopperArmor("weathered_copper_helmet", WeatheringCopperItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> OXIDIZED_COPPER_BOOTS = registerCopperArmor("oxidized_copper_boots", WeatheringCopperItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> OXIDIZED_COPPER_LEGGINGS = registerCopperArmor("oxidized_copper_leggings", WeatheringCopperItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> OXIDIZED_COPPER_CHESTPLATE = registerCopperArmor("oxidized_copper_chestplate", WeatheringCopperItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> OXIDIZED_COPPER_HELMET = registerCopperArmor("oxidized_copper_helmet", WeatheringCopperItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.HELMET);
        private static DeferredItem<Item> registerCopperArmor(String name, WeatheringCopperItems.WeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
            return REGISTRAR.registerItem(name, properties -> new WeatheringCopperItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).repairable(Items.COPPER_INGOT).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))));
        }
        // iron armor & variants<-(exposed,weathered,oxidized)
        public static final DeferredItem<Item> EXPOSED_IRON_BOOTS = registerIronArmor("exposed_iron_boots", WeatheringIronItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> EXPOSED_IRON_LEGGINGS = registerIronArmor("exposed_iron_leggings", WeatheringIronItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> EXPOSED_IRON_CHESTPLATE = registerIronArmor("exposed_iron_chestplate", WeatheringIronItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> EXPOSED_IRON_HELMET = registerIronArmor("exposed_iron_helmet", WeatheringIronItems.WeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.HELMET);
        public static final DeferredItem<Item> WEATHERED_IRON_BOOTS = registerIronArmor("weathered_iron_boots", WeatheringIronItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> WEATHERED_IRON_LEGGINGS = registerIronArmor("weathered_iron_leggings", WeatheringIronItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> WEATHERED_IRON_CHESTPLATE = registerIronArmor("weathered_iron_chestplate", WeatheringIronItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> WEATHERED_IRON_HELMET = registerIronArmor("weathered_iron_helmet", WeatheringIronItems.WeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.HELMET);
        public static final DeferredItem<Item> OXIDIZED_IRON_BOOTS = registerIronArmor("oxidized_iron_boots", WeatheringIronItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> OXIDIZED_IRON_LEGGINGS = registerIronArmor("oxidized_iron_leggings", WeatheringIronItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> OXIDIZED_IRON_CHESTPLATE = registerIronArmor("oxidized_iron_chestplate", WeatheringIronItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> OXIDIZED_IRON_HELMET = registerIronArmor("oxidized_iron_helmet", WeatheringIronItems.WeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.HELMET);
        private static DeferredItem<Item> registerIronArmor(String name, WeatheringIronItems.WeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
            return REGISTRAR.registerItem(name, properties -> new WeatheringIronItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).repairable(Items.IRON_INGOT).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))));
        }
        // amethyst armor
        public static final DeferredItem<Item> AMETHYST_BOOTS = REGISTRAR.registerItem("amethyst_boots", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.BOOTS).repairable(Items.AMETHYST_SHARD).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))
    {
            @Override
            public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
            components.accept(CommonComponents.EMPTY);
            for(int i=1; i<=4;i++)
            {
                components.accept(Component.translatable("tooltip.quietus.amethyst_armor."+i));
            }
        super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
    }
    }, new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_LEGGINGS = REGISTRAR.registerItem("amethyst_leggings", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.LEGGINGS).repairable(Items.AMETHYST_SHARD).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
                        components.accept(CommonComponents.EMPTY);
                        for(int i=1; i<=4;i++)
                        {
                            components.accept(Component.translatable("tooltip.quietus.amethyst_armor."+i));
                        }
                        super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                    }}, new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_CHESTPLATE = REGISTRAR.registerItem("amethyst_chestplate", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.CHESTPLATE).repairable(Items.AMETHYST_SHARD).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))){
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
                        components.accept(CommonComponents.EMPTY);
                        for(int i=1; i<=4;i++)
                        {
                            components.accept(Component.translatable("tooltip.quietus.amethyst_armor."+i));
                        }
                        super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                    }}, new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_HELMET = REGISTRAR.registerItem("amethyst_helmet", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.HELMET).repairable(Items.AMETHYST_SHARD).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel())))
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
                        components.accept(CommonComponents.EMPTY);
                        for(int i=1; i<=4;i++)
                        {
                            components.accept(Component.translatable("tooltip.quietus.amethyst_armor."+i));
                        }
                        super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                    }}, new Item.Properties());
    
    //#endregion

    //#region WEAPONS & TOOLS
        public static final DeferredItem<Item> TRIPLEBOW = REGISTRAR.registerItem("triplebow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty( 
                3,
                (xRot,index,random)-> xRot + (index-0.5f)*8.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*12.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                -1,
                20,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        public static final DeferredItem<Item> INFINIBOW = REGISTRAR.registerItem("infinibow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty(
                50,
                (xRot,index,random)-> xRot + (index-1)*5.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*15.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                -1,
                30,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        public static final DeferredItem<Item> PULLBOW = REGISTRAR.registerItem("pullbow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty( 
                3,
                (xRot,index,random)-> xRot + (index-1)*5.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*15.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                10,
                -1,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        public static final DeferredItem<Item> INSTABOW = REGISTRAR.registerItem("instabow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty( 
                3,
                (xRot,index,random)-> xRot + (index-1)*5.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*15.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                0,
                -1,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        
        public static final DeferredItem<QuietusProjectileWeaponItem> AMETHYST_STAFF =
            REGISTRAR.register("amethyst_staff", () -> new QuietusProjectileWeaponItem(
                    new QuietusItemProperties()
                        .addProjectile(0, 7.0f, 0.1d, (damage)->(float)(damage*1.5d), 0.4f, 0.0f, 200, QuietusProjectiles.AMETHYST_PROJECTILE.get())
                        .addSound(QuietusProjectileWeaponItem.MAPKEY_SOUND_PLAYER_SHOOT, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS)
                        .manaUse(5, UsesMana.Operation.ADD_VALUE, 0)
                        .weaponProperty(
                            1,
                            (xRot,index,random)-> xRot, 
                            (yRot,index,random)-> yRot,
                            1.4f,
                            0.5f,
                            (itemstack)-> true,
                            0,
                            -1,
                            16
                        )
                        .durability(384)
                            .enchantable(2).repairable(Items.AMETHYST_SHARD)
                        .useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("quietus:amethyst_staff"))).stacksTo(1).useCooldown(0.75f))
                        {
                            @Override
                            public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
                                components.accept(CommonComponents.EMPTY);
                                for(int i=1; i<=6;i++)
                                {
                                    components.accept(Component.translatable("tooltip.quietus.amethyst_staff."+i));
                                }

                                //may need a tooltip revamp that's more flexible in the future
                               // int mana_consume = pStack.get(QuietusComponents.USES_MANA.get()).calculateConsumption(Mana.getMana((LivingEntity)this.), Mana.getMaxMana((LivingEntity)pStack.getEntityRepresentation()),pStack,pStack.getEntityRepresentation().level());
                                /*components.accept(Component.literal(Component.translatable("tooltip.quietus.amethyst_staff."+5)+String.valueOf(mana_consume)));
                                components.accept(Component.translatable("tooltip.quietus.amethyst_staff."+6));*/
                                super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                            }
                        }
                    );
        public static final DeferredItem<QuietusProjectileWeaponItem> WEIRD_AMETHYST_STAFF =
            REGISTRAR.register("weird_amethyst_staff", () -> new QuietusProjectileWeaponItem(
                    new QuietusItemProperties()
                        .addProjectile(0, 5.0f, 0.05d, (damage)->(float)(damage*1.5d), 0.4f, 0.1f, 200, QuietusProjectiles.AMETHYST_PROJECTILE.get())
                        .addSound(QuietusProjectileWeaponItem.MAPKEY_SOUND_PLAYER_SHOOT, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS)
                        .manaUse(5, UsesMana.Operation.ADD_VALUE, 0)
                        .weaponProperty(
                            1,
                            (xRot,index,random)-> xRot, 
                            (yRot,index,random)-> yRot,
                            1.4f,
                            0.5f,
                            (itemstack)-> true,
                            10,
                            -1,
                            16
                        )
                        .durability(384)
                        .useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("quietus:weird_amethyst_staff"))).stacksTo(1).useCooldown(0.75f))
                    );

        public static final DeferredItem<Item> COPPER_SWORD = REGISTRAR.registerItem("copper_sword", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.UNAFFECTED, new QuietusItemProperties().sword(QuietusToolMaterial.COPPER, 3.0f, -2.4f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "copper_sword")))));    
        public static final DeferredItem<Item> COPPER_SHOVEL = REGISTRAR.registerItem("copper_shovel", properties -> new CopperShovelItem(WeatheringCopperItems.WeatherState.UNAFFECTED, new QuietusItemProperties().shovel(QuietusToolMaterial.COPPER, 1.5f, -3.0f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "copper_shovel")))));
        public static final DeferredItem<Item> COPPER_PICKAXE = REGISTRAR.registerItem("copper_pickaxe", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.UNAFFECTED, new QuietusItemProperties().pickaxe(QuietusToolMaterial.COPPER, 1.5f, -2.8f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "copper_pickaxe")))));    
        public static final DeferredItem<Item> COPPER_AXE = REGISTRAR.registerItem("copper_axe", properties -> new CopperAxeItem(WeatheringCopperItems.WeatherState.UNAFFECTED, new QuietusItemProperties().axe(QuietusToolMaterial.COPPER, 6.5f, -3.2f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "copper_axe")))));
        public static final DeferredItem<Item> COPPER_HOE = REGISTRAR.registerItem("copper_hoe", properties -> new CopperHoeItem(WeatheringCopperItems.WeatherState.UNAFFECTED, new QuietusItemProperties().hoe(QuietusToolMaterial.COPPER, -1.5f, -1.5f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "copper_hoe")))));
        public static final DeferredItem<Item> EXPOSED_COPPER_SWORD = REGISTRAR.registerItem("exposed_copper_sword", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.EXPOSED, new QuietusItemProperties().sword(QuietusToolMaterial.EXPOSED_COPPER, 3.0f, -2.4f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "exposed_copper_sword")))));    
        public static final DeferredItem<Item> EXPOSED_COPPER_SHOVEL = REGISTRAR.registerItem("exposed_copper_shovel", properties -> new CopperShovelItem(WeatheringCopperItems.WeatherState.EXPOSED, new QuietusItemProperties().shovel(QuietusToolMaterial.EXPOSED_COPPER, 1.5f, -3.0f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "exposed_copper_shovel")))));
        public static final DeferredItem<Item> EXPOSED_COPPER_PICKAXE = REGISTRAR.registerItem("exposed_copper_pickaxe", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.EXPOSED, new QuietusItemProperties().pickaxe(QuietusToolMaterial.EXPOSED_COPPER, 2.0f, -2.9f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "exposed_copper_pickaxe")))));    
        public static final DeferredItem<Item> EXPOSED_COPPER_AXE = REGISTRAR.registerItem("exposed_copper_axe", properties -> new CopperAxeItem(WeatheringCopperItems.WeatherState.EXPOSED, new QuietusItemProperties().axe(QuietusToolMaterial.EXPOSED_COPPER, 6.5f, -3.2f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "exposed_copper_axe")))));
        public static final DeferredItem<Item> EXPOSED_COPPER_HOE = REGISTRAR.registerItem("exposed_copper_hoe", properties -> new CopperHoeItem(WeatheringCopperItems.WeatherState.EXPOSED, new QuietusItemProperties().hoe(QuietusToolMaterial.EXPOSED_COPPER, -1.0f, -1.5f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "exposed_copper_hoe")))));
        public static final DeferredItem<Item> WEATHERED_COPPER_SWORD = REGISTRAR.registerItem("weathered_copper_sword", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.WEATHERED, new QuietusItemProperties().sword(QuietusToolMaterial.WEATHERED_COPPER, 3.0f, -2.4f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "weathered_copper_sword")))));    
        public static final DeferredItem<Item> WEATHERED_COPPER_SHOVEL = REGISTRAR.registerItem("weathered_copper_shovel", properties -> new CopperShovelItem(WeatheringCopperItems.WeatherState.WEATHERED, new QuietusItemProperties().shovel(QuietusToolMaterial.WEATHERED_COPPER, 1.5f, -3.0f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "weathered_copper_shovel")))));
        public static final DeferredItem<Item> WEATHERED_COPPER_PICKAXE = REGISTRAR.registerItem("weathered_copper_pickaxe", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.WEATHERED, new QuietusItemProperties().pickaxe(QuietusToolMaterial.WEATHERED_COPPER, 2.5f, -3.0f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "weathered_copper_pickaxe")))));    
        public static final DeferredItem<Item> WEATHERED_COPPER_AXE = REGISTRAR.registerItem("weathered_copper_axe", properties -> new CopperAxeItem(WeatheringCopperItems.WeatherState.WEATHERED, new QuietusItemProperties().axe(QuietusToolMaterial.WEATHERED_COPPER, 6.5f, -3.2f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "weathered_copper_axe")))));
        public static final DeferredItem<Item> WEATHERED_COPPER_HOE = REGISTRAR.registerItem("weathered_copper_hoe", properties -> new CopperHoeItem(WeatheringCopperItems.WeatherState.WEATHERED, new QuietusItemProperties().hoe(QuietusToolMaterial.WEATHERED_COPPER, -0.5f, -1.5f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "weathered_copper_hoe")))));
        public static final DeferredItem<Item> OXIDIZED_COPPER_SWORD = REGISTRAR.registerItem("oxidized_copper_sword", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.OXIDIZED, new QuietusItemProperties().sword(QuietusToolMaterial.OXIDIZED_COPPER, 3.0f, -2.4f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "oxidized_copper_sword")))));    
        public static final DeferredItem<Item> OXIDIZED_COPPER_SHOVEL = REGISTRAR.registerItem("oxidized_copper_shovel", properties -> new CopperShovelItem(WeatheringCopperItems.WeatherState.OXIDIZED, new QuietusItemProperties().shovel(QuietusToolMaterial.OXIDIZED_COPPER, 1.5f, -3.0f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "oxidized_copper_shovel")))));
        public static final DeferredItem<Item> OXIDIZED_COPPER_PICKAXE = REGISTRAR.registerItem("oxidized_copper_pickaxe", properties -> new WeatheringCopperItem(WeatheringCopperItems.WeatherState.OXIDIZED, new QuietusItemProperties().pickaxe(QuietusToolMaterial.OXIDIZED_COPPER, 3.0f, -3.1f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "oxidized_copper_pickaxe")))));    
        public static final DeferredItem<Item> OXIDIZED_COPPER_AXE = REGISTRAR.registerItem("oxidized_copper_axe", properties -> new CopperAxeItem(WeatheringCopperItems.WeatherState.OXIDIZED, new QuietusItemProperties().axe(QuietusToolMaterial.OXIDIZED_COPPER, 6.5f, -3.2f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "oxidized_copper_axe")))));
        public static final DeferredItem<Item> OXIDIZED_COPPER_HOE = REGISTRAR.registerItem("oxidized_copper_hoe", properties -> new CopperHoeItem(WeatheringCopperItems.WeatherState.OXIDIZED, new QuietusItemProperties().hoe(QuietusToolMaterial.OXIDIZED_COPPER, 0.0f, -1.5f).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "oxidized_copper_hoe")))));
        
        
    //#endregion

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event, ResourceKey<CreativeModeTab> tabKey) {
        if (tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
        if (tabKey == CreativeModeTabs.INGREDIENTS){
            event.accept(HARDENED_FUR);
        }
        if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(COPPER_SHOVEL);
            event.accept(COPPER_PICKAXE);
            event.accept(COPPER_AXE);
            event.accept(COPPER_HOE);
        }
        if (tabKey == CreativeModeTabs.COMBAT) {
            event.accept(COPPER_SWORD);
            event.accept(COPPER_AXE);
            event.accept(AMETHYST_STAFF);
            event.accept(COPPER_HELMET);
            event.accept(COPPER_CHESTPLATE);
            event.accept(COPPER_LEGGINGS);
            event.accept(COPPER_BOOTS);
            event.accept(AMETHYST_HELMET);
            event.accept(AMETHYST_CHESTPLATE);
            event.accept(AMETHYST_LEGGINGS);
            event.accept(AMETHYST_BOOTS);
        }
    }

    public static void registerWeatheringMappings() {
        // Register copper items in the OXIDATION_MAP of WeatheringCopperItems
        // copper armor
        WeatheringCopperItems.registerWeathering(COPPER_BOOTS.get(), EXPOSED_COPPER_BOOTS.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_BOOTS.get(), WEATHERED_COPPER_BOOTS.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_BOOTS.get(), OXIDIZED_COPPER_BOOTS.get());
        WeatheringCopperItems.registerWeathering(COPPER_LEGGINGS.get(), EXPOSED_COPPER_LEGGINGS.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_LEGGINGS.get(), WEATHERED_COPPER_LEGGINGS.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_LEGGINGS.get(), OXIDIZED_COPPER_LEGGINGS.get());
        WeatheringCopperItems.registerWeathering(COPPER_CHESTPLATE.get(), EXPOSED_COPPER_CHESTPLATE.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_CHESTPLATE.get(), WEATHERED_COPPER_CHESTPLATE.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_CHESTPLATE.get(), OXIDIZED_COPPER_CHESTPLATE.get());
        WeatheringCopperItems.registerWeathering(COPPER_HELMET.get(), EXPOSED_COPPER_HELMET.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_HELMET.get(), WEATHERED_COPPER_HELMET.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_HELMET.get(), OXIDIZED_COPPER_HELMET.get());
        // copper tools & weapons
        WeatheringCopperItems.registerWeathering(COPPER_SWORD.get(), EXPOSED_COPPER_SWORD.get());
        WeatheringCopperItems.registerWeathering(COPPER_SHOVEL.get(), EXPOSED_COPPER_SHOVEL.get());
        WeatheringCopperItems.registerWeathering(COPPER_PICKAXE.get(), EXPOSED_COPPER_PICKAXE.get());
        WeatheringCopperItems.registerWeathering(COPPER_AXE.get(), EXPOSED_COPPER_AXE.get());
        WeatheringCopperItems.registerWeathering(COPPER_HOE.get(), EXPOSED_COPPER_HOE.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_SWORD.get(), WEATHERED_COPPER_SWORD.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_SHOVEL.get(), WEATHERED_COPPER_SHOVEL.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_PICKAXE.get(), WEATHERED_COPPER_PICKAXE.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_AXE.get(), WEATHERED_COPPER_AXE.get());
        WeatheringCopperItems.registerWeathering(EXPOSED_COPPER_HOE.get(), WEATHERED_COPPER_HOE.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_SWORD.get(), OXIDIZED_COPPER_SWORD.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_SHOVEL.get(), OXIDIZED_COPPER_SHOVEL.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_PICKAXE.get(), OXIDIZED_COPPER_PICKAXE.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_AXE.get(), OXIDIZED_COPPER_AXE.get());
        WeatheringCopperItems.registerWeathering(WEATHERED_COPPER_HOE.get(), OXIDIZED_COPPER_HOE.get());
        // Register iron items in the OXIDATION_MAP of WeatheringIronItems
        // iron armor
        WeatheringIronItems.registerWeathering(Items.IRON_BOOTS, EXPOSED_IRON_BOOTS.get());
        WeatheringIronItems.registerWeathering(EXPOSED_IRON_BOOTS.get(), WEATHERED_IRON_BOOTS.get());
        WeatheringIronItems.registerWeathering(WEATHERED_IRON_BOOTS.get(), OXIDIZED_IRON_BOOTS.get());
        WeatheringIronItems.registerWeathering(Items.IRON_LEGGINGS, EXPOSED_IRON_LEGGINGS.get());
        WeatheringIronItems.registerWeathering(EXPOSED_IRON_LEGGINGS.get(), WEATHERED_IRON_LEGGINGS.get());
        WeatheringIronItems.registerWeathering(WEATHERED_IRON_LEGGINGS.get(), OXIDIZED_IRON_LEGGINGS.get());
        WeatheringIronItems.registerWeathering(Items.IRON_CHESTPLATE, EXPOSED_IRON_CHESTPLATE.get());
        WeatheringIronItems.registerWeathering(EXPOSED_IRON_CHESTPLATE.get(), WEATHERED_IRON_CHESTPLATE.get());
        WeatheringIronItems.registerWeathering(WEATHERED_IRON_CHESTPLATE.get(), OXIDIZED_IRON_CHESTPLATE.get());
        WeatheringIronItems.registerWeathering(Items.IRON_HELMET, EXPOSED_IRON_HELMET.get());
        WeatheringIronItems.registerWeathering(EXPOSED_IRON_HELMET.get(), WEATHERED_IRON_HELMET.get());
        WeatheringIronItems.registerWeathering(WEATHERED_IRON_HELMET.get(), OXIDIZED_IRON_HELMET.get());
        // Register vanilla iron items into ExtraWeatheringItem, so they will weather and is used in checking weathering process by other WeatheringItem: 
        /* Arguments: 
         *  the Item object, int array of possible WeatherStates(picked randomly from the array), float of oxidation chance, the Class<?> enumeration of WeatherState class this item should have (just take from existing classes as shown below)
        */
        Class<?> class_weathering_iron_armor_item = WeatheringIronItems.WeatherState.class;
        WeatheringItem.registerExtraWeatheringItem(Items.IRON_BOOTS, new int[]{0}, WeatheringIronItem.OXIDATION_CHANCE * 0.625f, WeatheringIronItem.OXIDATION_CHANCE_WARM * 0.625f, class_weathering_iron_armor_item);
        WeatheringItem.registerExtraWeatheringItem(Items.IRON_LEGGINGS, new int[]{0}, WeatheringIronItem.OXIDATION_CHANCE * 0.625f, WeatheringIronItem.OXIDATION_CHANCE_WARM * 0.625f, class_weathering_iron_armor_item);
        WeatheringItem.registerExtraWeatheringItem(Items.IRON_CHESTPLATE, new int[]{0}, WeatheringIronItem.OXIDATION_CHANCE * 0.625f, WeatheringIronItem.OXIDATION_CHANCE_WARM * 0.625f, class_weathering_iron_armor_item);
        WeatheringItem.registerExtraWeatheringItem(Items.IRON_HELMET, new int[]{0}, WeatheringIronItem.OXIDATION_CHANCE * 0.625f, WeatheringIronItem.OXIDATION_CHANCE_WARM * 0.625f, class_weathering_iron_armor_item);
    }

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
