/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class StructurePiecesBuilder
implements StructurePieceAccessor {
    private final List<StructurePiece> pieces = Lists.newArrayList();

    @Override
    public void addPiece(StructurePiece structurePiece) {
        this.pieces.add(structurePiece);
    }

    @Override
    @Nullable
    public StructurePiece findCollisionPiece(BoundingBox boundingBox) {
        return StructurePiece.findCollisionPiece(this.pieces, boundingBox);
    }

    @Deprecated
    public void offsetPiecesVertically(int n) {
        for (StructurePiece structurePiece : this.pieces) {
            structurePiece.move(0, n, 0);
        }
    }

    @Deprecated
    public int moveBelowSeaLevel(int n, int n2, RandomSource randomSource, int n3) {
        int n4 = n - n3;
        BoundingBox boundingBox = this.getBoundingBox();
        int n5 = boundingBox.getYSpan() + n2 + 1;
        if (n5 < n4) {
            n5 += randomSource.nextInt(n4 - n5);
        }
        int n6 = n5 - boundingBox.maxY();
        this.offsetPiecesVertically(n6);
        return n6;
    }

    public void moveInsideHeights(RandomSource randomSource, int n, int n2) {
        BoundingBox boundingBox = this.getBoundingBox();
        int n3 = n2 - n + 1 - boundingBox.getYSpan();
        int n4 = n3 > 1 ? n + randomSource.nextInt(n3) : n;
        int n5 = n4 - boundingBox.minY();
        this.offsetPiecesVertically(n5);
    }

    public PiecesContainer build() {
        return new PiecesContainer(this.pieces);
    }

    public void clear() {
        this.pieces.clear();
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public BoundingBox getBoundingBox() {
        return StructurePiece.createBoundingBox(this.pieces.stream());
    }
}

