package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.minecraftquietus.quietus.client.screens.skill_tree.SkillPointType;
import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.server.QuietusReloadableResources;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record SkillPoint(
    int maxAmount,
    int progressAmount, // minimum amount of this skill point needs to be upgraded for its children to consider it "complete" (for prerequisites checking)
    Prerequisites prerequisites,
    List<Reward> rewards,
    Optional<DisplayInfo> display
) {
    private SkillPoint(int maxAmount, Prerequisites prerequisites, List<Reward> rewards, Optional<DisplayInfo> display) {
        this(maxAmount, 1, prerequisites, rewards, display);
    }

    public static final Codec<SkillPoint> CODEC = RecordCodecBuilder.<SkillPoint>create(
        instance -> instance.group(
            Codec.INT.optionalFieldOf("max_amount", 1).forGetter(SkillPoint::maxAmount),
            Codec.INT.optionalFieldOf("amount_to_progress", 1).forGetter(SkillPoint::progressAmount),
            Prerequisites.CODEC.optionalFieldOf("prerequisites", Prerequisites.EMPTY).forGetter(SkillPoint::prerequisites),
            Reward.CODEC.listOf().fieldOf("rewards").forGetter(SkillPoint::rewards),
            DisplayInfo.CODEC.optionalFieldOf("display").forGetter(SkillPoint::display)
        ).apply(instance, SkillPoint::new)
    )
    .validate(SkillPoint::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillPoint> STREAM_CODEC = StreamCodec.ofMember(SkillPoint::serializeToNetwork, SkillPoint::deserializeFromNetwork);


    public void apply(Player player) {
        for (Reward action : this.rewards) {
            action.apply(player);
        }
    }

    private static DataResult<SkillPoint> validate(SkillPoint skillPoint) {
        Set<String> keys = new HashSet<>();
        keys.addAll(skillPoint.prerequisites().advancements().keySet());
        keys.addAll(skillPoint.prerequisites().parents().keySet());
        return skillPoint.prerequisites().requirements().validate(keys).map(requirements -> skillPoint);
    }

    private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
        int i = 0;
        if (this.display.isPresent()) {
            i |= 1;
        }
        buffer.writeInt(i);
        buffer.writeInt(this.maxAmount);
        Prerequisites.STREAM_CODEC.encode(buffer, this.prerequisites);
        buffer.writeCollection(this.rewards, Reward.STREAM_CODEC::encode);
        this.display.ifPresent((display) -> DisplayInfo.STREAM_CODEC.encode(buffer, display));
    }
    private static SkillPoint deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
        int i = buffer.readInt();
        int maxAmount = buffer.readInt();
        Prerequisites prerequisites = Prerequisites.STREAM_CODEC.decode(buffer);
        List<Reward> rewards = buffer.readCollection(ArrayList::new, Reward.STREAM_CODEC::decode);
        Optional<DisplayInfo> display = ((i & 1) != 0) ? Optional.of(DisplayInfo.STREAM_CODEC.decode(buffer)) : Optional.empty();
        return new SkillPoint(maxAmount, prerequisites, rewards, display);
    }


    public record DisplayInfo(
        SkillPointType type,
        Optional<ClientAsset> icon,
        Component header,
        Component description,
        Prerequisites.DisplayInfo prerequisites,
        boolean isHidden
    ) {
        public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                SkillPointType.CODEC.optionalFieldOf("type", SkillPointType.SQUARE).forGetter(DisplayInfo::type),
                ClientAsset.CODEC.optionalFieldOf("icon").forGetter(DisplayInfo::icon),
                ComponentSerialization.CODEC.fieldOf("header").forGetter(DisplayInfo::header),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description),
                Prerequisites.DisplayInfo.CODEC.fieldOf("prerequisites").forGetter(DisplayInfo::prerequisites),
                Codec.BOOL.optionalFieldOf("hidden", false).forGetter(DisplayInfo::isHidden)
            ).apply(instance, DisplayInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf,DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(
            DisplayInfo::serializeToNetwork, DisplayInfo::deserializeFromNetwork
        );

        private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
            int i = 0;
            if (this.icon.isPresent()) {
                i |= 1;
            }
            if (this.isHidden) {
                i |= 2;
            }
            buffer.writeInt(i);
            buffer.writeInt(this.type.index());
            this.icon.map(ClientAsset::id).ifPresent(buffer::writeResourceLocation);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.header);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.description);
            Prerequisites.DisplayInfo.STREAM_CODEC.encode(buffer, this.prerequisites);
        }
        private static DisplayInfo deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
            int i = buffer.readInt();
            int typeIndex = buffer.readInt();
            SkillPointType type = null;
            for (SkillPointType t : SkillPointType.values()) {
                if (t.index() == typeIndex) 
                    type = t;
            }
            Optional<ClientAsset> icon = (i&1)!=0 ? Optional.of(new ClientAsset(buffer.readResourceLocation())) : Optional.empty();
            boolean isHidden = (i & 2) != 0;
            Component header = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Prerequisites.DisplayInfo prerequisitesDisplayInfo = Prerequisites.DisplayInfo.STREAM_CODEC.decode(buffer);

            return new DisplayInfo(Objects.requireNonNullElseGet(type, () -> SkillPointType.values()[0]), icon, header, description, prerequisitesDisplayInfo, isHidden);
        }
    }


    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    } 
}
