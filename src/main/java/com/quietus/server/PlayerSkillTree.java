package com.quietus.server;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.quietus.server.resources.ServerSkillTreeManager;
import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillPointProgress;
import com.quietus.skilltree.SkillTreeNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.FileUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSkillTree {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    
    private final Path playerSavePath;
    private ServerPlayer player;
    private Map<Identifier, SkillCategory> categories;
    private final Map<SkillTreeNode,SkillPointProgress> progresses = new LinkedHashMap<>();

    public PlayerSkillTree(Path playerSavePath, ServerPlayer player, ServerSkillTreeManager manager) {
        this.playerSavePath = playerSavePath;
        this.player = player;
        this.categories = manager.getCategories();
        this.load(manager);
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public void reload(ServerSkillTreeManager manager) {
        this.progresses.clear();
        this.categories = manager.getCategories();
        this.load(manager);
    }
    
    private void load(ServerSkillTreeManager manager) {
        if (Files.isRegularFile(this.playerSavePath, new LinkOption[0])) { // checks for whether the path is valid (i.e. file exists)
            try {
                JsonReader jsonreader = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8));
                try {
                    jsonreader.setLenient(false);
                    JsonElement jsonelement = Streams.parse(jsonreader);
                    Data playerskilltree$data = (Data)Data.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonParseException::new);
                    this.applyFrom(manager, playerskilltree$data);
                } catch (Throwable var6) {
                    try {
                        jsonreader.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
    
                    throw var6;
                }

                jsonreader.close();
            } catch (IOException | JsonIOException var7) {
                LOGGER.error("Couldn't access player skill tree in {}", this.playerSavePath, var7);
            } catch (JsonParseException var8) {
                LOGGER.error("Couldn't parse player skill tree in {}", this.playerSavePath, var8);
            }
        }
    }

    public void save() {
        JsonElement jsonelement = (JsonElement)Data.CODEC.encodeStart(JsonOps.INSTANCE, this.asData()).getOrThrow();

        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
            Writer writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8);

            try {
                GSON.toJson(jsonelement, GSON.newJsonWriter(writer));
            } catch (Throwable var6) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (writer != null) {
                writer.close();
            }
        } catch (IOException | JsonIOException var7) {
            LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, var7);
        }

    }

    public void applyFrom(ServerSkillTreeManager manager, Data data) {
        data.forEach((Identifier, progress) -> {
            SkillTreeNode node = null;
            for (SkillCategory category : manager.getCategories().values()) {
                node = category.getNode(Identifier);
            }
            if (node == null) {
                LOGGER.warn("Ignored skill tree node '{}' in file {} - it doesn't exist anymore?", Identifier, this.playerSavePath);
            } else {
                SkillPointProgress newProgress = new SkillPointProgress(progress.obtainedTimes(), node.getSkillPoint());
                this.progresses.put(node, newProgress);
            }
        });
    }

    /**
     * Add progress to the said node
     * @param node SkillTreeNode
     * @return <code>true</code> if successfully added. <code>false</code> if unsuccessful, due to the progress already being maxed
     */
    public boolean addOrStartProgress(SkillTreeNode node) {
        if (this.progresses.containsKey(node)) {
            SkillPointProgress progress = this.progresses.get(node);
            if (progress.isMaxed()) return false;
            progress.addObtainedTime(Instant.now());
            return true;
        } else {
            SkillPointProgress newProgress = new SkillPointProgress(List.of(), node.getSkillPoint());
            newProgress.addObtainedTime(Instant.now());
            this.progresses.put(node, newProgress);
            return true;
        }
    }

    public Data asData() {
        Map<Identifier, SkillPointProgress> map = new LinkedHashMap<>();
        this.progresses.forEach((skillTreeNode, progress) -> {
            map.put(skillTreeNode.getId(), progress);
        });
        return new Data(map);
    }

    public Map<SkillTreeNode,SkillPointProgress> getProgresses() {
        return this.progresses;
    }

    public record Data(Map<Identifier, SkillPointProgress> map) {
        public static final Codec<Data> CODEC = Codec.unboundedMap(Identifier.CODEC, SkillPointProgress.CODEC).xmap(Data::new, Data::map);

        public Data(Map<Identifier, SkillPointProgress> map) {
            this.map = map;
        }

        public void forEach(BiConsumer<Identifier, SkillPointProgress> action) {
            this.map.entrySet().stream().sorted(Entry.comparingByValue()).forEach((entry) -> {
                action.accept((Identifier)entry.getKey(), (SkillPointProgress)entry.getValue());
            });
        }
    }
}
