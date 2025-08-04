package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;

public record Prerequisites(
    Map<String, Criterion<?>> criteria, 
    Set<Set<ResourceLocation>> parents 
) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_MAP_CODEC = Codec.unboundedMap(Codec.STRING, Criterion.CODEC);
    public static final Codec<Prerequisites> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            CRITERIA_MAP_CODEC.optionalFieldOf("criteria", Map.of()).forGetter(Prerequisites::criteria),
            ResourceLocation.CODEC.listOf().listOf().optionalFieldOf("parent", List.of()).forGetter(Prerequisites::encodeThisParentSetToList)
        ).apply(instance, (criteriaMap, parentListList) -> {
            Set<Set<ResourceLocation>> setset = new HashSet<>();
            for (List<ResourceLocation> list : parentListList) {
                Set<ResourceLocation> set = new HashSet<>();
                for (ResourceLocation i : list) {
                    set.add(i);
                }
                setset.add(set);
            }
            return new Prerequisites(criteriaMap, setset);
        })
    );
    private List<List<ResourceLocation>> encodeThisParentSetToList() {
        List<List<ResourceLocation>> out = new ArrayList<>();
        for (Set<ResourceLocation> set : this.parents) {
            List<ResourceLocation> list = new ArrayList<>();
            for (ResourceLocation i : set) {
                list.add(i);
            }
            out.add(list);
        }
        return out;
    }

    /**
     * Gets all parent ever mentioned in the parents of set of set.
     * @return Set of ResourceLocation of parents.
     */
    public Set<ResourceLocation> getAllParents() {
        Set<ResourceLocation> out = new HashSet();
        this.parents.forEach((set) -> set.forEach(out::add));
        return out;
    }

    public static final Prerequisites EMPTY = new Prerequisites(Map.of(), Set.of());

    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    }
}
