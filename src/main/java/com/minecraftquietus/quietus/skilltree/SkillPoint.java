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
    LayoutInfo layout,
    UnlockInfo unlock,
    List<Reward> rewards,
    Optional<DisplayInfo> display
) {
    /*private SkillPoint(int maxAmount, UnlockInfo unlock, List<Reward> rewards, Optional<DisplayInfo> display) {
        this(maxAmount, 1, unlock, rewards, display);
    }*/

    public static final Codec<SkillPoint> CODEC = RecordCodecBuilder.<SkillPoint>create(
        instance -> instance.group(
            Codec.INT.optionalFieldOf("max_amount", 1).forGetter(SkillPoint::maxAmount),
            LayoutInfo.CODEC.fieldOf("layout").forGetter(SkillPoint::layout),
            UnlockInfo.CODEC.fieldOf("unlock").forGetter(SkillPoint::unlock),
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
        Set<String> layoutKeys = new HashSet<>();
        layoutKeys.addAll(skillPoint.layout.prerequisites.advancements().keySet());
        layoutKeys.addAll(skillPoint.layout.prerequisites.parents().keySet());
        Set<String> unlockKeys = new HashSet<>();
        unlockKeys.addAll(skillPoint.unlock.prerequisites.advancements().keySet());
        unlockKeys.addAll(skillPoint.unlock.prerequisites.parents().keySet());

        return skillPoint.layout.prerequisites.requirements()
            .validate(layoutKeys)
            .apply2(
                (layoutReq, unlockReq) -> skillPoint,
                skillPoint.unlock.prerequisites.requirements().validate(unlockKeys)
            );
    }

    private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
        int i = 0;
        if (this.display.isPresent()) {
            i |= 1;
        }
        buffer.writeInt(i);
        buffer.writeInt(this.maxAmount);
        LayoutInfo.STREAM_CODEC.encode(buffer, this.layout);
        UnlockInfo.STREAM_CODEC.encode(buffer, this.unlock);
        buffer.writeCollection(this.rewards, Reward.STREAM_CODEC::encode);
        this.display.ifPresent((display) -> DisplayInfo.STREAM_CODEC.encode(buffer, display));
    }
    private static SkillPoint deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
        int i = buffer.readInt();
        int maxAmount = buffer.readInt();
        LayoutInfo layout = LayoutInfo.STREAM_CODEC.decode(buffer);
        UnlockInfo unlock = UnlockInfo.STREAM_CODEC.decode(buffer);
        List<Reward> rewards = buffer.readCollection(ArrayList::new, Reward.STREAM_CODEC::decode);
        Optional<DisplayInfo> display = ((i & 1) != 0) ? Optional.of(DisplayInfo.STREAM_CODEC.decode(buffer)) : Optional.empty();
        return new SkillPoint(maxAmount, layout, unlock, rewards, display);
    }

    public record UnlockInfo(
        int progress,
        Prerequisites prerequisites
    ) {

        public static final Codec<UnlockInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.INT.fieldOf("progress").forGetter(UnlockInfo::progress),
                Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(UnlockInfo::prerequisites)
            ).apply(instance, UnlockInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf,UnlockInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UnlockInfo::progress,
            Prerequisites.STREAM_CODEC, UnlockInfo::prerequisites,
            UnlockInfo::new
        );
    }

    public record LayoutInfo(
        boolean top,
        Prerequisites prerequisites
    ) {
        public static final Codec<LayoutInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.BOOL.optionalFieldOf("top",false).forGetter(LayoutInfo::top),
                Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(LayoutInfo::prerequisites)
            ).apply(instance, LayoutInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf,LayoutInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, LayoutInfo::top,
            Prerequisites.STREAM_CODEC, LayoutInfo::prerequisites,
            LayoutInfo::new
        );
    }

    public record DisplayInfo(
        SkillPointType type,
        Optional<ClientAsset> icon,
        Component header,
        Component description,
        Prerequisites.DisplayInfo prerequisites
    ) {
        public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                SkillPointType.CODEC.optionalFieldOf("type", SkillPointType.SQUARE).forGetter(DisplayInfo::type),
                ClientAsset.CODEC.optionalFieldOf("icon").forGetter(DisplayInfo::icon),
                ComponentSerialization.CODEC.fieldOf("header").forGetter(DisplayInfo::header),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description),
                Prerequisites.DisplayInfo.CODEC.fieldOf("prerequisites").forGetter(DisplayInfo::prerequisites)
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
            Component header = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Prerequisites.DisplayInfo prerequisitesDisplayInfo = Prerequisites.DisplayInfo.STREAM_CODEC.decode(buffer);

            return new DisplayInfo(Objects.requireNonNullElseGet(type, () -> SkillPointType.values()[0]), icon, header, description, prerequisitesDisplayInfo);
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
