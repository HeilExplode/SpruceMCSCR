/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DebugPackets {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendGameTestAddMarker(ServerLevel serverLevel, BlockPos blockPos, String string, int n, int n2) {
        DebugPackets.sendPacketToAllPlayers(serverLevel, new GameTestAddMarkerDebugPayload(blockPos, n, string, n2));
    }

    public static void sendGameTestClearPacket(ServerLevel serverLevel) {
        DebugPackets.sendPacketToAllPlayers(serverLevel, new GameTestClearMarkersDebugPayload());
    }

    public static void sendPoiPacketsForChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
    }

    public static void sendPoiAddedPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    public static void sendPoiRemovedPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    public static void sendPoiTicketCountPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    private static void sendVillageSectionsPacket(ServerLevel serverLevel, BlockPos blockPos) {
    }

    public static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float f) {
    }

    public static void sendNeighborsUpdatePacket(Level level, BlockPos blockPos) {
    }

    public static void sendStructurePacket(WorldGenLevel worldGenLevel, StructureStart structureStart) {
    }

    public static void sendGoalSelector(Level level, Mob mob, GoalSelector goalSelector) {
    }

    public static void sendRaids(ServerLevel serverLevel, Collection<Raid> collection) {
    }

    public static void sendEntityBrain(LivingEntity livingEntity) {
    }

    public static void sendBeeInfo(Bee bee) {
    }

    public static void sendBreezeInfo(Breeze breeze) {
    }

    public static void sendGameEventInfo(Level level, Holder<GameEvent> holder, Vec3 vec3) {
    }

    public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
    }

    public static void sendHiveInfo(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
    }

    private static List<String> getMemoryDescriptions(LivingEntity livingEntity, long l) {
        Map<MemoryModuleType<?>, Optional<ExpirableValue<?>>> map = livingEntity.getBrain().getMemories();
        ArrayList arrayList = Lists.newArrayList();
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : map.entrySet()) {
            Object object;
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            Optional<ExpirableValue<?>> optional = entry.getValue();
            if (optional.isPresent()) {
                ExpirableValue<?> expirableValue = optional.get();
                Object obj = expirableValue.getValue();
                if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                    long l2 = l - (Long)obj;
                    object = l2 + " ticks ago";
                } else {
                    object = expirableValue.canExpire() ? DebugPackets.getShortDescription((ServerLevel)livingEntity.level(), obj) + " (ttl: " + expirableValue.getTimeToLive() + ")" : DebugPackets.getShortDescription((ServerLevel)livingEntity.level(), obj);
                }
            } else {
                object = "-";
            }
            arrayList.add(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath() + ": " + (String)object);
        }
        arrayList.sort(String::compareTo);
        return arrayList;
    }

    private static String getShortDescription(ServerLevel serverLevel, @Nullable Object object) {
        if (object == null) {
            return "-";
        }
        if (object instanceof UUID) {
            return DebugPackets.getShortDescription(serverLevel, serverLevel.getEntity((UUID)object));
        }
        if (object instanceof LivingEntity) {
            Entity entity = (Entity)object;
            return DebugEntityNameGenerator.getEntityName(entity);
        }
        if (object instanceof Nameable) {
            return ((Nameable)object).getName().getString();
        }
        if (object instanceof WalkTarget) {
            return DebugPackets.getShortDescription(serverLevel, ((WalkTarget)object).getTarget());
        }
        if (object instanceof EntityTracker) {
            return DebugPackets.getShortDescription(serverLevel, ((EntityTracker)object).getEntity());
        }
        if (object instanceof GlobalPos) {
            return DebugPackets.getShortDescription(serverLevel, ((GlobalPos)object).pos());
        }
        if (object instanceof BlockPosTracker) {
            return DebugPackets.getShortDescription(serverLevel, ((BlockPosTracker)object).currentBlockPosition());
        }
        if (object instanceof DamageSource) {
            Entity entity = ((DamageSource)object).getEntity();
            return entity == null ? object.toString() : DebugPackets.getShortDescription(serverLevel, entity);
        }
        if (object instanceof Collection) {
            ArrayList arrayList = Lists.newArrayList();
            for (Object t : (Iterable)object) {
                arrayList.add(DebugPackets.getShortDescription(serverLevel, t));
            }
            return ((Object)arrayList).toString();
        }
        return object.toString();
    }

    private static void sendPacketToAllPlayers(ServerLevel serverLevel, CustomPacketPayload customPacketPayload) {
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket(customPacketPayload);
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            serverPlayer.connection.send(clientboundCustomPayloadPacket);
        }
    }

    private static /* synthetic */ void lambda$sendGameEventInfo$7(ServerLevel serverLevel, Vec3 vec3, ResourceKey resourceKey) {
        DebugPackets.sendPacketToAllPlayers(serverLevel, new GameEventDebugPayload(resourceKey, vec3));
    }

    private static /* synthetic */ void lambda$sendEntityBrain$6(List list, UUID uUID, Object2IntMap object2IntMap) {
        String string = DebugEntityNameGenerator.getEntityName(uUID);
        object2IntMap.forEach((gossipType, n) -> list.add(string + ": " + String.valueOf(gossipType) + ": " + n));
    }

    private static /* synthetic */ String lambda$sendEntityBrain$4(String string) {
        return StringUtil.truncateStringIfNecessary(string, 255, true);
    }

    private static /* synthetic */ void lambda$sendGoalSelector$3(List list, WrappedGoal wrappedGoal) {
        list.add(new GoalDebugPayload.DebugGoal(wrappedGoal.getPriority(), wrappedGoal.isRunning(), wrappedGoal.getGoal().getClass().getSimpleName()));
    }

    private static /* synthetic */ String lambda$sendPoiAddedPacket$2(ResourceKey resourceKey) {
        return resourceKey.location().toString();
    }

    private static /* synthetic */ void lambda$sendPoiPacketsForChunk$1(ServerLevel serverLevel, PoiRecord poiRecord) {
        DebugPackets.sendPoiAddedPacket(serverLevel, poiRecord.getPos());
    }

    private static /* synthetic */ boolean lambda$sendPoiPacketsForChunk$0(Holder holder) {
        return true;
    }
}

