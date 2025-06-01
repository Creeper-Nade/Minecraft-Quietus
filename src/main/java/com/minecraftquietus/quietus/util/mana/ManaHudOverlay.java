package com.minecraftquietus.quietus.util.mana;

import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Random;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ManaHudOverlay {
    private static final ResourceLocation MANA_ICONS =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/mana_icons.png");

    private static final int ICON_SIZE = 9;
    private static final int TEXTURE_WIDTH = 63;
    private static final int SLOTS_PER_ROW = 10;

    public static int Display_Mana=0;
    private static int prev_mana=0;
    private static long globalBlinkEndTime=0;
    public static int Display_MaxMana=20;
    private static int slots;
    public static int row_space;
    private static final int SHAKE_THRESHOLD = 20; // 20% of max mana
    private static int lastUpdateTime = 0;
    private static int lastWaveStartTime = 0;
    private static int[] Jitter_offsets = new int[0] ;
    private static int currentAnimatingSlot = 0;
    //private static int currentTick;
    //private static Player Hudplayer;
    //private static int slots;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        globalBlinkEndTime=0;
        lastUpdateTime=0;
        lastWaveStartTime=0;
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        globalBlinkEndTime=0;
        lastUpdateTime=0;
        lastWaveStartTime=0;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if(event.isCanceled()) globalBlinkEndTime = 0;
        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        int currentTick= player.tickCount;

        if (mc.options.hideGui || mc.player == null|| player.isCreative() || player.isSpectator()) return;

        //ManaComponent mana = player.getData(QuietusAttachments.MANA_ATTACHMENT);
        //int currentTick = player.tickCount;
        Display_Mana= ClientPayloadHandler.getInstance().GetManaFromPack();
        Display_MaxMana= ClientPayloadHandler.getInstance().GetMaxManaFromPack();
        boolean is_speed_charging;
        is_speed_charging=ClientPayloadHandler.getInstance().GetManaChargeStatus();

        float manaPercent = (Display_Mana * 100f) / Display_MaxMana;
        boolean shouldShake = manaPercent < SHAKE_THRESHOLD;

        //if(prev_mana==0) prev_mana=Display_Mana;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position above hunger bar (matches vanilla layout)
        int xStart = screenWidth / 2 +10 ;
        int yPos = screenHeight - 49;

        int totalSlots = getTotalSlots();
        row_space= Math.clamp(totalSlots/SLOTS_PER_ROW, 0, 7);

        if(Jitter_offsets.length!=Display_MaxMana)
        {
            Jitter_offsets =new int[Display_MaxMana];
        }


        renderSlots(gui, player, yPos,xStart, currentTick,totalSlots,row_space,shouldShake, is_speed_charging);
        //renderFills(gui, screenWidth, yPos, xStart,mana,totalSlots,row_space);

    }





    private static void renderSlots(GuiGraphics gui, Player player,int yPos, int xStart,int currentTick, int totalSlots, int row_space,boolean shouldShake,boolean is_speed_charging) {

        if (Display_Mana<prev_mana || (Display_Mana==Display_MaxMana && prev_mana < Display_Mana)) {
            blinkContainers(4, player);
        }


        for(int slot = totalSlots-1; slot >=0; slot--) {
            int row = slot / SLOTS_PER_ROW;
            int col = slot % SLOTS_PER_ROW;

            // Calculate position with animation offset
            // Apply shake effect to each icon if mana is low

            int x = xStart + col * 8;
            int y = yPos - row * (10-row_space);
            if(shouldShake) y-= Jitter_offsets[slot];
            if(is_speed_charging && CheckWaveCD(currentTick)) y-= getWaveAnimOffset(slot,currentTick);

            
            boolean blink = shouldBlinkContainers(currentTick);

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
        //update icon anim
        if(currentTick-lastUpdateTime>=1)
        {
            lastUpdateTime=currentTick;
            if(shouldShake) SetJitterPosition(totalSlots);
            if(is_speed_charging)
            {
                if(CheckWaveCD(currentTick))
                updateWaveAnimation(currentTick, totalSlots);
            }
            else
            {
                currentAnimatingSlot=0;
                lastWaveStartTime=0;
            }


        }
        prev_mana= Display_Mana;
    }

    public static void blinkContainers(int ticks, Player player) {
        globalBlinkEndTime = player.tickCount + ticks;
    }

    public static boolean shouldBlinkContainers(int currentTick) {
        return currentTick < globalBlinkEndTime;
    }

    public static int getTotalSlots() {
    }

    public static int getRowCount() {

        return (int) Math.ceil((double)slots / 10);
    }

    private static void SetJitterPosition(int totalSlots) {
        Random random = new Random();
        for (int i = totalSlots - 1; i >= 0; i--) {
            Jitter_offsets[i] = random.nextInt(-1, 1); // -2 to +2
        }
    }

    private static void updateWaveAnimation(int currentTick, int totalSlots) {
                currentAnimatingSlot++;

                // Reset when wave completes
                if (currentAnimatingSlot >= totalSlots) {
                    currentAnimatingSlot=0;
                    lastWaveStartTime=currentTick;
                }
    }
    private static boolean CheckWaveCD(int currentTick)
    {
        return currentTick-lastWaveStartTime>20;
    }


    private static int getWaveAnimOffset(int slot, int currentTick) {
        if (slot != currentAnimatingSlot) return 0;
        return 2;
    }




}
