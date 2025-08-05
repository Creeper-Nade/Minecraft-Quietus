package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;


public class SkillCategory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, SkillTreeNode> nodes = new Object2ObjectOpenHashMap<>();
    private final Set<SkillTreeNode> roots = new ObjectLinkedOpenHashSet<>();
    private final Set<SkillTreeNode> dependants = new ObjectLinkedOpenHashSet<>();
    
    private final Prerequisites prerequisites;
    private final Optional<DisplayInfo> display;

    private final ResourceLocation id;

    /* For CODEC decoding only, hence this constructor will not be visible */
    private SkillCategory(Prerequisites prerequisites, Optional<DisplayInfo> display) {
        this.prerequisites = prerequisites;
        this.id = null;
        this.display = display;
    }
    
    /* Constructs actual usable SkillCategory instances. */
    public SkillCategory(ResourceLocation id, Prerequisites prerequisites, Optional<DisplayInfo> display) {
        this.id = id;
        this.prerequisites = prerequisites;
        this.display = display;
    }

    public static final Codec<SkillCategory> CODEC = RecordCodecBuilder.create(
        (instance) -> instance.group(
            Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(SkillCategory::getPrerequisites),
            DisplayInfo.CODEC.optionalFieldOf("tab_display").forGetter(SkillCategory::getDisplay)
        ).apply(instance, SkillCategory::new) // should use the private constructor without id assignment.
    );

    public static final Codec<SkillCategory> STREAM_CODEC = 

    public record DisplayInfo(
        Optional<ClientAsset> icon,
        Component name,
        Component description,
        Prerequisites.DisplayInfo prerequisites
    ) {
        public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ClientAsset.CODEC.optionalFieldOf("icon").forGetter(DisplayInfo::icon),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(DisplayInfo::name),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description),
                Prerequisites.DisplayInfo.CODEC.fieldOf("prerequisites").forGetter(DisplayInfo::prerequisites)
            ).apply(instance, DisplayInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf,DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(DisplayInfo::serializeToNetwork, DisplayInfo::deserializeFromNetwork);

        private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
            int i = 0;
            if (this.icon.isPresent()) {
                i |= 1;
            }
            buffer.writeInt(i);
            this.icon.map(ClientAsset::id).ifPresent(buffer::writeResourceLocation);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.name);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.description);
            Prerequisites.DisplayInfo.STREAM_CODEC.encode(buffer, this.prerequisites);
        }
        private static DisplayInfo deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
            int i = buffer.readInt();
            Optional<ClientAsset> icon = (i&1)!=0 ? Optional.of(new ClientAsset(buffer.readResourceLocation())) : Optional.empty();
            Component name = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Prerequisites.DisplayInfo prerequisitesDisplayInfo = Prerequisites.DisplayInfo.STREAM_CODEC.decode(buffer);
            return new DisplayInfo(icon, name, description, prerequisitesDisplayInfo);
        }
    }

    public void addAll(Map<ResourceLocation, SkillPoint> map) {
        List<ResourceLocation> list = new ArrayList<>(map.keySet());

        while (!list.isEmpty()) {
            if (!list.removeIf((location) -> this.tryInsert(location, map.get(location)))) {
                LOGGER.error("Couldn't load skill tree nodes: {}", list);
                break;
            }
        }

        LOGGER.info("Loaded {} skill tree nodes for category {}", this.nodes.size(), this.id.toString());
    }

    private boolean tryInsert(ResourceLocation location, SkillPoint skillPoint) {
        Set<ResourceLocation> parents = skillPoint.prerequisites().getAllParents();
        List<SkillTreeNode> parentNodes = parents.isEmpty() ? new ArrayList<>() : parents.stream().map(this.nodes::get).collect(Collectors.toList());

        if (parentNodes.contains(null) && !parents.isEmpty()) { // this node should have parents, and that any of its parents are not created yet.
            return false;
        } else {
            SkillTreeNode node = new SkillTreeNode(location, skillPoint);

            this.nodes.put(location, node);
            
            if (!parentNodes.contains(null)) { // all parents already created
                /* Updating parents of this node and children of parents  */
                node.setParents(parentNodes);
                parentNodes.forEach((parent) -> parent.addChild(node));

                /* Updating roots and dependants */
                if (parents.isEmpty()) { // 没腐没木
                    this.roots.add(node);
                } else {
                    this.dependants.add(node);
                }

                return true;
            } else { // not all parents already created, AND this node should have more parents created: leave this node to be further improved
                return false;
            }
            
        }
    }

    public Prerequisites getPrerequisites() {
        return this.prerequisites;
    }

    public Optional<DisplayInfo> getDisplay() {
        return this.display;
    }

}
