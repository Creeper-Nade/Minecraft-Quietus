package com.minecraftquietus.quietus.server;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import net.minecraft.FileUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;


/**
 * The ServerSkillTreeManager is responsible for reading data, which is called upon by com.minecraftquietus.quietus.event_listener.ResourceEvent
 */
public class ServerSkillTreeManager extends ContextAwareReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String DIR_NAME = String.join("/", MODID, "skill_tree"); // TODO: watch out for the separator here, likely different for different OS. Json reader seems to be able to convert it, but still find an alternative

    public static final String FILENAME_TAB_DATA = "_tab_.json";
    
    private final DynamicOps<JsonElement> ops;
    private final Codec<? extends SkillPoint> skillPointCodec;
    private final Codec<? extends SkillCategory> skillCategoryCodec;
    private final FileToIdConverter lister = FileToIdConverter.json(DIR_NAME);

    private ImmutableMap<ResourceLocation, SkillCategory> categories = ImmutableMap.of();

    public ServerSkillTreeManager(HolderLookup.Provider registries, Codec<? extends SkillCategory> codec1, Codec<? extends SkillPoint> codec2) {
        this.ops = registries.createSerializationContext(JsonOps.INSTANCE);
        this.skillCategoryCodec = codec1;
        this.skillPointCodec = codec2;
    }

    public Map<ResourceLocation, SkillCategory> get() {
        return this.categories;
    }

    /** 
     * What to do when datapack is reloaded
     * Has used CompletableFuture for asynchronous functions
     */
    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor
    ) {
        CompletableFuture<Map<ResourceLocation, SkillPoint>> skillPointLoad = CompletableFuture.supplyAsync(() -> this.prepareSkillPoints(manager, Profiler.get()), backgroundExecutor);
        return CompletableFuture.supplyAsync(() -> this.prepareSkillCategories(manager, Profiler.get()), backgroundExecutor)
            .thenCompose(barrier::wait)
            .thenAcceptBothAsync(skillPointLoad, (skillCategory,skillPoint) -> this.apply(skillCategory, skillPoint, manager, Profiler.get()), gameExecutor);
    }

    /**
     * Takes in the decoded objects and further process them
     * In this case it adds inheritance relations to each node, 
     * and is lastly sorted into their appropriate categories, 
     * fully prepared for use as Map<ResourceLocation,SkillCategory>. 
     * @param obj1
     * @param obj2
     * @param resourceManager
     * @param profiler
     */
    protected void apply(Map<ResourceLocation, SkillCategory> obj1, Map<ResourceLocation, SkillPoint> obj2, ResourceManager resourceManager,
            ProfilerFiller profiler) {
        /* obj1.forEach((location, skillCategory) -> {
            LOGGER.info(location.toString() + " -> " + skillCategory.toString());
        });
        obj2.forEach((location, skillPoint) -> {
            LOGGER.info(location.toString() + " -> " + skillPoint.toString());
        }); */
        ImmutableMap.Builder<ResourceLocation,SkillCategory> immutableMap$builder = ImmutableMap.builder();
        obj1.forEach((location, skillCategory) -> {
            SkillCategory category = new SkillCategory(location, skillCategory.getPrerequisites(), skillCategory.getDisplay());
            Map<ResourceLocation, SkillPoint> filtered_map = obj2.entrySet().stream()
                .filter( // filter all the skill nodes that should be in this category, which should contain the location of this category in their paths (and obviously also same namespace)
                    (entry) -> entry.getKey().getPath().startsWith(location.getPath()) && entry.getKey().getNamespace().equals(location.getNamespace())
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); 
            category.addAll(filtered_map); // note: category::addAll should construct the SkillTreeNodes (and übrigens save their pointers for itself), so they do not have to be constructed here
            immutableMap$builder.put(location,category);
        });
        this.categories = immutableMap$builder.build();
    }

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    protected Map<ResourceLocation, SkillPoint> prepareSkillPoints(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SkillPoint> map = new HashMap<>();
        scanDirectory(resourceManager, this.lister, this.makeConditionalOps(this.ops), this.skillPointCodec, map, false);
        return map;
    }
    protected Map<ResourceLocation, SkillCategory> prepareSkillCategories(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SkillCategory> map = new HashMap<>();
        scanDirectory(resourceManager, this.lister, this.makeConditionalOps(this.ops), this.skillCategoryCodec, map, true);
        return map;
    }


    private static <T> void scanDirectory(
        ResourceManager resourceManager, 
        FileToIdConverter lister, 
        DynamicOps<JsonElement> ops, 
        Codec<? extends T> codec, 
        Map<ResourceLocation, T> output,
        boolean scanningForCategory
    ) {
        var conditionalCodec = net.neoforged.neoforge.common.conditions.ConditionalOps.createConditionalCodec(codec);
        for (Entry<ResourceLocation, Resource> entry : lister.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = lister.fileToId(location);

            try {
                String filename = location.getPath().substring(location.getPath().lastIndexOf("/")+1); // skill nodes data files must be in one tab directory
                boolean shouldProcess = scanningForCategory ? filename.equals(FILENAME_TAB_DATA) : !filename.equals(FILENAME_TAB_DATA);
                
                if (shouldProcess) {
                    ResourceLocation outputId = scanningForCategory ? id.withPath(id.getPath().substring(0, id.getPath().lastIndexOf("/"))) : id;
                    
                    try (Reader reader = entry.getValue().openAsReader()) {
                        conditionalCodec.parse(ops, JsonParser.parseReader(reader)).ifSuccess(optional -> {
                            if (optional.isEmpty()) {
                                LOGGER.debug("Skipping loading data file '{}' from '{}' as its conditions were not met", outputId, location);
                            } else if (output.putIfAbsent(outputId, optional.get()) != null) {
                                throw new IllegalStateException("Duplicate data file ignored with ID " + outputId);
                            }
                        }).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", outputId, location, error));
                    } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                        LOGGER.error("Couldn't parse data file '{}' from '{}'", outputId, location, jsonparseexception);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                LOGGER.info("Ignoring data file '{}' from '{}' because it is not in any directories for skill_tree category. Place it in at least one sub-directory under data/<namespace>/{}/skill_tree", id, location, MODID);
                continue;
            }
        }
    }

}