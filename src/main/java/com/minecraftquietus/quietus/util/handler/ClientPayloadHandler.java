package com.minecraftquietus.quietus.util.handler;

import com.minecraftquietus.quietus.packet.ManaPack;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {
    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    private static int maxMana;
    private static int mana;
    private static boolean Mana_Speed_Charging;


    public static void ManaHandler(final ManaPack Mpack, final IPayloadContext context) {
        // Do something with the data, on the network thread
        // 在network 线程中对data数据做一些处理的话，代码写在这里。

        // Do something with the data, on the main thread
        // 在Main 线程里面做一些什么，代码西在下面
        context.enqueueWork(() -> {
                    // 写在这里
                    //System.out.println(Mpack.Mana());
                    maxMana = Mpack.MaxMana();
                    mana = Mpack.Mana();
                    Mana_Speed_Charging=Mpack.fast_charging();
                })
                .exceptionally(e -> {
                    // 处理异常
                    // Handle exception
                    context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
                    return null;
                });
    }
    public int GetMaxManaFromPack()
    {
        return maxMana;
    }
    public boolean GetManaChargeStatus(){return Mana_Speed_Charging;}
    public int GetManaFromPack()
    {
        return mana;
    }
}
