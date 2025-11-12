/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 */
package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoneycombItem
extends Item
implements SignApplicator {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.WAXED_COPPER_BLOCK).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WAXED_EXPOSED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.WAXED_WEATHERED_COPPER).put((Object)Blocks.OXIDIZED_COPPER, (Object)Blocks.WAXED_OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.WAXED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER).put((Object)Blocks.OXIDIZED_CUT_COPPER, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.WAXED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.OXIDIZED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.WAXED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS).put((Object)Blocks.CHISELED_COPPER, (Object)Blocks.WAXED_CHISELED_COPPER).put((Object)Blocks.EXPOSED_CHISELED_COPPER, (Object)Blocks.WAXED_EXPOSED_CHISELED_COPPER).put((Object)Blocks.WEATHERED_CHISELED_COPPER, (Object)Blocks.WAXED_WEATHERED_CHISELED_COPPER).put((Object)Blocks.OXIDIZED_CHISELED_COPPER, (Object)Blocks.WAXED_OXIDIZED_CHISELED_COPPER).put((Object)Blocks.COPPER_DOOR, (Object)Blocks.WAXED_COPPER_DOOR).put((Object)Blocks.EXPOSED_COPPER_DOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_DOOR).put((Object)Blocks.WEATHERED_COPPER_DOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_DOOR).put((Object)Blocks.OXIDIZED_COPPER_DOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_DOOR).put((Object)Blocks.COPPER_TRAPDOOR, (Object)Blocks.WAXED_COPPER_TRAPDOOR).put((Object)Blocks.EXPOSED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR).put((Object)Blocks.WEATHERED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR).put((Object)Blocks.OXIDIZED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR).put((Object)Blocks.COPPER_GRATE, (Object)Blocks.WAXED_COPPER_GRATE).put((Object)Blocks.EXPOSED_COPPER_GRATE, (Object)Blocks.WAXED_EXPOSED_COPPER_GRATE).put((Object)Blocks.WEATHERED_COPPER_GRATE, (Object)Blocks.WAXED_WEATHERED_COPPER_GRATE).put((Object)Blocks.OXIDIZED_COPPER_GRATE, (Object)Blocks.WAXED_OXIDIZED_COPPER_GRATE).put((Object)Blocks.COPPER_BULB, (Object)Blocks.WAXED_COPPER_BULB).put((Object)Blocks.EXPOSED_COPPER_BULB, (Object)Blocks.WAXED_EXPOSED_COPPER_BULB).put((Object)Blocks.WEATHERED_COPPER_BULB, (Object)Blocks.WAXED_WEATHERED_COPPER_BULB).put((Object)Blocks.OXIDIZED_COPPER_BULB, (Object)Blocks.WAXED_OXIDIZED_COPPER_BULB).build());
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public HoneycombItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState2 = level.getBlockState(blockPos);
        return HoneycombItem.getWaxed(blockState2).map(blockState -> {
            Player player = useOnContext.getPlayer();
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }
            itemStack.shrink(1);
            level.setBlock(blockPos, (BlockState)blockState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState));
            level.levelEvent(player, 3003, blockPos, 0);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }).orElse(InteractionResult.PASS);
    }

    public static Optional<BlockState> getWaxed(BlockState blockState) {
        return Optional.ofNullable((Block)WAXABLES.get().get((Object)blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player) {
        if (signBlockEntity.setWaxed(true)) {
            level.levelEvent(null, 3003, signBlockEntity.getBlockPos(), 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canApplyToSign(SignText signText, Player player) {
        return true;
    }
}

