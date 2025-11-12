/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class AxeItem
extends DiggerItem {
    protected static final Map<Block, Block> STRIPPABLES = new ImmutableMap.Builder().put((Object)Blocks.OAK_WOOD, (Object)Blocks.STRIPPED_OAK_WOOD).put((Object)Blocks.OAK_LOG, (Object)Blocks.STRIPPED_OAK_LOG).put((Object)Blocks.DARK_OAK_WOOD, (Object)Blocks.STRIPPED_DARK_OAK_WOOD).put((Object)Blocks.DARK_OAK_LOG, (Object)Blocks.STRIPPED_DARK_OAK_LOG).put((Object)Blocks.ACACIA_WOOD, (Object)Blocks.STRIPPED_ACACIA_WOOD).put((Object)Blocks.ACACIA_LOG, (Object)Blocks.STRIPPED_ACACIA_LOG).put((Object)Blocks.CHERRY_WOOD, (Object)Blocks.STRIPPED_CHERRY_WOOD).put((Object)Blocks.CHERRY_LOG, (Object)Blocks.STRIPPED_CHERRY_LOG).put((Object)Blocks.BIRCH_WOOD, (Object)Blocks.STRIPPED_BIRCH_WOOD).put((Object)Blocks.BIRCH_LOG, (Object)Blocks.STRIPPED_BIRCH_LOG).put((Object)Blocks.JUNGLE_WOOD, (Object)Blocks.STRIPPED_JUNGLE_WOOD).put((Object)Blocks.JUNGLE_LOG, (Object)Blocks.STRIPPED_JUNGLE_LOG).put((Object)Blocks.SPRUCE_WOOD, (Object)Blocks.STRIPPED_SPRUCE_WOOD).put((Object)Blocks.SPRUCE_LOG, (Object)Blocks.STRIPPED_SPRUCE_LOG).put((Object)Blocks.WARPED_STEM, (Object)Blocks.STRIPPED_WARPED_STEM).put((Object)Blocks.WARPED_HYPHAE, (Object)Blocks.STRIPPED_WARPED_HYPHAE).put((Object)Blocks.CRIMSON_STEM, (Object)Blocks.STRIPPED_CRIMSON_STEM).put((Object)Blocks.CRIMSON_HYPHAE, (Object)Blocks.STRIPPED_CRIMSON_HYPHAE).put((Object)Blocks.MANGROVE_WOOD, (Object)Blocks.STRIPPED_MANGROVE_WOOD).put((Object)Blocks.MANGROVE_LOG, (Object)Blocks.STRIPPED_MANGROVE_LOG).put((Object)Blocks.BAMBOO_BLOCK, (Object)Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public AxeItem(Tier tier, Item.Properties properties) {
        super(tier, BlockTags.MINEABLE_WITH_AXE, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();
        if (AxeItem.playerHasShieldUseIntent(useOnContext)) {
            return InteractionResult.PASS;
        }
        Optional<BlockState> optional = this.evaluateNewBlockState(level, blockPos, player, level.getBlockState(blockPos));
        if (optional.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
        }
        level.setBlock(blockPos, optional.get(), 11);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, optional.get()));
        if (player != null) {
            itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean playerHasShieldUseIntent(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        return useOnContext.getHand().equals((Object)InteractionHand.MAIN_HAND) && player.getOffhandItem().is(Items.SHIELD) && !player.isSecondaryUseActive();
    }

    private Optional<BlockState> evaluateNewBlockState(Level level, BlockPos blockPos, @Nullable Player player, BlockState blockState) {
        Optional<BlockState> optional = this.getStripped(blockState);
        if (optional.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            return optional;
        }
        Optional<BlockState> optional2 = WeatheringCopper.getPrevious(blockState);
        if (optional2.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3005, blockPos, 0);
            return optional2;
        }
        Optional<BlockState> optional3 = Optional.ofNullable((Block)HoneycombItem.WAX_OFF_BY_BLOCK.get().get((Object)blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
        if (optional3.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3004, blockPos, 0);
            return optional3;
        }
        return Optional.empty();
    }

    private Optional<BlockState> getStripped(BlockState blockState) {
        return Optional.ofNullable(STRIPPABLES.get(blockState.getBlock())).map(block -> (BlockState)block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockState.getValue(RotatedPillarBlock.AXIS)));
    }
}

