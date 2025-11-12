/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

public interface ChunkIOErrorReporter {
    public void reportChunkLoadFailure(Throwable var1, RegionStorageInfo var2, ChunkPos var3);

    public void reportChunkSaveFailure(Throwable var1, RegionStorageInfo var2, ChunkPos var3);

    public static ReportedException createMisplacedChunkReport(ChunkPos chunkPos, ChunkPos chunkPos2) {
        CrashReport crashReport = CrashReport.forThrowable(new IllegalStateException("Retrieved chunk position " + String.valueOf(chunkPos) + " does not match requested " + String.valueOf(chunkPos2)), "Chunk found in invalid location");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Misplaced Chunk");
        crashReportCategory.setDetail("Stored Position", chunkPos::toString);
        return new ReportedException(crashReport);
    }

    default public void reportMisplacedChunk(ChunkPos chunkPos, ChunkPos chunkPos2, RegionStorageInfo regionStorageInfo) {
        this.reportChunkLoadFailure(ChunkIOErrorReporter.createMisplacedChunkReport(chunkPos, chunkPos2), regionStorageInfo, chunkPos2);
    }
}

