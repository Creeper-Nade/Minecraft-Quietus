package com.quietus.client;

import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;

import static com.quietus.Quietus.MODID;

public class QuietusKeyBindings {
    public static final KeyMapping.Category TRANSLATION_KEY_CATEGORY_QUIETUS =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(MODID, "quietus"));

    public static final String TRANSLATION_KEY_SKILL_TREE = "key.quietus.skill_tree";
    public static final String TRANSLATION_KEY_GRAPPLING_HOOK = "key.quietus.grappling_hook";
    public static final Lazy<KeyMapping> SKILL_TREE_KEY = Lazy.of(() -> new KeyMapping(
        TRANSLATION_KEY_SKILL_TREE,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        TRANSLATION_KEY_CATEGORY_QUIETUS
    ));
    public static final Lazy<KeyMapping> GRAPPLING_HOOK_KEY = Lazy.of(() -> new KeyMapping(
        TRANSLATION_KEY_GRAPPLING_HOOK,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        TRANSLATION_KEY_CATEGORY_QUIETUS
    ));
}
