package com.quietus.server;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.quietus.Quietus;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import static com.quietus.server.QuietusReloadableResources.skillTreeManager;

public class PlayerData {

    private final MinecraftServer server;
    private final Path worldPath;

    private Map<UUID, PlayerSkillTree> playerSkillTree = new LinkedHashMap<>();
    private static final String PATHNAME_SKILLTREE = "skilltree";

    public PlayerData(MinecraftServer server) {
        this.server = server;
        this.worldPath = server.getWorldPath(LevelResource.ROOT);
        server.getPlayerList().getPlayers().forEach(this::loadPlayer);
    }
    

    /**
     * Reloads all the data of the player. Does {@link PlayerData#loadPlayer(ServerPlayer)} instead if this player has never been loaded
     * @param player a ServerPlayer instance
     */
    public void reloadPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (this.playerSkillTree.containsKey(uuid)) {
            /* Skill tree */
            this.playerSkillTree.get(uuid).reload(skillTreeManager);
        } else {
            this.loadPlayer(player);
        }
    }
    /**
     * Loads all the data of the player. May override previous data if this player was already loaded.
     * @param player a ServerPlayer instance
     */
    public void loadPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Path playerSavePath = this.worldPath.resolve(Quietus.MODID).resolve(PATHNAME_SKILLTREE).resolve(uuid.toString());
        /* Skill tree */
        PlayerSkillTree tree = new PlayerSkillTree(playerSavePath, player, skillTreeManager);
        this.playerSkillTree.put(uuid, tree);
    }
    /**
     * Saves all the data of the player
     * @param player a ServerPlayer instance
     */
    public void savePlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerSkillTree removingSkillTree = this.playerSkillTree.remove(uuid);
        /* Skill tree */
        if (Objects.nonNull(removingSkillTree)) {
            removingSkillTree.save();
        }
    }

    public PlayerSkillTree getSkillTree(UUID uuid) {
        return this.playerSkillTree.get(uuid);
    }
}
