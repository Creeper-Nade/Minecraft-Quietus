package com.minecraftquietus.quietus.skilltree;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public class SkillPointProgress implements Comparable<SkillPointProgress> {
    private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT).xmap(Instant::from, (p_300659_) -> {
        return p_300659_.atZone(ZoneId.systemDefault());
    });
    public static final Codec<SkillPointProgress> CODEC = RecordCodecBuilder.<SkillPointProgress>create((instance) -> 
        instance.group(
            OBTAINED_TIME_CODEC.listOf().optionalFieldOf("obtained_times",List.of()).forGetter(SkillPointProgress::obtainedTimes),
            Codec.BOOL.fieldOf("can_progress").orElse(false).forGetter(SkillPointProgress::isProgressing)
        ).apply(instance, (listTimes, canProgress) -> new SkillPointProgress(listTimes))
    );

    /**
     * Data that are synced to client. Client does not need specific times, so 
     * here is just giving the list size of times.
     */
    public record ClientData(
        int times,
        int maxAmount,
        int progressAmount
    ) {  
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientData> STREAM_CODEC = 
            StreamCodec.composite(
                ByteBufCodecs.INT, ClientData::times,
                ByteBufCodecs.INT, ClientData::maxAmount,
                ByteBufCodecs.INT, ClientData::progressAmount,
                ClientData::new
            );
        public boolean isMaxed() {
            return this.times >= this.maxAmount;
        }
        public boolean isProgressing() {
            return this.times >= this.progressAmount;
        }
    }

    public ClientData asClientData() {
        return new ClientData(times.size(), maxAmount, progressAmount);
    }

    private List<Instant> times = new ArrayList<>();
    private int maxAmount;
    private int progressAmount;

    public SkillPointProgress(List<Instant> times) {
        this.times.addAll(times);
    }
    public SkillPointProgress(List<Instant> times, SkillPoint skill) {
        this(times, skill.maxAmount(), skill.progressAmount());
    }
    public SkillPointProgress(List<Instant> times, int maxAmount, int progressAmount) {
        this.times.addAll(times);
        this.times.sort(Instant::compareTo);
        this.maxAmount = maxAmount;
        this.progressAmount = progressAmount;
    }



    /**
     * Adds obtained time to this skill. Will discard the earliest time if reaches maximum 
     * @param time Instant of obtained time
     */
    public void addObtainedTime(Instant time) {
        this.times.add(time);
        this.times.sort(Instant::compareTo);
        if (this.times.size() > this.maxAmount) {
            this.times.removeLast();
        }
    }
    public void clearObtainedTimes() {
        this.times.clear();
    }

    public List<Instant> obtainedTimes() {
        return this.times;
    }

    public int getAmount() {
        return this.times.size();
    }
    public int getMaxAmount() {
        return this.maxAmount;
    }

    public boolean isProgressing() {
        return this.getAmount() >= this.progressAmount;
    }
    public boolean isMaxed() {
        return this.getAmount() >= this.maxAmount;
    }
    @Nullable
    public Instant getFirstProgressDate() {
        return (Instant)this.times.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }
    @Override
    public int compareTo(SkillPointProgress other) {
        Instant i1 = this.getFirstProgressDate();
        Instant i2 = other.getFirstProgressDate();
        if (i1 == null && i2 != null) {
            return 1;
        } else if (i1 != null && i2 == null) {
            return -1;
        } else {
            return i1 == null && i2 == null ? 0 : i1.compareTo(i2);
        }
    }
}
