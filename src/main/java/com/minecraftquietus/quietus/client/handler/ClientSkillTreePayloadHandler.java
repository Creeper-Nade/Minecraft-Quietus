package com.minecraftquietus.quietus.client.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.client.screens.skill_tree.SkillTreeScreen;
import com.minecraftquietus.quietus.skilltree.SkillCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class ClientSkillTreePayloadHandler {
    private static Minecraft minecraft = Minecraft.getInstance();

    private static ClientSkillTree skillTree = new ClientSkillTree(minecraft);
    
    // no use yet... just do Minecraft.getInstance() to get this running minecraft instance
    public static void initialize(Minecraft a) {
        minecraft = a;
        skillTree = new ClientSkillTree(a);
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

    public static ClientSkillTree getSkillTree() {
        return skillTree;
    }

    public static Map<ResourceLocation, SkillCategory> getCategories() {
        return skillTree.getCategories();
    }
}
