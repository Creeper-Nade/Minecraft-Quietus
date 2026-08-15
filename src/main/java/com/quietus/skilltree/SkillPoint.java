package com.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.quietus.client.screens.skill_tree.SkillPointType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.ClientAsset;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record SkillPoint(
    int maxAmount,
    int progress,
    LayoutInfo layout,
    UnlockInfo unlock,
    Rewards rewards,
    Optional<DisplayInfo> display
) {
    /*private SkillPoint(int maxAmount, int progress, UnlockInfo unlock, RewardsInfo rewards, Optional<DisplayInfo> display) {
        this(maxAmount, progress, layout, unlock, rewards, display);
    }*/

    public static final Codec<SkillPoint> CODEC = RecordCodecBuilder.<SkillPoint>create(
        instance -> instance.group(
            Codec.INT.optionalFieldOf("max_amount", 1).forGetter(SkillPoint::maxAmount),
            Codec.INT.optionalFieldOf("progress", 1).forGetter(SkillPoint::progress),
            LayoutInfo.CODEC.fieldOf("layout").forGetter(SkillPoint::layout),
            UnlockInfo.CODEC.optionalFieldOf("unlock", UnlockInfo.EMPTY).forGetter(SkillPoint::unlock),
            Rewards.CODEC.optionalFieldOf("rewards", Rewards.EMPTY).forGetter(SkillPoint::rewards),
            DisplayInfo.CODEC.optionalFieldOf("display").forGetter(SkillPoint::display)
        ).apply(instance, SkillPoint::new)
    )
    .validate(SkillPoint::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillPoint> STREAM_CODEC = StreamCodec.ofMember(SkillPoint::serializeToNetwork, SkillPoint::deserializeFromNetwork);


    public void apply(Player player) {
        this.rewards.apply(player);
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
        buffer.writeInt(this.progress);
        LayoutInfo.STREAM_CODEC.encode(buffer, this.layout);
        UnlockInfo.STREAM_CODEC.encode(buffer, this.unlock);
        Rewards.STREAM_CODEC.encode(buffer, this.rewards);
        this.display.ifPresent((display) -> DisplayInfo.STREAM_CODEC.encode(buffer, display));
    }
    private static SkillPoint deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
        int i = buffer.readInt();
        int maxAmount = buffer.readInt();
        int progress = buffer.readInt();
        LayoutInfo layout = LayoutInfo.STREAM_CODEC.decode(buffer);
        UnlockInfo unlock = UnlockInfo.STREAM_CODEC.decode(buffer);
        Rewards rewards = Rewards.STREAM_CODEC.decode(buffer);
        Optional<DisplayInfo> display = ((i & 1) != 0) ? Optional.of(DisplayInfo.STREAM_CODEC.decode(buffer)) : Optional.empty();
        return new SkillPoint(maxAmount, progress, layout, unlock, rewards, display);
    }

    public record Rewards(
        List<Reward> skills,
        Optional<Identifier> function
    ) {
        public static final Rewards EMPTY = new Rewards(List.of(), Optional.empty());

        public static final Codec<Rewards> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Reward.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(Rewards::skills),
                Identifier.CODEC.optionalFieldOf("function").forGetter(Rewards::function)
            ).apply(instance, Rewards::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Rewards> STREAM_CODEC = StreamCodec.ofMember(
            Rewards::serializeToNetwork, Rewards::deserializeFromNetwork
        );

        private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
            buffer.writeCollection(this.skills, Reward.STREAM_CODEC::encode);
            buffer.writeOptional(this.function, FriendlyByteBuf::writeIdentifier);
        }

        private static Rewards deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
            List<Reward> skills = buffer.readCollection(ArrayList::new, Reward.STREAM_CODEC::decode);
            Optional<Identifier> function = buffer.readOptional(FriendlyByteBuf::readIdentifier);
            return new Rewards(skills, function);
        }

        public void apply(Player player) {
            for (Reward action : this.skills) {
                action.apply(player);
            }
            if (this.function.isPresent() && player instanceof ServerPlayer serverPlayer) {
                MinecraftServer server = serverPlayer.level().getServer();
                if (server != null) {
                    server.getFunctions().get(this.function.get()).ifPresent(commandFunction -> {
                        server.getFunctions().execute(commandFunction, serverPlayer.createCommandSourceStack().withSuppressedOutput());
                    });
                }
            }
        }
    }

    public record UnlockInfo(
        Prerequisites prerequisites
    ) {
        public static final UnlockInfo EMPTY = new UnlockInfo(Prerequisites.EMPTY);

        public static final Codec<UnlockInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Prerequisites.CODEC.optionalFieldOf("prerequisites", Prerequisites.EMPTY).forGetter(UnlockInfo::prerequisites)
            ).apply(instance, UnlockInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, UnlockInfo> STREAM_CODEC = StreamCodec.composite(
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
                Prerequisites.CODEC.optionalFieldOf("prerequisites", Prerequisites.EMPTY).forGetter(LayoutInfo::prerequisites)
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
        Optional<ClientAsset.ResourceTexture> icon,
        Component header,
        Component description,
        Prerequisites.DisplayInfo prerequisites
    ) {
        public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                SkillPointType.CODEC.optionalFieldOf("type", SkillPointType.SQUARE).forGetter(DisplayInfo::type),
                ClientAsset.ResourceTexture.CODEC.optionalFieldOf("icon").forGetter(DisplayInfo::icon),
                ComponentSerialization.CODEC.fieldOf("header").forGetter(DisplayInfo::header),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description),
                Prerequisites.DisplayInfo.CODEC.optionalFieldOf("prerequisites", Prerequisites.DisplayInfo.EMPTY).forGetter(DisplayInfo::prerequisites)
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
            this.icon.map(ClientAsset::id).ifPresent(buffer::writeIdentifier);
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
            Optional<ClientAsset.ResourceTexture> icon = (i&1)!=0 ? Optional.of(new ClientAsset.ResourceTexture(buffer.readIdentifier())) : Optional.empty();
            Component header = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Prerequisites.DisplayInfo prerequisitesDisplayInfo = Prerequisites.DisplayInfo.STREAM_CODEC.decode(buffer);

            return new DisplayInfo(Objects.requireNonNullElseGet(type, () -> SkillPointType.values()[0]), icon, header, description, prerequisitesDisplayInfo);
        }

        public final static Function<String,Component> FUNC_DEFAULT_HEADING = (languageKey) -> Component.translatable(String.join(".", "skillTree", languageKey, "header"));
        public final static Function<String,Component> FUNC_DEFAULT_DESCRIPTION = (languageKey) -> Component.translatable(String.join(".", "skillTree", languageKey, "description"));
    }


    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    } 
}
