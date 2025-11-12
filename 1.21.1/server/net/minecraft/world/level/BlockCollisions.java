/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCollisions<T>
extends AbstractIterator<T> {
    private final AABB box;
    private final CollisionContext context;
    private final Cursor3D cursor;
    private final BlockPos.MutableBlockPos pos;
    private final VoxelShape entityShape;
    private final CollisionGetter collisionGetter;
    private final boolean onlySuffocatingBlocks;
    @Nullable
    private BlockGetter cachedBlockGetter;
    private long cachedBlockGetterPos;
    private final BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    public BlockCollisions(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB, boolean bl, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> biFunction) {
        this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        this.pos = new BlockPos.MutableBlockPos();
        this.entityShape = Shapes.create(aABB);
        this.collisionGetter = collisionGetter;
        this.box = aABB;
        this.onlySuffocatingBlocks = bl;
        this.resultProvider = biFunction;
        int n = Mth.floor(aABB.minX - 1.0E-7) - 1;
        int n2 = Mth.floor(aABB.maxX + 1.0E-7) + 1;
        int n3 = Mth.floor(aABB.minY - 1.0E-7) - 1;
        int n4 = Mth.floor(aABB.maxY + 1.0E-7) + 1;
        int n5 = Mth.floor(aABB.minZ - 1.0E-7) - 1;
        int n6 = Mth.floor(aABB.maxZ + 1.0E-7) + 1;
        this.cursor = new Cursor3D(n, n3, n5, n2, n4, n6);
    }

    @Nullable
    private BlockGetter getChunk(int n, int n2) {
        BlockGetter blockGetter;
        int n3 = SectionPos.blockToSectionCoord(n);
        int n4 = SectionPos.blockToSectionCoord(n2);
        long l = ChunkPos.asLong(n3, n4);
        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == l) {
            return this.cachedBlockGetter;
        }
        this.cachedBlockGetter = blockGetter = this.collisionGetter.getChunkForCollisions(n3, n4);
        this.cachedBlockGetterPos = l;
        return blockGetter;
    }

    protected T computeNext() {
        while (this.cursor.advance()) {
            BlockGetter blockGetter;
            int n = this.cursor.nextX();
            int n2 = this.cursor.nextY();
            int n3 = this.cursor.nextZ();
            int n4 = this.cursor.getNextType();
            if (n4 == 3 || (blockGetter = this.getChunk(n, n3)) == null) continue;
            this.pos.set(n, n2, n3);
            BlockState blockState = blockGetter.getBlockState(this.pos);
            if (this.onlySuffocatingBlocks && !blockState.isSuffocating(blockGetter, this.pos) || n4 == 1 && !blockState.hasLargeCollisionShape() || n4 == 2 && !blockState.is(Blocks.MOVING_PISTON)) continue;
            VoxelShape voxelShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
            if (voxelShape == Shapes.block()) {
                if (!this.box.intersects(n, n2, n3, (double)n + 1.0, (double)n2 + 1.0, (double)n3 + 1.0)) continue;
                return this.resultProvider.apply(this.pos, voxelShape.move(n, n2, n3));
            }
            VoxelShape voxelShape2 = voxelShape.move(n, n2, n3);
            if (voxelShape2.isEmpty() || !Shapes.joinIsNotEmpty(voxelShape2, this.entityShape, BooleanOp.AND)) continue;
            return this.resultProvider.apply(this.pos, voxelShape2);
        }
        return (T)this.endOfData();
    }
}

