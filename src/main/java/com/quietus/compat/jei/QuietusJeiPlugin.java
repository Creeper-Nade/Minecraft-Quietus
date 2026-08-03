package com.quietus.compat.jei;

import com.quietus.item.QuietusComponents;
import com.quietus.item.WeatheringCopperItems;
import com.quietus.item.WeatheringIronItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.quietus.Quietus.MODID;
import static com.quietus.event_listener.RustTreatmentHandler.GRINDSTONE_DAMAGE_PER_STAGE;
import static com.quietus.event_listener.RustTreatmentHandler.INGOT_STAGES_REMOVED;
import static com.quietus.event_listener.RustTreatmentHandler.NUGGETS_PER_TREATMENT;
import static com.quietus.event_listener.RustTreatmentHandler.NUGGET_STAGES_REMOVED;

/** Optional JEI recipe and information integration. Loaded only when JEI is installed. */
@JeiPlugin
public final class QuietusJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(MODID, "rust_treatment");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
        List<IJeiAnvilRecipe> anvilRecipes = new ArrayList<>();

        addFamilyRecipes(factory, grindstoneRecipes, anvilRecipes, RustFamily.COPPER,
                Items.COPPER_NUGGET, Items.COPPER_INGOT);
        addFamilyRecipes(factory, grindstoneRecipes, anvilRecipes, RustFamily.IRON,
                Items.IRON_NUGGET, Items.IRON_INGOT);

        registration.addRecipes(RecipeTypes.GRINDSTONE, grindstoneRecipes);
        registration.addRecipes(RecipeTypes.ANVIL, anvilRecipes);
    }

    private static void addFamilyRecipes(
            IVanillaRecipeFactory factory,
            List<IJeiGrindstoneRecipe> grindstoneRecipes,
            List<IJeiAnvilRecipe> anvilRecipes,
            RustFamily family,
            Item nugget,
            Item ingot
    ) {
        for (Item rustedItem : family.rustedItems()) {
            int stage = family.stageOf(rustedItem);
            Item pristineItem = family.itemAfterRemovingStages(rustedItem, stage);
            ItemStack grindstoneOutput = new ItemStack(pristineItem);
            if (grindstoneOutput.isDamageableItem()) {
                int damage = Math.max(1, Math.round(grindstoneOutput.getMaxDamage()
                        * GRINDSTONE_DAMAGE_PER_STAGE * stage));
                grindstoneOutput.setDamageValue(Math.min(grindstoneOutput.getMaxDamage() - 1, damage));
            }
            grindstoneRecipes.add(factory.createGrindstoneRecipe(
                    List.of(new ItemStack(rustedItem)),
                    List.of(),
                    List.of(grindstoneOutput),
                    0,
                    0,
                    recipeUid("grindstone", rustedItem)
            ));

            Item oneStageCleaner = family.itemAfterRemovingStages(rustedItem, NUGGET_STAGES_REMOVED);
            anvilRecipes.add(factory.createAnvilRecipe(
                    new ItemStack(rustedItem),
                    List.of(new ItemStack(nugget, NUGGETS_PER_TREATMENT)),
                    List.of(new ItemStack(oneStageCleaner)),
                    recipeUid("anvil_nugget", rustedItem)
            ));

            Item twoStagesCleaner = family.itemAfterRemovingStages(rustedItem, INGOT_STAGES_REMOVED);
            anvilRecipes.add(factory.createAnvilRecipe(
                    new ItemStack(rustedItem),
                    List.of(new ItemStack(ingot)),
                    List.of(new ItemStack(twoStagesCleaner)),
                    recipeUid("anvil_ingot", rustedItem)
            ));
        }

        for (Item rustableItem : family.allRustableItems()) {
            ItemStack waxedOutput = new ItemStack(rustableItem);
            waxedOutput.set(QuietusComponents.WAXED.get(), Unit.INSTANCE);
            anvilRecipes.add(factory.createAnvilRecipe(
                    new ItemStack(rustableItem),
                    List.of(new ItemStack(Items.HONEYCOMB)),
                    List.of(waxedOutput),
                    recipeUid("waxing", rustableItem)
            ));
        }
    }

    private static Identifier recipeUid(String operation, Item input) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(input);
        return Identifier.fromNamespaceAndPath(MODID,
                "rust_treatment/" + operation + "/" + itemId.getNamespace() + "/" + itemId.getPath());
    }

    private enum RustFamily {
        COPPER {
            @Override
            Optional<Item> previous(Item item) {
                return WeatheringCopperItems.getPrevious(item);
            }

            @Override
            Set<Item> allRustableItems() {
                Set<Item> items = new LinkedHashSet<>(WeatheringCopperItems.OXIDATION_MAP.keySet());
                items.addAll(WeatheringCopperItems.OXIDATION_MAP.values());
                return items;
            }
        },
        IRON {
            @Override
            Optional<Item> previous(Item item) {
                return WeatheringIronItems.getPrevious(item);
            }

            @Override
            Set<Item> allRustableItems() {
                Set<Item> items = new LinkedHashSet<>(WeatheringIronItems.OXIDATION_MAP.keySet());
                items.addAll(WeatheringIronItems.OXIDATION_MAP.values());
                return items;
            }
        };

        abstract Optional<Item> previous(Item item);

        abstract Set<Item> allRustableItems();

        Set<Item> rustedItems() {
            Set<Item> items = allRustableItems();
            items.removeIf(item -> previous(item).isEmpty());
            return items;
        }

        int stageOf(Item item) {
            int stage = 0;
            Item current = item;
            Optional<Item> previous;
            while ((previous = previous(current)).isPresent()) {
                current = previous.get();
                stage++;
            }
            return stage;
        }

        Item itemAfterRemovingStages(Item item, int stages) {
            Item result = item;
            for (int i = 0; i < stages; i++) {
                Optional<Item> previous = previous(result);
                if (previous.isEmpty()) {
                    break;
                }
                result = previous.get();
            }
            return result;
        }
    }
}
