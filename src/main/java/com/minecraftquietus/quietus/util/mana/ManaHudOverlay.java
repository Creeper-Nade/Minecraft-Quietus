package com.minecraftquietus.quietus.util.mana;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ManaHudOverlay {
    private static final ResourceLocation MANA_ICONS =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/mana_icons.png");

    private static final int ICON_SIZE = 9;
    private static final int TEXTURE_WIDTH = 63;
    private static final int SLOTS_PER_ROW = 10;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (mc.player == null|| player.isCreative()) return;

        ManaComponent mana = mc.player.getData(ManaComponent.ATTACHMENT);
        int currentTick = player.tickCount;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position above hunger bar (matches vanilla layout)
        int xStart = screenWidth / 2 +10 ;
        int yPos = screenHeight - 49;


        renderContainers(gui, screenWidth, yPos,xStart, mana, currentTick);
        renderFills(gui, screenWidth, yPos, xStart,mana);

    }


    private static void renderContainers(GuiGraphics gui, int screenWidth,int yPos, int xStart,
                                         ManaComponent mana,int currentTick) {
        int totalSlots = mana.getTotalSlots();

        for(int slot = 0; slot < totalSlots; slot++) {
            int row = slot / SLOTS_PER_ROW;
            int col = slot % SLOTS_PER_ROW;

            // Calculate position with animation offset
            int x = xStart + col * 8;
            int y = yPos - row * 10;

            // Blink all containers when any slot completes
            boolean blink = mana.shouldBlinkContainers(currentTick);
            int texCol = blink ? 1 : 0; // 0=normal, 1=blinking container

            gui.blit(
                    net.minecraft.client.renderer.RenderType::guiTextured,
                    MANA_ICONS,
                    x, y,
                    texCol * ICON_SIZE, 0,
                    ICON_SIZE, ICON_SIZE,
                    TEXTURE_WIDTH, ICON_SIZE
            );
        }
    }

    private static void renderFills(GuiGraphics gui, int screenWidth,int yPos, int xStart,
                                    ManaComponent mana) {
        int totalSlots = mana.getTotalSlots();

        for(int slot = 0; slot < totalSlots; slot++) {
            int row = slot / SLOTS_PER_ROW;
            int col = slot % SLOTS_PER_ROW;

            int x = xStart + col * 8;
            int y = yPos - row * 10 ;

            // Calculate fill state
            int slotMana = Math.min(mana.getMana(), (slot+1)*4) - slot*4;
            if(slotMana <= 0) continue;

            // Select texture column based on fill amount
            int texCol = switch(slotMana) {
                case 4 -> 2; // Full
                case 3 -> 4; // 3/4
                case 2 -> 5; // 1/2
                case 1 -> 6; // 1/4
                default -> 0;
            };

            gui.blit(
                    net.minecraft.client.renderer.RenderType::guiTextured,
                    MANA_ICONS,
                    x, y,
                    texCol * ICON_SIZE, 0,
                    ICON_SIZE, ICON_SIZE,
                    TEXTURE_WIDTH, ICON_SIZE
            );
        }
    }
}
