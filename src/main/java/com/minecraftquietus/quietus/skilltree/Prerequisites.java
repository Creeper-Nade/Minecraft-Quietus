package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
    public static final Codec<Prerequisites> STREAM_CODEC = StreamCodec.composite(
        

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


    public record DisplayInfo(
        Map<String, Component> criteria
    ) {
        private static final Codec<Map<String, Component>> CRITERIA_DISPLAY_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC);
        public static final Codec<Prerequisites.DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                CRITERIA_DISPLAY_MAP_CODEC.optionalFieldOf("criteria", Map.of()).forGetter(DisplayInfo::criteria)
            ).apply(instance, DisplayInfo::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf,Map<String,Component>> CRITIERIA_DISPLAY_MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, 
            ByteBufCodecs.STRING_UTF8, 
            ComponentSerialization.STREAM_CODEC,
            256
        );
        public static final StreamCodec<RegistryFriendlyByteBuf,DisplayInfo> STREAM_CODEC = StreamCodec.composite(
            CRITIERIA_DISPLAY_MAP_STREAM_CODEC, DisplayInfo::criteria,
            DisplayInfo::new
        );

    }

    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    }
}
