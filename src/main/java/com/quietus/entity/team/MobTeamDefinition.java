package com.quietus.entity.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/** A single, data-driven group of mobs which are friendly to one another. */
public record MobTeamDefinition(List<Member> members) {
    public static final Codec<MobTeamDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Member.CODEC.listOf().fieldOf("members").forGetter(MobTeamDefinition::members)
    ).apply(instance, MobTeamDefinition::new));

    public boolean contains(Entity entity) {
        return members.stream().anyMatch(member -> member.matches(entity));
    }

    public record Member(ResourceKey<EntityType<?>> entityType, TagKey<EntityType<?>> entityTag) {
        public static final Codec<Member> CODEC = Codec.STRING.comapFlatMap(Member::decode, Member::encode);

        private static DataResult<Member> decode(String value) {
            boolean isTag = value.startsWith("#");
            String identifierText = isTag ? value.substring(1) : value;
            Identifier identifier = Identifier.tryParse(identifierText);
            if (identifier == null) {
                return DataResult.error(() -> "Invalid entity or entity tag identifier: " + value);
            }

            return DataResult.success(isTag
                    ? new Member(null, TagKey.create(Registries.ENTITY_TYPE, identifier))
                    : new Member(ResourceKey.create(Registries.ENTITY_TYPE, identifier), null));
        }

        private static String encode(Member member) {
            if (member.entityTag != null) {
                return "#" + member.entityTag.location();
            }
            return member.entityType.identifier().toString();
        }

        public boolean matches(Entity entity) {
            return entityTag != null
                    ? entity.typeHolder().is(entityTag)
                    : entity.typeHolder().is(entityType);
        }
    }
}
