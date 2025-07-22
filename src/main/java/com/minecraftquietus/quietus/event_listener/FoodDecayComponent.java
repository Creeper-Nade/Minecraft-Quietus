package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.quietus.item.component.CanDecay;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;


import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class FoodDecayComponent {
    

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        

        // 基准熟肉类设置 (已存在)
        modifyToDecayableMeat(event, Items.COOKED_BEEF, 512);
        modifyToDecayableMeat(event, Items.COOKED_MUTTON, 512);
        modifyToDecayableMeat(event, Items.COOKED_CHICKEN, 512);
        modifyToDecayableMeat(event, Items.COOKED_PORKCHOP, 512);
        modifyToDecayableMeat(event, Items.COOKED_RABBIT, 512);
        modifyToDecayableMeat(event, Items.COOKED_COD, 448);
        modifyToDecayableMeat(event, Items.COOKED_SALMON, 448);

        // ========== 新增食物设置 ==========
        
        // 生肉类食材 (低保鲜度)
        modifyToDecayableMeat(event, Items.BEEF, 256);
        modifyToDecayableMeat(event, Items.MUTTON, 256);
        modifyToDecayableMeat(event, Items.CHICKEN, 256);
        modifyToDecayableMeat(event, Items.PORKCHOP, 256);
        modifyToDecayableMeat(event, Items.RABBIT, 256);
        modifyToDecayableMeat(event, Items.RABBIT_FOOT, 256);
        modifyToDecayableMeat(event, Items.SPIDER_EYE, 256);
        modifyToDecayableMeat(event, Items.FERMENTED_SPIDER_EYE, 384);


        // 生鱼类食材 (比肉类更低)
        modifyToDecayableMeat(event, Items.COD, 192);
        modifyToDecayableMeat(event, Items.SALMON, 192);
        modifyToDecayableMeat(event, Items.TROPICAL_FISH, 192);
        modifyToDecayableMeat(event, Items.PUFFERFISH, 192);
        
        // 土豆类
        modifyToDecayablePlant(event, Items.POTATO, 192);
        modifyToDecayablePlant(event, Items.BAKED_POTATO, 384);
        modifyToDecayablePlant(event, Items.POISONOUS_POTATO, 192);
        
        // 面包类 (高保鲜度)
        modifyToDecayablePlant(event, Items.BREAD, 640);
        modifyToDecayablePlant(event, Items.PUMPKIN_PIE, 576);

        // 曲奇 (极高保鲜度)
        modifyToDecayablePlant(event, Items.COOKIE, 768);

        
        // 蛋糕
        event.modify(Items.CAKE, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(800).convertsInto(new ItemStack(QuietusItems.MOLD.get())).build())
        );
        
        // stew
        event.modify(Items.MUSHROOM_STEW, builder ->  // 蘑菇炖菜
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(QuietusItems.MOLD_BOWL.get())).build()).set(DataComponents.MAX_STACK_SIZE, 16)
        );
        event.modify(Items.BEETROOT_SOUP, builder ->  // 甜菜汤
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(QuietusItems.MOLD_BOWL.get())).build()).set(DataComponents.MAX_STACK_SIZE, 16)
        );
        event.modify(Items.RABBIT_STEW, builder ->    // 兔肉煲
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(QuietusItems.MOLD_BOWL.get())).build()).set(DataComponents.MAX_STACK_SIZE, 16)
        );
        event.modify(Items.SUSPICIOUS_STEW, builder ->    // suspicious stew
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(512).convertsInto(new ItemStack(QuietusItems.MOLD_BOWL.get())).build()).set(DataComponents.MAX_STACK_SIZE, 16)
        );

        // milk bucket has more than one decaying result. This is changed in WeatheringHandler via DecayEvent. 
        // default mold bucket. 50% to be otherwise.
        event.modify(Items.MILK_BUCKET, builder ->  
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(384).convertsInto(new ItemStack(QuietusItems.MOLD_BUCKET.get())).build()).set(DataComponents.MAX_STACK_SIZE, 16)
        );
        
        // golden foods
        modifyToDecayablePlant(event, Items.GOLDEN_CARROT, Integer.MAX_VALUE);
        modifyToDecayablePlant(event, Items.GOLDEN_APPLE, Integer.MAX_VALUE);
        modifyToDecayablePlant(event, Items.ENCHANTED_GOLDEN_APPLE, Integer.MAX_VALUE);
        
        // 其他植物类食材
        modifyToDecayablePlant(event, Items.CARROT, 320);
        modifyToDecayablePlant(event, Items.BEETROOT, 320);
        modifyToDecayablePlant(event, Items.APPLE, 384);
        modifyToDecayablePlant(event, Items.MELON, 384);
        modifyToDecayablePlant(event, Items.PUMPKIN, 384);
        modifyToDecayablePlant(event, Items.MELON_SLICE, 256);
        modifyToDecayablePlant(event, Items.GLISTERING_MELON_SLICE, 256);
        modifyToDecayablePlant(event, Items.SWEET_BERRIES, 192);
        modifyToDecayablePlant(event, Items.GLOW_BERRIES, 192);
        modifyToDecayablePlant(event, Items.WHEAT, Integer.MAX_VALUE);
        modifyToDecayablePlant(event, Items.DRIED_KELP, Integer.MAX_VALUE);

        // seeds
        modifyToDecayablePlant(event, Items.WHEAT_SEEDS, 1536);
        modifyToDecayablePlant(event, Items.BEETROOT_SEEDS, 1536);
        modifyToDecayablePlant(event, Items.MELON_SEEDS, 1536);
        modifyToDecayablePlant(event, Items.PUMPKIN_SEEDS, 1536);
        modifyToDecayablePlant(event, Items.TORCHFLOWER_SEEDS, 1536);
    }

    private static void modifyToDecayableMeat(ModifyDefaultComponentsEvent event, Item item, int maxDecay) {
        event.modify(item, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(maxDecay).convertsInto(new ItemStack(Items.ROTTEN_FLESH)).build())
        );
    }

    private static void modifyToDecayablePlant(ModifyDefaultComponentsEvent event, Item item, int maxDecay) {
        event.modify(item, builder ->
            builder.set(QuietusComponents.CAN_DECAY.get(), CanDecay.builder().maxDecay(maxDecay).convertsInto(new ItemStack(QuietusItems.MOLD.get())).build())
        );
    }

}
