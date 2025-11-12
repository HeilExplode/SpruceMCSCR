/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.invoke.LambdaMetafactory;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public class PlayerChunkSender {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float MIN_CHUNKS_PER_TICK = 0.01f;
    public static final float MAX_CHUNKS_PER_TICK = 64.0f;
    private static final float START_CHUNKS_PER_TICK = 9.0f;
    private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
    private final LongSet pendingChunks = new LongOpenHashSet();
    private final boolean memoryConnection;
    private float desiredChunksPerTick = 9.0f;
    private float batchQuota;
    private int unacknowledgedBatches;
    private int maxUnacknowledgedBatches = 1;

    public PlayerChunkSender(boolean bl) {
        this.memoryConnection = bl;
    }

    public void markChunkPendingToSend(LevelChunk levelChunk) {
        this.pendingChunks.add(levelChunk.getPos().toLong());
    }

    public void dropChunk(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        if (!this.pendingChunks.remove(chunkPos.toLong()) && serverPlayer.isAlive()) {
            serverPlayer.connection.send(new ClientboundForgetLevelChunkPacket(chunkPos));
        }
    }

    public void sendNextChunks(ServerPlayer serverPlayer) {
        if (this.unacknowledgedBatches >= this.maxUnacknowledgedBatches) {
            return;
        }
        float f = Math.max(1.0f, this.desiredChunksPerTick);
        this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, f);
        if (this.batchQuota < 1.0f) {
            return;
        }
        if (this.pendingChunks.isEmpty()) {
            return;
        }
        ServerLevel serverLevel = serverPlayer.serverLevel();
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
        List<LevelChunk> list = this.collectChunksToSend(chunkMap, serverPlayer.chunkPosition());
        if (list.isEmpty()) {
            return;
        }
        ServerGamePacketListenerImpl serverGamePacketListenerImpl = serverPlayer.connection;
        ++this.unacknowledgedBatches;
        serverGamePacketListenerImpl.send(ClientboundChunkBatchStartPacket.INSTANCE);
        for (LevelChunk levelChunk : list) {
            PlayerChunkSender.sendChunk(serverGamePacketListenerImpl, serverLevel, levelChunk);
        }
        serverGamePacketListenerImpl.send(new ClientboundChunkBatchFinishedPacket(list.size()));
        this.batchQuota -= (float)list.size();
    }

    private static void sendChunk(ServerGamePacketListenerImpl serverGamePacketListenerImpl, ServerLevel serverLevel, LevelChunk levelChunk) {
        serverGamePacketListenerImpl.send(new ClientboundLevelChunkWithLightPacket(levelChunk, serverLevel.getLightEngine(), null, null));
        ChunkPos chunkPos = levelChunk.getPos();
        DebugPackets.sendPoiPacketsForChunk(serverLevel, chunkPos);
    }

    /*
     * Unable to fully structure code
     */
    private List<LevelChunk> collectChunksToSend(ChunkMap var1_1, ChunkPos var2_2) {
        var4_3 = Mth.floor(this.batchQuota);
        if (this.memoryConnection) ** GOTO lbl7
        if (this.pendingChunks.size() <= var4_3) {
lbl7:
            // 2 sources

            var3_4 = this.pendingChunks.longStream().mapToObj((LongFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getChunkToSend(long ), (J)Lnet/minecraft/world/level/chunk/LevelChunk;)((ChunkMap)var1_1)).filter((Predicate<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/level/chunk/LevelChunk;)Z)()).sorted(Comparator.comparingInt((ToIntFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, lambda$collectChunksToSend$0(net.minecraft.world.level.ChunkPos net.minecraft.world.level.chunk.LevelChunk ), (Lnet/minecraft/world/level/chunk/LevelChunk;)I)((ChunkPos)var2_2))).toList();
        } else {
            var3_4 = ((List)this.pendingChunks.stream().collect(Comparators.least((int)var4_3, Comparator.comparingInt((ToIntFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, distanceSquared(long ), (Ljava/lang/Long;)I)((ChunkPos)var2_2))))).stream().mapToLong((ToLongFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)J, longValue(), (Ljava/lang/Long;)J)()).mapToObj((LongFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getChunkToSend(long ), (J)Lnet/minecraft/world/level/chunk/LevelChunk;)((ChunkMap)var1_1)).filter((Predicate<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/level/chunk/LevelChunk;)Z)()).toList();
        }
        for (LevelChunk var6_6 : var3_4) {
            this.pendingChunks.remove(var6_6.getPos().toLong());
        }
        return var3_4;
    }

    public void onChunkBatchReceivedByClient(float f) {
        --this.unacknowledgedBatches;
        float f2 = this.desiredChunksPerTick = Double.isNaN(f) ? 0.01f : Mth.clamp(f, 0.01f, 64.0f);
        if (this.unacknowledgedBatches == 0) {
            this.batchQuota = 1.0f;
        }
        this.maxUnacknowledgedBatches = 10;
    }

    public boolean isPending(long l) {
        return this.pendingChunks.contains(l);
    }

    private static /* synthetic */ int lambda$collectChunksToSend$0(ChunkPos chunkPos, LevelChunk levelChunk) {
        return chunkPos.distanceSquared(levelChunk.getPos());
    }
}

