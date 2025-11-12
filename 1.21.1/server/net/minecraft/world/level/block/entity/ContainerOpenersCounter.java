/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;
    private double maxInteractionRange;

    protected abstract void onOpen(Level var1, BlockPos var2, BlockState var3);

    protected abstract void onClose(Level var1, BlockPos var2, BlockState var3);

    protected abstract void openerCountChanged(Level var1, BlockPos var2, BlockState var3, int var4, int var5);

    protected abstract boolean isOwnContainer(Player var1);

    public void incrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        int n;
        if ((n = this.openCount++) == 0) {
            this.onOpen(level, blockPos, blockState);
            level.gameEvent((Entity)player, GameEvent.CONTAINER_OPEN, blockPos);
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
        this.openerCountChanged(level, blockPos, blockState, n, this.openCount);
        this.maxInteractionRange = Math.max(player.blockInteractionRange(), this.maxInteractionRange);
    }

    public void decrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        int n = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(level, blockPos, blockState);
            level.gameEvent((Entity)player, GameEvent.CONTAINER_CLOSE, blockPos);
            this.maxInteractionRange = 0.0;
        }
        this.openerCountChanged(level, blockPos, blockState, n, this.openCount);
    }

    private List<Player> getPlayersWithContainerOpen(Level level, BlockPos blockPos) {
        double d = this.maxInteractionRange + 4.0;
        AABB aABB = new AABB(blockPos).inflate(d);
        return level.getEntities(EntityTypeTest.forClass(Player.class), aABB, this::isOwnContainer);
    }

    public void recheckOpeners(Level level, BlockPos blockPos, BlockState blockState) {
        List<Player> list = this.getPlayersWithContainerOpen(level, blockPos);
        this.maxInteractionRange = 0.0;
        for (Player player : list) {
            this.maxInteractionRange = Math.max(player.blockInteractionRange(), this.maxInteractionRange);
        }
        int n = this.openCount;
        int n2 = list.size();
        if (n != n2) {
            boolean bl;
            boolean bl2 = n2 != 0;
            boolean bl3 = bl = n != 0;
            if (bl2 && !bl) {
                this.onOpen(level, blockPos, blockState);
                level.gameEvent(null, GameEvent.CONTAINER_OPEN, blockPos);
            } else if (!bl2) {
                this.onClose(level, blockPos, blockState);
                level.gameEvent(null, GameEvent.CONTAINER_CLOSE, blockPos);
            }
            this.openCount = n2;
        }
        this.openerCountChanged(level, blockPos, blockState, n, n2);
        if (n2 > 0) {
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(Level level, BlockPos blockPos, BlockState blockState) {
        level.scheduleTick(blockPos, blockState.getBlock(), 5);
    }
}

