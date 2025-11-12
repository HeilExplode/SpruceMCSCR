/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoeItem
extends DiggerItem {
    protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap((Map)ImmutableMap.of((Object)Blocks.GRASS_BLOCK, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.DIRT_PATH, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.DIRT, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.COARSE_DIRT, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.DIRT.defaultBlockState())), (Object)Blocks.ROOTED_DIRT, (Object)Pair.of(useOnContext -> true, HoeItem.changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))));

    public HoeItem(Tier tier, Item.Properties properties) {
        super(tier, BlockTags.MINEABLE_WITH_HOE, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLABLES.get(level.getBlockState(blockPos = useOnContext.getClickedPos()).getBlock());
        if (pair == null) {
            return InteractionResult.PASS;
        }
        Predicate predicate = (Predicate)pair.getFirst();
        Consumer consumer = (Consumer)pair.getSecond();
        if (predicate.test(useOnContext)) {
            Player player = useOnContext.getPlayer();
            level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide) {
                consumer.accept(useOnContext);
                if (player != null) {
                    useOnContext.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState blockState) {
        return useOnContext -> {
            useOnContext.getLevel().setBlock(useOnContext.getClickedPos(), blockState, 11);
            useOnContext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useOnContext.getClickedPos(), GameEvent.Context.of(useOnContext.getPlayer(), blockState));
        };
    }

    public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState blockState, ItemLike itemLike) {
        return useOnContext -> {
            useOnContext.getLevel().setBlock(useOnContext.getClickedPos(), blockState, 11);
            useOnContext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useOnContext.getClickedPos(), GameEvent.Context.of(useOnContext.getPlayer(), blockState));
            Block.popResourceFromFace(useOnContext.getLevel(), useOnContext.getClickedPos(), useOnContext.getClickedFace(), new ItemStack(itemLike));
        };
    }

    public static boolean onlyIfAirAbove(UseOnContext useOnContext) {
        return useOnContext.getClickedFace() != Direction.DOWN && useOnContext.getLevel().getBlockState(useOnContext.getClickedPos().above()).isAir();
    }
}

