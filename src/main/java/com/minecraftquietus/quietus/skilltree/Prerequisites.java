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
        for (List<String> list : this.requirements.requirements()) {
            for (String reqKey : list) {
                ResourceLocation loc = this.parents.get(reqKey);
                if (loc != null) {
                    out.add(loc);
                }
            }
        }
        return out;
    }

    /**
     * Gets ids of all parents ever mentioned in the requirements, 
     * that when these parents are not met, the requirements will never succeed the test
     * @return Set of ResourceLocation of "must" parents
     */
    public Set<ResourceLocation> getAllMustParents() {
        Set<ResourceLocation> out = new HashSet<>();
        for (List<String> list : this.requirements.requirements()) {
            if (list.size() == 1) {
                ResourceLocation loc = this.parents. get(list.getFirst());
                if (loc != null) out.add(loc);
            }
        }
        return out;
    }
    /**
     * Gets ids of all parents ever mentioned in the requirements, 
     * that do not single-handedly decide failing of the requirements
     * @return Set of ResourceLocation of "or" parents
     */
    public Set<ResourceLocation> getAllOrParents() {
        Set<ResourceLocation> out = getAllParents();
        Set<ResourceLocation> musts = getAllMustParents();

        out.removeAll(musts);
        return out;
    }

    /**
     * Is a Conjunctive Normal Form boolean (boolean algebra).
     * Test with {@link Requirements#test} method.
     * @param requirements
     */
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

        /**
         * Subsumption Check: If a set is a superset of another, it is redundant in CNF.
         * E.g., [[A], [A, B]] simplifies to [[A]] because if A is true, [A, B] is automatically true.
         */
        private static List<Set<String>> applySubsumption(List<Set<String>> sets) {
            List<Set<String>> result = new java.util.ArrayList<>();
            for (int i = 0; i < sets.size(); i++) {
                Set<String> current = sets.get(i);
                boolean isSubsumed = false;
                for (int j = 0; j < sets.size(); j++) {
                    if (i == j) continue;
                    Set<String> other = sets.get(j);

                    // If 'other' is a subset of 'current', 'current' is a redundant, broader requirement
                    if (current.containsAll(other)) {
                        // Tie-breaker for identical sets
                        if (current.size() > other.size() || (current.size() == other.size() && i > j)) {
                            isSubsumed = true;
                            break;
                        }
                    }
                }
                if (!isSubsumed) result.add(current);
            }
            return result;
        }

        /**
         * Builds the Abstract Syntax Tree (AST) representing the requirements.
         * It is subsumed if possible.
         */
        public RequirementNode makeNestedNode() {
            List<Set<String>> baseSets = new java.util.ArrayList<>();
            for (List<String> list : this.requirements) {
                baseSets.add(new HashSet<>(list));
            }

            baseSets = applySubsumption(baseSets);
            return RequirementNode.factorize(baseSets);
        }

        public interface Strategy {
            Requirements.Strategy AND = Requirements::allOf;
            Requirements.Strategy OR = Requirements::anyOf;

            Requirements create(Collection<String> criteria);
        }
    }


    /**
     * NOTE: ai-generated slop below
     * AI-generated stuff for generating description text for the skill widget
     */

    public sealed interface RequirementNode permits LeafNode, AndNode, OrNode {
        /**
         * Recursively factors CNF sets into an Abstract Syntax Tree.
         */
        private static RequirementNode factorize(List<Set<String>> sets) {
            if (sets == null || sets.isEmpty()) return null;
            if (sets.stream().anyMatch(Set::isEmpty)) return null; // An empty set means impossible requirements? Or auto-pass depending on design.

            if (sets.size() == 1) {
                List<RequirementNode> leaves = sets.get(0).stream().map(LeafNode::new).collect(java.util.stream.Collectors.toList());
                return leaves.size() == 1 ? leaves.get(0) : new OrNode(leaves);
            }

            // Find the most frequently shared element to factor out
            Map<String, Integer> counts = new HashMap<>();
            for (Set<String> s : sets) {
                for (String item : s) counts.put(item, counts.getOrDefault(item, 0) + 1);
            }

            String bestElement = null;
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    bestElement = entry.getKey();
                }
            }

            if (maxCount < 2) {
                // No factorization possible, return standard AND of ORs
                List<RequirementNode> andChildren = new java.util.ArrayList<>();
                for (Set<String> s : sets) {
                    List<RequirementNode> orLeaves = s.stream().map(LeafNode::new).collect(java.util.stream.Collectors.toList());
                    andChildren.add(orLeaves.size() == 1 ? orLeaves.get(0) : new OrNode(orLeaves));
                }
                return new AndNode(andChildren);
            }

            // Factor out the best element
            List<Set<String>> withBest = new java.util.ArrayList<>();
            List<Set<String>> withoutBest = new java.util.ArrayList<>();

            for (Set<String> s : sets) {
                if (s.contains(bestElement)) {
                    Set<String> remainder = new HashSet<>(s);
                    remainder.remove(bestElement);
                    if (!remainder.isEmpty()) withBest.add(remainder);
                } else {
                    withoutBest.add(s);
                }
            }

            // Factorization math: (A OR B) AND (A OR C) => A OR (B AND C)
            RequirementNode remaindersFactored = factorize(withBest);
            List<RequirementNode> orChildren = new java.util.ArrayList<>();
            orChildren.add(new LeafNode(bestElement));
            if (remaindersFactored != null) orChildren.add(remaindersFactored);

            RequirementNode factoredGroup = orChildren.size() == 1 ? orChildren.get(0) : new OrNode(orChildren);

            // Combine with sets that didn't have the best element using AND
            if (withoutBest.isEmpty()) return factoredGroup;

            List<RequirementNode> topLevelAnd = new java.util.ArrayList<>();
            topLevelAnd.add(factoredGroup);

            RequirementNode withoutBestFactored = factorize(withoutBest);
            if (withoutBestFactored instanceof AndNode andNode) {
                topLevelAnd.addAll(andNode.children()); // Flatten nested ANDs
            } else if (withoutBestFactored != null) {
                topLevelAnd.add(withoutBestFactored);
            }

            return new AndNode(topLevelAnd);
        }
    }

    public record LeafNode(String key) implements RequirementNode {}

    public record AndNode(List<RequirementNode> children) implements RequirementNode {}

    public record OrNode(List<RequirementNode> children) implements RequirementNode {}


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
