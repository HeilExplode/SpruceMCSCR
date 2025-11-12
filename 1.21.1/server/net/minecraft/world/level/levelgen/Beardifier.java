/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier
implements DensityFunctions.BeardifierOrMarker {
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], fArray -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    fArray[i * 24 * 24 + j * 24 + k] = (float)Beardifier.computeBeardContribution(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectListIterator<Rigid> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos) {
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        ObjectArrayList objectArrayList = new ObjectArrayList(10);
        ObjectArrayList objectArrayList2 = new ObjectArrayList(32);
        structureManager.startsForStructure(chunkPos, structure -> structure.terrainAdaptation() != TerrainAdjustment.NONE).forEach(arg_0 -> Beardifier.lambda$forStructuresInChunk$2(chunkPos, (ObjectList)objectArrayList, n, n2, (ObjectList)objectArrayList2, arg_0));
        return new Beardifier((ObjectListIterator<Rigid>)objectArrayList.iterator(), (ObjectListIterator<JigsawJunction>)objectArrayList2.iterator());
    }

    @VisibleForTesting
    public Beardifier(ObjectListIterator<Rigid> objectListIterator, ObjectListIterator<JigsawJunction> objectListIterator2) {
        this.pieceIterator = objectListIterator;
        this.junctionIterator = objectListIterator2;
    }

    @Override
    public double compute(DensityFunction.FunctionContext functionContext) {
        int n;
        int n2;
        Object object;
        int n3 = functionContext.blockX();
        int n4 = functionContext.blockY();
        int n5 = functionContext.blockZ();
        double d = 0.0;
        while (this.pieceIterator.hasNext()) {
            object = (Rigid)this.pieceIterator.next();
            BoundingBox boundingBox = ((Rigid)object).box();
            n2 = ((Rigid)object).groundLevelDelta();
            n = Math.max(0, Math.max(boundingBox.minX() - n3, n3 - boundingBox.maxX()));
            int n6 = Math.max(0, Math.max(boundingBox.minZ() - n5, n5 - boundingBox.maxZ()));
            int n7 = boundingBox.minY() + n2;
            int n8 = n4 - n7;
            int n9 = switch (((Rigid)object).terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case TerrainAdjustment.NONE -> 0;
                case TerrainAdjustment.BURY, TerrainAdjustment.BEARD_THIN -> n8;
                case TerrainAdjustment.BEARD_BOX -> Math.max(0, Math.max(n7 - n4, n4 - boundingBox.maxY()));
                case TerrainAdjustment.ENCAPSULATE -> Math.max(0, Math.max(boundingBox.minY() - n4, n4 - boundingBox.maxY()));
            };
            d += (switch (((Rigid)object).terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case TerrainAdjustment.NONE -> 0.0;
                case TerrainAdjustment.BURY -> Beardifier.getBuryContribution(n, (double)n9 / 2.0, n6);
                case TerrainAdjustment.BEARD_THIN, TerrainAdjustment.BEARD_BOX -> Beardifier.getBeardContribution(n, n9, n6, n8) * 0.8;
                case TerrainAdjustment.ENCAPSULATE -> Beardifier.getBuryContribution((double)n / 2.0, (double)n9 / 2.0, (double)n6 / 2.0) * 0.8;
            });
        }
        this.pieceIterator.back(Integer.MAX_VALUE);
        while (this.junctionIterator.hasNext()) {
            object = (JigsawJunction)this.junctionIterator.next();
            int n10 = n3 - ((JigsawJunction)object).getSourceX();
            n2 = n4 - ((JigsawJunction)object).getSourceGroundY();
            n = n5 - ((JigsawJunction)object).getSourceZ();
            d += Beardifier.getBeardContribution(n10, n2, n, n2) * 0.4;
        }
        this.junctionIterator.back(Integer.MAX_VALUE);
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(double d, double d2, double d3) {
        double d4 = Mth.length(d, d2, d3);
        return Mth.clampedMap(d4, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int n, int n2, int n3, int n4) {
        int n5 = n + 12;
        int n6 = n2 + 12;
        int n7 = n3 + 12;
        if (!(Beardifier.isInKernelRange(n5) && Beardifier.isInKernelRange(n6) && Beardifier.isInKernelRange(n7))) {
            return 0.0;
        }
        double d = (double)n4 + 0.5;
        double d2 = Mth.lengthSquared((double)n, d, (double)n3);
        double d3 = -d * Mth.fastInvSqrt(d2 / 2.0) / 2.0;
        return d3 * (double)BEARD_KERNEL[n7 * 24 * 24 + n5 * 24 + n6];
    }

    private static boolean isInKernelRange(int n) {
        return n >= 0 && n < 24;
    }

    private static double computeBeardContribution(int n, int n2, int n3) {
        return Beardifier.computeBeardContribution(n, (double)n2 + 0.5, n3);
    }

    private static double computeBeardContribution(int n, double d, int n2) {
        double d2 = Mth.lengthSquared((double)n, d, (double)n2);
        double d3 = Math.pow(Math.E, -d2 / 16.0);
        return d3;
    }

    private static /* synthetic */ void lambda$forStructuresInChunk$2(ChunkPos chunkPos, ObjectList objectList, int n, int n2, ObjectList objectList2, StructureStart structureStart) {
        TerrainAdjustment terrainAdjustment = structureStart.getStructure().terrainAdaptation();
        for (StructurePiece structurePiece : structureStart.getPieces()) {
            if (!structurePiece.isCloseToChunk(chunkPos, 12)) continue;
            if (structurePiece instanceof PoolElementStructurePiece) {
                PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
                if (projection == StructureTemplatePool.Projection.RIGID) {
                    objectList.add((Object)new Rigid(poolElementStructurePiece.getBoundingBox(), terrainAdjustment, poolElementStructurePiece.getGroundLevelDelta()));
                }
                for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
                    int n3 = jigsawJunction.getSourceX();
                    int n4 = jigsawJunction.getSourceZ();
                    if (n3 <= n - 12 || n4 <= n2 - 12 || n3 >= n + 15 + 12 || n4 >= n2 + 15 + 12) continue;
                    objectList2.add((Object)jigsawJunction);
                }
                continue;
            }
            objectList.add((Object)new Rigid(structurePiece.getBoundingBox(), terrainAdjustment, 0));
        }
    }

    @VisibleForTesting
    public record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
    }
}

