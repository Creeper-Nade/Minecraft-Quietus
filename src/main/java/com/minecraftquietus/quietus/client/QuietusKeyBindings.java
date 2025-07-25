package com.minecraftquietus.quietus.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;

public class QuietusKeyBindings {
    public static final String TRANSLATION_KEY_CATEGORY_QUIETUS = "key.category.quietus";

    public static final String TRANSLATION_KEY_SKILL_TREE = "key.quietus.skill_tree";
    public static final Lazy<KeyMapping> SKILL_TREE_KEY = Lazy.of(() -> new KeyMapping(
        TRANSLATION_KEY_SKILL_TREE,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        TRANSLATION_KEY_CATEGORY_QUIETUS
    ));
}
