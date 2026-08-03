package com.quietus.event_listener;

import com.quietus.item.QuietusComponents;
import com.quietus.item.WeatheringCopperItems;
import com.quietus.item.WeatheringIronItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;

import java.util.Optional;

import static com.quietus.Quietus.MODID;

/** Handles deliberate removal and prevention of rust on equipment. */
@EventBusSubscriber(modid = MODID)
public final class RustTreatmentHandler {
    public static final int NUGGETS_PER_TREATMENT = 5;
    public static final int NUGGET_STAGES_REMOVED = 1;
    public static final int INGOT_STAGES_REMOVED = 2;
    public static final float GRINDSTONE_DAMAGE_PER_STAGE = 0.10F;

    private static final float NUGGET_REPAIR_FRACTION = 1.0F / 9.0F;
    private static final float INGOT_REPAIR_FRACTION = 0.25F;

    private RustTreatmentHandler() {
    }

    @SubscribeEvent
    public static void onGrindstonePlaceItem(GrindstoneEvent.OnPlaceItem event) {
        ItemStack input = getSingleInput(event.getTopItem(), event.getBottomItem());
        if (input.isEmpty()) {
            return;
        }

        RustInfo rust = RustInfo.of(input.getItem());
        boolean waxed = input.has(QuietusComponents.WAXED.get());
        if (rust == null || (rust.stage() == 0 && !waxed)) {
            return;
        }

        ItemStack output = input.transmuteCopy(rust.pristineItem());
        output.setCount(1);
        output.remove(QuietusComponents.WAXED.get());
        removeNonCurseEnchantments(output);

        if (output.isDamageableItem() && rust.stage() > 0) {
            int damageAdded = Math.max(1, Math.round(output.getMaxDamage()
                    * GRINDSTONE_DAMAGE_PER_STAGE * rust.stage()));
            output.setDamageValue(Math.min(output.getMaxDamage() - 1,
                    output.getDamageValue() + damageAdded));
        }

        event.setOutput(output);
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        RustInfo rust = RustInfo.of(left.getItem());
        if (rust == null || right.isEmpty()) {
            return;
        }

        if (right.is(Items.HONEYCOMB)) {
            if (left.has(QuietusComponents.WAXED.get())) {
                return;
            }

            ItemStack output = left.copyWithCount(1);
            output.set(QuietusComponents.WAXED.get(), Unit.INSTANCE);
            applyName(output, event.getName());
            finishAnvilOperation(event, output, 1);
            return;
        }

        TreatmentMaterial material = TreatmentMaterial.forItems(rust.family(), right.getItem());
        if (material == null || rust.stage() == 0 || right.getCount() < material.materialCost()) {
            return;
        }

        Item target = rust.itemAfterRemovingStages(material.stagesRemoved());
        ItemStack output = left.transmuteCopy(target);
        output.setCount(1);
        repair(output, material.repairFraction());
        applyName(output, event.getName());
        finishAnvilOperation(event, output, material.materialCost());
    }

    private static ItemStack getSingleInput(ItemStack top, ItemStack bottom) {
        if (top.isEmpty() == bottom.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return top.isEmpty() ? bottom : top;
    }

    private static void removeNonCurseEnchantments(ItemStack stack) {
        ItemEnchantments remaining = EnchantmentHelper.updateEnchantments(stack,
                enchantments -> enchantments.removeIf(enchantment -> !enchantment.is(EnchantmentTags.CURSE)));
        int repairCost = 0;
        for (int i = 0; i < remaining.size(); i++) {
            repairCost = AnvilMenu.calculateIncreasedRepairCost(repairCost);
        }
        stack.set(DataComponents.REPAIR_COST, repairCost);
    }

    private static void repair(ItemStack stack, float fraction) {
        if (!stack.isDamageableItem() || !stack.isDamaged()) {
            return;
        }
        int repaired = Math.max(1, Math.round(stack.getMaxDamage() * fraction));
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - repaired));
    }

    private static void applyName(ItemStack stack, String name) {
        if (name == null) {
            return;
        }
        if (name.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_NAME);
        } else {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
    }

    private static void finishAnvilOperation(AnvilUpdateEvent event, ItemStack output, int materialCost) {
        int priorWorkCost = output.getOrDefault(DataComponents.REPAIR_COST, 0);
        output.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(priorWorkCost));
        event.setOutput(output);
        event.setMaterialCost(materialCost);
        event.setXpCost(Math.max(1, priorWorkCost + 1));
    }

    private enum RustFamily {
        COPPER,
        IRON
    }

    private record TreatmentMaterial(int stagesRemoved, float repairFraction, int materialCost) {
        private static final TreatmentMaterial NUGGET = new TreatmentMaterial(
                NUGGET_STAGES_REMOVED, NUGGET_REPAIR_FRACTION, NUGGETS_PER_TREATMENT);
        private static final TreatmentMaterial INGOT = new TreatmentMaterial(
                INGOT_STAGES_REMOVED, INGOT_REPAIR_FRACTION, 1);

        private static TreatmentMaterial forItems(RustFamily family, Item material) {
            if (family == RustFamily.COPPER) {
                if (material == Items.COPPER_NUGGET) return NUGGET;
                if (material == Items.COPPER_INGOT) return INGOT;
            } else {
                if (material == Items.IRON_NUGGET) return NUGGET;
                if (material == Items.IRON_INGOT) return INGOT;
            }
            return null;
        }
    }

    private record RustInfo(RustFamily family, Item currentItem, Item pristineItem, int stage) {
        private static RustInfo of(Item item) {
            if (WeatheringCopperItems.OXIDATION_MAP.containsKey(item)
                    || WeatheringCopperItems.OXIDATION_MAP.containsValue(item)) {
                return fromCopper(item);
            }
            if (WeatheringIronItems.OXIDATION_MAP.containsKey(item)
                    || WeatheringIronItems.OXIDATION_MAP.containsValue(item)) {
                return fromIron(item);
            }
            return null;
        }

        private static RustInfo fromCopper(Item item) {
            Item pristine = item;
            int stage = 0;
            Optional<Item> previous;
            while ((previous = WeatheringCopperItems.getPrevious(pristine)).isPresent()) {
                pristine = previous.get();
                stage++;
            }
            return new RustInfo(RustFamily.COPPER, item, pristine, stage);
        }

        private static RustInfo fromIron(Item item) {
            Item pristine = item;
            int stage = 0;
            Optional<Item> previous;
            while ((previous = WeatheringIronItems.getPrevious(pristine)).isPresent()) {
                pristine = previous.get();
                stage++;
            }
            return new RustInfo(RustFamily.IRON, item, pristine, stage);
        }

        private Item itemAfterRemovingStages(int stages) {
            Item result = currentItem;
            for (int i = 0; i < stages; i++) {
                Optional<Item> previous = family == RustFamily.COPPER
                        ? WeatheringCopperItems.getPrevious(result)
                        : WeatheringIronItems.getPrevious(result);
                if (previous.isEmpty()) {
                    break;
                }
                result = previous.get();
            }
            return result;
        }
    }
}
