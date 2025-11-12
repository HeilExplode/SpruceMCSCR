/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.navigation;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation
extends GroundPathNavigation {
    @Nullable
    private BlockPos pathToPosition;

    public WallClimberNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public Path createPath(BlockPos blockPos, int n) {
        this.pathToPosition = blockPos;
        return super.createPath(blockPos, n);
    }

    @Override
    public Path createPath(Entity entity, int n) {
        this.pathToPosition = entity.blockPosition();
        return super.createPath(entity, n);
    }

    @Override
    public boolean moveTo(Entity entity, double d) {
        Path path = this.createPath(entity, 0);
        if (path != null) {
            return this.moveTo(path, d);
        }
        this.pathToPosition = entity.blockPosition();
        this.speedModifier = d;
        return true;
    }

    @Override
    public void tick() {
        if (this.isDone()) {
            if (this.pathToPosition != null) {
                if (this.pathToPosition.closerToCenterThan(this.mob.position(), this.mob.getBbWidth()) || this.mob.getY() > (double)this.pathToPosition.getY() && BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ()).closerToCenterThan(this.mob.position(), this.mob.getBbWidth())) {
                    this.pathToPosition = null;
                } else {
                    this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
                }
            }
            return;
        }
        super.tick();
    }
}

