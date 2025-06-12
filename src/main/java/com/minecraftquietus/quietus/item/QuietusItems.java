package com.minecraftquietus.quietus.item;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.item.weapons.NonAmmoProjectileWeaponItem;
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
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static com.minecraftquietus.quietus.block.QuietusBlocks.EXAMPLE_BLOCK;

import com.minecraftquietus.quietus.item.WeatheringCopperItems.CopperWeatherState;
import com.minecraftquietus.quietus.item.WeatheringIronItems.IronWeatherState;
import com.minecraftquietus.quietus.item.component.UsesMana;
import com.minecraftquietus.quietus.item.equipment.AmethystArmorItem;
import com.minecraftquietus.quietus.item.equipment.QuietusArmorMaterials;

import java.util.function.Consumer;
import com.minecraftquietus.quietus.item.weapons.AmmoProjectileWeaponItem;


public class QuietusItems {
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

    //#region EQUIPMENTS
        /**
         * Note: (as of Neoforge 21.5.75)
         * handling ArmorMaterials as argument on properties of items in register seems to cause java.lang.ExceptionInInitializerError. 
         * Instead, modify the second argument (the item creation factory) for item (instead of Item::new) and put the property with desired ArmorMaterial as argument, and add otherwise properties in the third argument class Item.Properties.
         */
        // copper armor & variants<-((normal),exposed,weathered,oxidized)
        public static final DeferredItem<Item> COPPER_BOOTS = registerCopperArmor("copper_boots", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> COPPER_LEGGINGS = registerCopperArmor("copper_leggings", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> COPPER_CHESTPLATE = registerCopperArmor("copper_chestplate", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> COPPER_HELMET = registerCopperArmor("copper_helmet", CopperWeatherState.UNAFFECTED, QuietusArmorMaterials.COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> EXPOSED_COPPER_BOOTS = registerCopperArmor("exposed_copper_boots", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> EXPOSED_COPPER_LEGGINGS = registerCopperArmor("exposed_copper_leggings", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> EXPOSED_COPPER_CHESTPLATE = registerCopperArmor("exposed_copper_chestplate", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> EXPOSED_COPPER_HELMET = registerCopperArmor("exposed_copper_helmet", CopperWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> WEATHERED_COPPER_BOOTS = registerCopperArmor("weathered_copper_boots", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> WEATHERED_COPPER_LEGGINGS = registerCopperArmor("weathered_copper_leggings", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> WEATHERED_COPPER_CHESTPLATE = registerCopperArmor("weathered_copper_chestplate", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> WEATHERED_COPPER_HELMET = registerCopperArmor("weathered_copper_helmet", CopperWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_COPPER, ArmorType.HELMET);
        public static final DeferredItem<Item> OXIDIZED_COPPER_BOOTS = registerCopperArmor("oxidized_copper_boots", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.BOOTS);
        public static final DeferredItem<Item> OXIDIZED_COPPER_LEGGINGS = registerCopperArmor("oxidized_copper_leggings", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> OXIDIZED_COPPER_CHESTPLATE = registerCopperArmor("oxidized_copper_chestplate", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> OXIDIZED_COPPER_HELMET = registerCopperArmor("oxidized_copper_helmet", CopperWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_COPPER, ArmorType.HELMET);
        private static DeferredItem<Item> registerCopperArmor(String name, CopperWeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
            return REGISTRAR.registerItem(name, properties -> new WeatheringCopperArmorItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))));
        }
        // iron armor & variants<-(exposed,weathered,oxidized)
        public static final DeferredItem<Item> EXPOSED_IRON_BOOTS = registerIronArmor("exposed_iron_boots", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> EXPOSED_IRON_LEGGINGS = registerIronArmor("exposed_iron_leggings", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> EXPOSED_IRON_CHESTPLATE = registerIronArmor("exposed_iron_chestplate", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> EXPOSED_IRON_HELMET = registerIronArmor("exposed_iron_helmet", IronWeatherState.EXPOSED, QuietusArmorMaterials.EXPOSED_IRON, ArmorType.HELMET);
        public static final DeferredItem<Item> WEATHERED_IRON_BOOTS = registerIronArmor("weathered_iron_boots", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> WEATHERED_IRON_LEGGINGS = registerIronArmor("weathered_iron_leggings", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> WEATHERED_IRON_CHESTPLATE = registerIronArmor("weathered_iron_chestplate", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> WEATHERED_IRON_HELMET = registerIronArmor("weathered_iron_helmet", IronWeatherState.WEATHERED, QuietusArmorMaterials.WEATHERED_IRON, ArmorType.HELMET);
        public static final DeferredItem<Item> OXIDIZED_IRON_BOOTS = registerIronArmor("oxidized_iron_boots", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.BOOTS);
        public static final DeferredItem<Item> OXIDIZED_IRON_LEGGINGS = registerIronArmor("oxidized_iron_leggings", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.LEGGINGS);
        public static final DeferredItem<Item> OXIDIZED_IRON_CHESTPLATE = registerIronArmor("oxidized_iron_chestplate", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.CHESTPLATE);
        public static final DeferredItem<Item> OXIDIZED_IRON_HELMET = registerIronArmor("oxidized_iron_helmet", IronWeatherState.OXIDIZED, QuietusArmorMaterials.OXIDIZED_IRON, ArmorType.HELMET);
        private static DeferredItem<Item> registerIronArmor(String name, IronWeatherState weatherState, ArmorMaterial armorMaterial, ArmorType armorType) {
            return REGISTRAR.registerItem(name, properties -> new WeatheringIronArmorItem(weatherState, new Item.Properties().humanoidArmor(armorMaterial, armorType).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))));
        }
        // amethyst armor
        public static final DeferredItem<Item> AMETHYST_BOOTS = REGISTRAR.registerItem("amethyst_boots", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.BOOTS).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))), new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_LEGGINGS = REGISTRAR.registerItem("amethyst_leggings", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.LEGGINGS).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))), new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_CHESTPLATE = REGISTRAR.registerItem("amethyst_chestplate", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.CHESTPLATE).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))), new Item.Properties());
        public static final DeferredItem<Item> AMETHYST_HELMET = REGISTRAR.registerItem("amethyst_helmet", properties -> new AmethystArmorItem(new QuietusItemProperties().quietusHumanoidArmor(QuietusArmorMaterials.AMETHYST, ArmorType.HELMET).setId(ResourceKey.create(Registries.ITEM, properties.effectiveModel()))), new Item.Properties());
    
    //#endregion

    //#region WEAPONS
        public static final DeferredItem<Item> TRIPLEBOW = REGISTRAR.registerItem("triple_bow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty( 
                3,
                (xRot,index,random)-> xRot + (index-1)*5.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*15.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        public static final DeferredItem<Item> INFINIBOW = REGISTRAR.registerItem("infini_bow", AmmoProjectileWeaponItem::new, new QuietusItemProperties()
            .weaponProperty(
                50,
                (xRot,index,random)-> xRot + (index-1)*5.0f*(random.nextFloat()-0.5f), 
                (yRot,index,random)-> yRot + index*15.0f*(random.nextFloat()-0.5f),
                3.0f,
                1.5f,
                ProjectileWeaponItem.ARROW_ONLY,
                16
            )
            .addProjectileCritChance(AmmoProjectileWeaponItem.MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE,0.2)
            .durability(384)
            .enchantable(1)
        );
        /* public static final DeferredItem<MagicalWeapon<amethystProjectile>> AMETHYST_STAFF =
            REGISTRAR.register("amethyst_staff", () ->new MagicalWeapon<>(
                    new QuietusItemProperties().projectileProperties(0, 5.0f, 0.05d, 0.4f, 0.0f, 200, QuietusProjectiles.AMETHYST_PROJECTILE.get())
                        .manaUse(5, UsesMana.Operation.ADDITION, 0).useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("quietus:amethyst_staff"))).stacksTo(1).useCooldown(0.75f),
                    QuietusProjectiles.AMETHYST_PROJECTILE, // Direct RegistryObject reference
                    1.5f, 0.0f, 0.4f, 5f, 200,0.05, SoundEvents.AMETHYST_CLUSTER_HIT){
                @Override
                public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {


                    components.accept(CommonComponents.EMPTY);
                    for(int i=1; i<=6;i++)
                    {
                        components.accept(Component.translatable("tooltip.quietus.amethyst_staff."+i));
                    }
                    super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                }

            }); */
        public static final DeferredItem<NonAmmoProjectileWeaponItem> AMETHYST_STAFF =
            REGISTRAR.register("amethyst_staff", () -> new NonAmmoProjectileWeaponItem(
                    new QuietusItemProperties()
                        .addProjectile(0, 5.0f, 0.05d, (damage)->(float)(damage*1.5d), 0.4f, 0.0f, 200, QuietusProjectiles.AMETHYST_PROJECTILE.get())
                        .addSound(NonAmmoProjectileWeaponItem.MAPKEY_SOUND_PLAYER_SHOOT, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS)
                        .manaUse(5, UsesMana.Operation.ADD_VALUE, 0)
                        .weaponProperty(
                            1,
                            (xRot,index,random)-> xRot, 
                            (yRot,index,random)-> yRot,
                            1.4f,
                            0.5f,
                            (itemstack)-> true,
                            16
                        )
                        .useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("quietus:amethyst_staff"))).stacksTo(1).useCooldown(0.75f))
                        {
                            @Override
                            public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
                                components.accept(CommonComponents.EMPTY);
                                for(int i=1; i<=6;i++)
                                {
                                    components.accept(Component.translatable("tooltip.quietus.amethyst_staff."+i));
                                }
                                super.appendHoverText(pStack, pContext, tooltipDisplay, components, tooltipFlag);
                            }
                        }
                    );
        public static final DeferredItem<NonAmmoProjectileWeaponItem> WEIRD_AMETHYST_STAFF =
            REGISTRAR.register("weird_amethyst_staff", () -> new NonAmmoProjectileWeaponItem(
                    new QuietusItemProperties()
                        .addProjectile(0, 5.0f, 0.05d, (damage)->(float)(damage*1.5d), 0.4f, 0.1f, 200, QuietusProjectiles.AMETHYST_PROJECTILE.get())
                        .addSound(NonAmmoProjectileWeaponItem.MAPKEY_SOUND_PLAYER_SHOOT, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS)
                        .manaUse(5, UsesMana.Operation.ADD_VALUE, 0)
                        .weaponProperty(
                            1,
                            (xRot,index,random)-> xRot, 
                            (yRot,index,random)-> yRot,
                            1.4f,
                            0.5f,
                            (itemstack)-> true,
                            16
                        )
                        .useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("quietus:weird_amethyst_staff"))).stacksTo(1).useCooldown(0.75f))
                    );

    //#endregion


    public static void registerWeatheringMappings() {
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
    }

    public static void register(IEventBus eventBus)
    {
        REGISTRAR.register(eventBus);
    }
}
