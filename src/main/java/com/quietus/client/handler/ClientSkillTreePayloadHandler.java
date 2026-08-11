package com.quietus.client.handler;

import static com.quietus.Quietus.MODID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.client.packet.SkillTreeAdvancementsGrantRevokePacket;
import com.quietus.client.packet.SkillTreeAdvancementsUpdatePacket;
import com.quietus.client.packet.SkillTreeUpdatePacket;
import com.quietus.client.screens.skill_tree.SkillTreeScreen;
import com.quietus.client.screens.skill_tree.SkillTreeTab;
import com.quietus.client.screens.skill_tree.SkillTreeTab.TabScrollData;
import com.quietus.skilltree.SkillCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ClientSkillTreePayloadHandler {

    private static ClientSkillTree skillTree;
    private static Minecraft minecraft = Minecraft.getInstance();

    private static Map<Identifier,SkillTreeTab.TabScrollData> scrollData = new HashMap<>();
    private static List<Identifier> tabsOrder = new ArrayList<>();
    @Nullable private static Identifier tabSelected = null;
    

    public static void initialize() {
        skillTree = new ClientSkillTree(minecraft);
    }

    public static void close() {
        skillTree = null;
        scrollData.clear();
        tabsOrder.clear();
        tabSelected = null;
    }

    public static void handleSkillTreeUpdate(SkillTreeUpdatePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            skillTree.update(packet);
            Screen screen = minecraft.screen;
            if (!Objects.isNull(screen) && screen instanceof SkillTreeScreen skillTreeScreen) {
                skillTreeScreen.makeTabs();
            }
        })
        .exceptionally(e -> {
            context.disconnect(Component.translatable("quietus.networking.failed.skillTree", e.getMessage()));
            return null;
        });
    }

    public static void handleSkillTreeAdvancementsSync(SkillTreeAdvancementsUpdatePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            skillTree.syncAdvancements(packet.advancementIds(), Set.of(), true);
        })
        .exceptionally(e -> {
            context.disconnect(Component.translatable("quietus.networking.failed.skillTree", e.getMessage()));
            return null;
        });
    }

    public static void handleSkillTreeAdvancementsSync(SkillTreeAdvancementsGrantRevokePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.isGrant()) {
                skillTree.syncAdvancements(Set.of(packet.advancementId()), Set.of(), false);
            } else {
                skillTree.syncAdvancements(Set.of(), Set.of(packet.advancementId()), false);
            }
        })
        .exceptionally(e -> {
            context.disconnect(Component.translatable("quietus.networking.failed.skillTree", e.getMessage()));
            return null;
        });
    }

    public static ClientSkillTree getSkillTree() {
        return skillTree;
    }

    public static Map<Identifier, SkillCategory> getCategories() {
        return skillTree.getCategories();
    }

    public static void putScrollDataEntry(Identifier id, SkillTreeTab.TabScrollData data) {
        scrollData.put(id, data);
    }
    public static void putTabsOrderAndSelected(List<Identifier> id, Identifier selected) {
        tabsOrder = id;
        tabSelected = selected;
    }
    public static Map<Identifier,SkillTreeTab.TabScrollData> getScrollData() {
        Map<Identifier,SkillTreeTab.TabScrollData> out = new HashMap<>();
        out.putAll(scrollData);
        return out;
    }
    public static List<Identifier> getTabsOrder() {
        return tabsOrder;
    }
    public static Identifier getTabSelected() {
        return tabSelected;
    }

}
