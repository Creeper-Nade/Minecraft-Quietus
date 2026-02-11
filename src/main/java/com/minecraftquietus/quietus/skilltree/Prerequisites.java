package com.minecraftquietus.quietus.skilltree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record Prerequisites(
    Map<String, ResourceLocation> advancements,
    Map<String, ResourceLocation> parents,
    Requirements requirements
) {
    /* Constructor for stream codec, which does not send advancements across network to client */
    private Prerequisites(Map<String, ResourceLocation> parents, Requirements requirements) {
        this(null, parents, requirements);
    }
    
    public static final Prerequisites EMPTY = new Prerequisites(Map.of(), Map.of(), Requirements.EMPTY);

    private static final Codec<Map<String, ResourceLocation>> ADVANCEMENTS_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC);
    private static final Codec<Map<String, ResourceLocation>> PARENTS_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC);
    public static final Codec<Prerequisites> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ADVANCEMENTS_MAP_CODEC.optionalFieldOf("advancements", Map.of()).forGetter(Prerequisites::advancements),
            PARENTS_MAP_CODEC.optionalFieldOf("parents", Map.of()).forGetter(Prerequisites::parents),
            Requirements.CODEC.optionalFieldOf("requirements").forGetter((prerequisites)-> Optional.of(prerequisites.requirements()))
        ).apply(instance, (advancementsMap, parentsMap, optionalRequirements) -> {
            Set<String> keys = new HashSet();
            keys.addAll(advancementsMap.keySet());
            keys.addAll(parentsMap.keySet());
            return new Prerequisites(advancementsMap, parentsMap, optionalRequirements.orElseGet(() -> Requirements.allOf(keys)));
        })
    );
    
    public static final StreamCodec<FriendlyByteBuf,Prerequisites> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ResourceLocation.STREAM_CODEC, 256), Prerequisites::parents,
        Requirements.STREAM_CODEC, Prerequisites::requirements,
        Prerequisites::new
    );
        

    /**
     * Gets ids of all parent ever mentioned in the requirements (all including "must" and "or")
     * @return Set of ResourceLocation of parents
     */
    public Set<ResourceLocation> getAllParents() {
        Set<ResourceLocation> out = new HashSet<>();
        this.parents.keySet().forEach((key) -> {
            this.requirements.forEach((list) -> list.forEach((requirement) -> {
                if (key.equals(requirement)) out.add(this.parents.get(key));
            }));
        });
        return out;
    }

    /**
     * Gets ids of all parents ever mentioned in the requirements, 
     * that are must demanded by this prerequisite
     * @return Set of ResourceLocation of "must" parents
     */
    public Set<ResourceLocation> getAllMustParents() {
        Set<ResourceLocation> out = new HashSet<>();
        List<List<String>> reqs = this.requirements.requirements();
        if (reqs.isEmpty()) return out;

        Set<String> mustParents = null;

        for (List<String> list : reqs) {
            Set<String> currentParents = new HashSet<>();
            for (String s : list) {
                if (this.parents.containsKey(s)) {
                    currentParents.add(s);
                }
            }

            if (mustParents == null) {
                mustParents = currentParents;
            } else {
                mustParents.retainAll(currentParents);
            }

            if (mustParents.isEmpty()) break;
        }

        if (mustParents != null) {
            for (String s : mustParents) {
                out.add(this.parents.get(s));
            }
        }
        return out;
    }
    /**
     * Gets ids of all parents ever mentioned in the requirements, 
     * that are demanded by this prerequisite as alternatives to other parents
     * @return Set of ResourceLocation of "or" parents
     */
    public Set<ResourceLocation> getAllOrParents() {
        Set<ResourceLocation> out = new HashSet<>();
        Set<ResourceLocation> mustParents = getAllMustParents();

        this.requirements.forEach((list) -> {
            for (String s : list) {
                if (this.parents.containsKey(s)) {
                    ResourceLocation loc = this.parents.get(s);
                    if (!mustParents.contains(loc)) {
                        out.add(loc);
                    }
                }
            }
        });
        return out;
    }

    public record Requirements(
        List<List<String>> requirements
    ) {
        public static final Requirements EMPTY = new Requirements(List.of());

        public static final Codec<Requirements> CODEC = Codec.STRING.listOf().listOf().xmap(Requirements::new, Requirements::requirements);

        public static final StreamCodec<FriendlyByteBuf,Requirements> STREAM_CODEC = StreamCodec.ofMember(
            (requirements,buffer) -> {
                ((FriendlyByteBuf)buffer).writeCollection(((Requirements)requirements).requirements(), (buffer2, list) -> buffer2.writeCollection(list, FriendlyByteBuf::writeUtf));
            },
            (buffer) ->
                new Requirements(((FriendlyByteBuf)buffer).readList(buffer2-> buffer2.readList(FriendlyByteBuf::readUtf)))
        );

        public static Requirements allOf(Collection<String> args) {
            return new Requirements(args.stream().map(List::of).toList());
        }
        public static Requirements anyOf(Collection<String> args) {
            return new Requirements(List.of(List.copyOf(args)));
        }

        public int size() {
            return this.requirements.size();
        }
        public boolean isEmpty() {
            return this.requirements.isEmpty();
        }
        public void forEach(Consumer<? super List<String>> func) {
            this.requirements.forEach(func);
        }

        /**
         * Testing for completion of this prerequisite
         * @param predicate a string predicate to decide whether the said string is finished.
         * @return true if passed (including when there are no requirements), false if any failed.
         */
        public boolean test(Predicate<String> predicate) {
            Iterator<List<String>> iter = this.requirements.iterator();

            List<String> list;
            do {
                if (!iter.hasNext()) {
                    return true;
                }
                list = iter.next();
            } while (anyMatch(list, predicate));

            return false;
        }

        /*public int count(Predicate<String> filter) {
            int i = 0;

            for (List<String> list : this.requirements) {
                if (anyMatch(list, filter)) {
                    i++;
                }
            }

            return i;
        }*/

        private static boolean anyMatch(List<String> requirements, Predicate<String> predicate) {
            for (String s : requirements) {
                if (predicate.test(s)) {
                    return true;
                }
            }

            return false;
        }

        public DataResult<Requirements> validate(Set<String> requirements) {
            Set<String> set = new ObjectOpenHashSet<>();

            for (List<String> list : this.requirements) {
                if (list.isEmpty() && requirements.isEmpty()) {
                    return DataResult.error(() -> "Requirement entry cannot be empty");
                }

                set.addAll(list);
            }

            if (!requirements.equals(set)) {
                Set<String> set1 = Sets.difference(requirements, set);
                Set<String> set2 = Sets.difference(set, requirements);
                return DataResult.error(
                    () -> "Skill tree node completion requirements did not exactly match specified advancements. Missing: " + set1 + ". Unknown: " + set2
                );
            } else {
                return DataResult.success(this);
            }
        }

        public interface Strategy {
            Requirements.Strategy AND = Requirements::allOf;
            Requirements.Strategy OR = Requirements::anyOf;

            Requirements create(Collection<String> criteria);
        }
    }

    public record DisplayInfo(
        Map<String, Component> advancements
    ) {
        private static final Codec<Map<String, Component>> CRITERIA_DISPLAY_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC);
        public static final Codec<Prerequisites.DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                CRITERIA_DISPLAY_MAP_CODEC.optionalFieldOf("advancements", Map.of()).forGetter(DisplayInfo::advancements)
            ).apply(instance, DisplayInfo::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf,Map<String,Component>> CRITIERIA_DISPLAY_MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, 
            ByteBufCodecs.STRING_UTF8, 
            ComponentSerialization.STREAM_CODEC,
            256
        );
        public static final StreamCodec<RegistryFriendlyByteBuf,DisplayInfo> STREAM_CODEC = StreamCodec.composite(
            CRITIERIA_DISPLAY_MAP_STREAM_CODEC, DisplayInfo::advancements,
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
