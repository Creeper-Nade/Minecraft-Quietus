package com.minecraftquietus.quietus.item.tool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.minecraftquietus.quietus.item.WeatheringCopperItem;
import com.minecraftquietus.quietus.item.WeatheringCopperItems;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CopperHoeItem extends WeatheringCopperItem {
    /**
     * @deprecated Forge: This map is patched out of vanilla code.
     * Listen to {@link net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent}
     * or override {@link net.neoforged.neoforge.common.extensions.IBlockExtension#getToolModifiedState(BlockState, UseOnContext, net.neoforged.neoforge.common.ItemAbility, boolean)}.
     */
    @Deprecated
    protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap(
        ImmutableMap.of(
            Blocks.GRASS_BLOCK,
            Pair.of(CopperHoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT_PATH,
            Pair.of(CopperHoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT,
            Pair.of(CopperHoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.COARSE_DIRT,
            Pair.of(CopperHoeItem::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.ROOTED_DIRT,
            Pair.of(p_238242_ -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))
        )
    );

    public CopperHoeItem(WeatheringCopperItems.WeatherState weatherState, Item.Properties properties) {
        super(weatherState, properties);
    }

    /**
     * Called when this item is used when targeting a Block
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState toolModifiedState = level.getBlockState(blockpos).getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.HOE_TILL, false);
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = toolModifiedState == null ? null : Pair.of(ctx -> true, changeIntoState(toolModifiedState));
        if (pair == null) {
            return InteractionResult.PASS;
        } else {
            Predicate<UseOnContext> predicate = pair.getFirst();
            Consumer<UseOnContext> consumer = pair.getSecond();
            if (predicate.test(context)) {
                Player player = context.getPlayer();
                level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    consumer.accept(context);
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    }
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState state) {
        return p_316061_ -> {
            p_316061_.getLevel().setBlock(p_316061_.getClickedPos(), state, 11);
            p_316061_.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, p_316061_.getClickedPos(), GameEvent.Context.of(p_316061_.getPlayer(), state));
        };
    }

    public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState state, ItemLike itemToDrop) {
        return p_316064_ -> {
            p_316064_.getLevel().setBlock(p_316064_.getClickedPos(), state, 11);
            p_316064_.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, p_316064_.getClickedPos(), GameEvent.Context.of(p_316064_.getPlayer(), state));
            Block.popResourceFromFace(p_316064_.getLevel(), p_316064_.getClickedPos(), p_316064_.getClickedFace(), new ItemStack(itemToDrop));
        };
    }

    public static boolean onlyIfAirAbove(UseOnContext context) {
        return context.getClickedFace() != Direction.DOWN && context.getLevel().getBlockState(context.getClickedPos().above()).isAir();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
    }
}
