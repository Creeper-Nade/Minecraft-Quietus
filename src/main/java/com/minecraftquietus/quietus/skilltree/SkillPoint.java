package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record SkillPoint(
    Prerequisites prerequisites,
    List<Reward> rewards,
    Optional<DisplayInfo> display
) {
    public static final Codec<SkillPoint> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Prerequisites.CODEC.optionalFieldOf("prerequisites", Prerequisites.EMPTY).forGetter(SkillPoint::prerequisites),
            Reward.CODEC.listOf().fieldOf("rewards").forGetter(SkillPoint::rewards),
            DisplayInfo.CODEC.optionalFieldOf("display").forGetter(SkillPoint::display)
        ).apply(instance, SkillPoint::new)
    );

    public void apply(Player player) {
        for (Reward action : this.rewards) {
            action.apply(player);
        }
    }


    public record DisplayInfo(
        Optional<ClientAsset> icon,
        Component header,
        Component description,
        Prerequisites.DisplayInfo prerequisites,
        boolean isHidden
    ) {
        public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ClientAsset.CODEC.optionalFieldOf("icon").forGetter(DisplayInfo::icon),
                ComponentSerialization.CODEC.fieldOf("header").forGetter(DisplayInfo::header),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description),
                Prerequisites.DisplayInfo.CODEC.fieldOf("prerequisites").forGetter(DisplayInfo::prerequisites),
                Codec.BOOL.optionalFieldOf("hidden", false).forGetter(DisplayInfo::isHidden)
            ).apply(instance, DisplayInfo::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf,DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(DisplayInfo::serializeToNetwork, DisplayInfo::deserializeFromNetwork);

        private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
            int i = 0;
            if (this.icon.isPresent()) {
                i |= 1;
            }
            if (this.isHidden) {
                i |= 2;
            }
            buffer.writeInt(i);
            this.icon.map(ClientAsset::id).ifPresent(buffer::writeResourceLocation);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.header);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.description);
            Prerequisites.DisplayInfo.STREAM_CODEC.encode(buffer, this.prerequisites);
        }
        private static DisplayInfo deserializeFromNetwork(RegistryFriendlyByteBuf buffer) {
            int i = buffer.readInt();
            Optional<ClientAsset> icon = (i&1)!=0 ? Optional.of(new ClientAsset(buffer.readResourceLocation())) : Optional.empty();
            boolean isHidden = (i & 2) != 0;
            Component header = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            Prerequisites.DisplayInfo prerequisitesDisplayInfo = Prerequisites.DisplayInfo.STREAM_CODEC.decode(buffer);
            return new DisplayInfo(icon, header, description, prerequisitesDisplayInfo, isHidden);
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
