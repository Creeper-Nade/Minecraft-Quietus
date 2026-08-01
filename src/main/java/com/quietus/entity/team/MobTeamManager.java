package com.quietus.entity.team;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/** Holds the mob teams loaded from data packs under data/&lt;namespace&gt;/mob_teams. */
public final class MobTeamManager extends SimpleJsonResourceReloadListener<MobTeamDefinition> {
    public static final MobTeamManager INSTANCE = new MobTeamManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private volatile List<MobTeamDefinition> teams = List.of();

    private MobTeamManager() {
        super(MobTeamDefinition.CODEC, FileToIdConverter.json("mob_teams"));
    }

    @Override
    protected void apply(Map<Identifier, MobTeamDefinition> definitions,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        teams = List.copyOf(definitions.values());
        LOGGER.info("Loaded {} Quietus mob team(s)", teams.size());
    }

    /** Players and non-mob entities are deliberately excluded from this system. */
    public boolean areAllies(Entity first, Entity second) {
        if (first == second) {
            return true;
        }
        if (!(first instanceof Mob) || !(second instanceof Mob)) {
            return false;
        }

        for (MobTeamDefinition team : teams) {
            if (team.contains(first) && team.contains(second)) {
                return true;
            }
        }
        return false;
    }
}
