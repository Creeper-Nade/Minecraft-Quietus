package com.minecraftquietus.quietus.client.screen;

import static com.minecraftquietus.quietus.Quietus.MODID;

import com.minecraftquietus.quietus.client.QuietusKeyBindings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeScreen extends Screen {
    private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/window.png");

    private static final Component TITLE = Component.translatable("gui.skill_tree");


    public SkillTreeScreen() {
        super(TITLE);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (QuietusKeyBindings.SKILL_TREE_KEY.get().matches(keyCode, scanCode)
          || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
