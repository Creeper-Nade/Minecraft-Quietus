package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.event.QuietusCommonEvents;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ManaHudOverlay {
    private static final ResourceLocation MANA_ICONS =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/mana_icons.png");

    private static final int ICON_SIZE = 9;
    private static final int TEXTURE_WIDTH = 63;
    private static final int SLOTS_PER_ROW = 10;

    public static int Display_Mana=0;
    public static int Display_MaxMana=20;
    public static int row_space;
    //private static int currentTick;
    //private static Player Hudplayer;
    //private static int slots;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Player player = QuietusCommonEvents.QuietusServerPlayer;
        int currentTick= player.tickCount;

        if (mc.options.hideGui || mc.player == null|| player.isCreative()) return;

        ManaComponent mana = player.getData(QuietusAttachments.MANA_ATTACHMENT);
        //int currentTick = player.tickCount;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position above hunger bar (matches vanilla layout)
        int xStart = screenWidth / 2 +10 ;
        int yPos = screenHeight - 49;

        int totalSlots = mana.getTotalSlots(Display_MaxMana);
        row_space= Math.clamp(totalSlots/SLOTS_PER_ROW, 0, 7);


        renderSlots(gui, screenWidth, yPos,xStart, mana, currentTick,totalSlots,row_space);
        //renderFills(gui, screenWidth, yPos, xStart,mana,totalSlots,row_space);

    }





    private static void renderSlots(GuiGraphics gui, int screenWidth,int yPos, int xStart,
                                         ManaComponent mana,int currentTick, int totalSlots, int row_space) {

        for(int slot = totalSlots-1; slot >=0; slot--) {
            int row = slot / SLOTS_PER_ROW;
            int col = slot % SLOTS_PER_ROW;

            // Calculate position with animation offset
            int x = xStart + col * 8;
            int y = yPos - row * (10-row_space);

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

            // Calculate fill state
            int slotMana = Math.min(Display_Mana, (slot+1)*4) - slot*4;
            if(slotMana <= 0) continue;

            // Select texture column based on fill amount
            int FilltexCol = switch(slotMana) {
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
                    FilltexCol * ICON_SIZE, 0,
                    ICON_SIZE, ICON_SIZE,
                    TEXTURE_WIDTH, ICON_SIZE
            );
        }
    }

    private static void renderFills(GuiGraphics gui, int screenWidth,int yPos, int xStart,
                                    ManaComponent mana,int totalSlots, int row_space) {


        for(int slot = 0; slot < totalSlots; slot++) {
            int row = slot / SLOTS_PER_ROW;
            int col = slot % SLOTS_PER_ROW;

            int x = xStart + col * 8;
            int y = yPos - row * (10-row_space) ;

            // Calculate fill state
            int slotMana = Math.min(Display_Mana, (slot+1)*4) - slot*4;
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
