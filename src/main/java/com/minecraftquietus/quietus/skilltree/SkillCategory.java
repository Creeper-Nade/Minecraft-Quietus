package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceLocation;


public class SkillCategory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, SkillTreeNode> nodes = new Object2ObjectOpenHashMap<>();
    private final Set<SkillTreeNode> roots = new ObjectLinkedOpenHashSet<>();
    private final Set<SkillTreeNode> dependants = new ObjectLinkedOpenHashSet<>();

    private static final int DEFAULT_MAX_WIDTH = 16;

    private final int maxWidth;
    private final Prerequisites prerequisites;
    private final Optional<DisplayInfo> display;

    private final ResourceLocation id;

    private SkillCategory.Listener listener;

    
    /* Constructs actual usable SkillCategory instances. */
    public SkillCategory(ResourceLocation id, int maxWidth, Prerequisites prerequisites, Optional<DisplayInfo> display) {
        this.id = id;
        this.maxWidth = maxWidth;
        this.prerequisites = prerequisites;
        this.display = display;
    }

    /* For CODEC decoding only */
    public static SkillCategory makeDecodedInstance(int maxWidth, Prerequisites prerequisites, Optional<DisplayInfo> display) {
        return new SkillCategory(null, maxWidth, prerequisites, display);
    }

    public static final Codec<SkillCategory> CODEC = RecordCodecBuilder.create(
        (instance) -> instance.group(
            Codec.INT.optionalFieldOf("max_nodes_per_layer",DEFAULT_MAX_WIDTH).forGetter(SkillCategory::maxWidth),
            Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(SkillCategory::prerequisites),
            DisplayInfo.CODEC.optionalFieldOf("tab_display").forGetter(SkillCategory::display)
        ).apply(instance, SkillCategory::makeDecodedInstance) // should use the private constructor without id assignment.
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillCategory> STREAM_CODEC = StreamCodec.ofMember(SkillCategory::serializeToNetwork, SkillCategory::deserializeFromNetwork);

    private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
        int i = 0;
        if (this.display.isPresent()) {
            i |= 1;
        }
        buffer.writeInt(i);
        buffer.writeInt(this.maxWidth);
        ResourceLocation.STREAM_CODEC.encode(buffer, this.id);
        Map<ResourceLocation,SkillPoint> map = new HashMap<>();
        this.nodes.forEach((key, value) -> map.put(key, value.getSkillPoint()));
        buffer.writeMap(
            map,
            (StreamEncoder<FriendlyByteBuf, ResourceLocation>) ResourceLocation.STREAM_CODEC::encode,
            (buf, value) -> SkillPoint.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, value)
            /* ResourceLocation.STREAM_CODEC::encode,
            SkillPoint.STREAM_CODEC::encode */
        );
        Prerequisites.STREAM_CODEC.encode(buffer, this.prerequisites);
        if (this.display.isPresent()) DisplayInfo.STREAM_CODEC.encode(buffer, this.display.get());
    }
    private static SkillCategory deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
        int i = buffer.readInt();
        int maxWidth = buffer.readInt();
        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
        Map<ResourceLocation,SkillPoint> map = buffer.readMap(
            buf -> ResourceLocation.STREAM_CODEC.decode((FriendlyByteBuf) buf),
            buf -> SkillPoint.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf)
        );
        Prerequisites prerequisites = Prerequisites.STREAM_CODEC.decode(buffer);
        Optional<DisplayInfo> display = ((i & 1) != 0) ? Optional.of(DisplayInfo.STREAM_CODEC.decode(buffer)) : Optional.empty();
        SkillCategory out = new SkillCategory(id, maxWidth, prerequisites, display);
        out.addAll(map);
        return out;
    }

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
        Set<ResourceLocation> parents = skillPoint.unlock().prerequisites().getAllParents(); // parents from unlock prerequisites, not layout prerequisites
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
                    if (!Objects.isNull(listener))
                        this.listener.onAddRootSkillNode(this.id, node);
                } else {
                    this.dependants.add(node);
                    if (!Objects.isNull(listener))
                        this.listener.onAddDependantSkillNode(this.id, node);
                }

                return true;
            } else { // not all parents already created, AND this node should have more parents created: leave this node to be further improved
                return false;
            }
        }
    }

    public ConnectivityPosition positionNodes(int nodeWidth, int nodeHeight) {
        Set<SkillTreeNode> nodes = new HashSet<>();
        nodes.addAll(this.roots);
        nodes.addAll(this.dependants);
        LegacyPosition positioning = new LegacyPosition(nodeWidth, nodeHeight);
        return positioning.layout(nodes);
    }

    public @Nullable SkillTreeNode getNode(ResourceLocation location) {
        return this.nodes.get(location);
    }

    protected Map<ResourceLocation, SkillTreeNode> getNodesMap() {
        return this.nodes;
    }

    public void setListener(@Nullable SkillCategory.Listener listener) {
        this.listener = listener;
        if (!Objects.isNull(listener)) {
            for (SkillTreeNode node : this.roots) {
                listener.onAddRootSkillNode(this.id, node);
            }
            for (SkillTreeNode node : this.dependants) {
                listener.onAddDependantSkillNode(this.id, node);
            }
        }
    }

    public interface Listener {
        void onAddRootSkillNode(ResourceLocation categoryId, SkillTreeNode node);
        
        void onAddDependantSkillNode(ResourceLocation categoryId, SkillTreeNode node);
    }

    public int maxWidth() {
        return this.maxWidth;
    }
    public Prerequisites prerequisites() {
        return this.prerequisites;
    }
    public Optional<DisplayInfo> display() {
        return this.display;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof SkillCategory otherCategory) {
            if (this.id == otherCategory.id) {
                return true;
            }
        }
        return false;
    }

}
