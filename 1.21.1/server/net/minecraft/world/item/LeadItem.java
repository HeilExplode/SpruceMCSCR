/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class LeadItem
extends Item {
    public LeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(BlockTags.FENCES)) {
            Player player = useOnContext.getPlayer();
            if (!level.isClientSide && player != null) {
                LeadItem.bindPlayerMobs(player, level, blockPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
        LeashFenceKnotEntity leashFenceKnotEntity = null;
        List<Leashable> list = LeadItem.leashableInArea(level, blockPos, leashable -> leashable.getLeashHolder() == player);
        for (Leashable leashable2 : list) {
            if (leashFenceKnotEntity == null) {
                leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
                leashFenceKnotEntity.playPlacementSound();
            }
            leashable2.setLeashedTo(leashFenceKnotEntity, true);
        }
        if (!list.isEmpty()) {
            level.gameEvent(GameEvent.BLOCK_ATTACH, blockPos, GameEvent.Context.of(player));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static List<Leashable> leashableInArea(Level level, BlockPos blockPos, Predicate<Leashable> predicate) {
        double d = 7.0;
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        AABB aABB = new AABB((double)n - 7.0, (double)n2 - 7.0, (double)n3 - 7.0, (double)n + 7.0, (double)n2 + 7.0, (double)n3 + 7.0);
        return level.getEntitiesOfClass(Entity.class, aABB, entity -> {
            Leashable leashable;
            return entity instanceof Leashable && predicate.test(leashable = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }
}

