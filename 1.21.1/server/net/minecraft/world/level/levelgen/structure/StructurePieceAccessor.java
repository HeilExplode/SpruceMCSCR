/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure;

import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public interface StructurePieceAccessor {
    public void addPiece(StructurePiece var1);

    @Nullable
    public StructurePiece findCollisionPiece(BoundingBox var1);
}

