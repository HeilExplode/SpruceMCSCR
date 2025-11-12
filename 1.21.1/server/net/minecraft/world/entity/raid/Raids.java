/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class Raids
extends SavedData {
    private static final String RAID_FILE_ID = "raids";
    private final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final ServerLevel level;
    private int nextAvailableID;
    private int tick;

    public static SavedData.Factory<Raids> factory(ServerLevel serverLevel) {
        return new SavedData.Factory<Raids>(() -> new Raids(serverLevel), (compoundTag, provider) -> Raids.load(serverLevel, compoundTag), DataFixTypes.SAVED_DATA_RAIDS);
    }

    public Raids(ServerLevel serverLevel) {
        this.level = serverLevel;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public Raid get(int n) {
        return this.raidMap.get(n);
    }

    public void tick() {
        ++this.tick;
        Iterator<Raid> iterator = this.raidMap.values().iterator();
        while (iterator.hasNext()) {
            Raid raid = iterator.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                raid.stop();
            }
            if (raid.isStopped()) {
                iterator.remove();
                this.setDirty();
                continue;
            }
            raid.tick();
        }
        if (this.tick % 200 == 0) {
            this.setDirty();
        }
        DebugPackets.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(Raider raider, Raid raid) {
        if (raider != null && raid != null && raid.getLevel() != null) {
            return raider.isAlive() && raider.canJoinRaid() && raider.getNoActionTime() <= 2400 && raider.level().dimensionType() == raid.getLevel().dimensionType();
        }
        return false;
    }

    @Nullable
    public Raid createOrExtendRaid(ServerPlayer serverPlayer, BlockPos blockPos) {
        Object object;
        if (serverPlayer.isSpectator()) {
            return null;
        }
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            return null;
        }
        DimensionType dimensionType = serverPlayer.level().dimensionType();
        if (!dimensionType.hasRaids()) {
            return null;
        }
        List<PoiRecord> list = this.level.getPoiManager().getInRange(holder -> holder.is(PoiTypeTags.VILLAGE), blockPos, 64, PoiManager.Occupancy.IS_OCCUPIED).toList();
        int n = 0;
        Vec3 vec3 = Vec3.ZERO;
        for (PoiRecord object22 : list) {
            BlockPos blockPos2 = object22.getPos();
            vec3 = vec3.add(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
            ++n;
        }
        if (n > 0) {
            vec3 = vec3.scale(1.0 / (double)n);
            object = BlockPos.containing(vec3);
        } else {
            object = blockPos;
        }
        Raid raid = this.getOrCreateRaid(serverPlayer.serverLevel(), (BlockPos)object);
        if (!raid.isStarted() && !this.raidMap.containsKey(raid.getId())) {
            this.raidMap.put(raid.getId(), raid);
        }
        if (!raid.isStarted() || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel()) {
            raid.absorbRaidOmen(serverPlayer);
        }
        this.setDirty();
        return raid;
    }

    private Raid getOrCreateRaid(ServerLevel serverLevel, BlockPos blockPos) {
        Raid raid = serverLevel.getRaidAt(blockPos);
        return raid != null ? raid : new Raid(this.getUniqueId(), serverLevel, blockPos);
    }

    public static Raids load(ServerLevel serverLevel, CompoundTag compoundTag) {
        Raids raids = new Raids(serverLevel);
        raids.nextAvailableID = compoundTag.getInt("NextAvailableID");
        raids.tick = compoundTag.getInt("Tick");
        ListTag listTag = compoundTag.getList("Raids", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            Raid raid = new Raid(serverLevel, compoundTag2);
            raids.raidMap.put(raid.getId(), raid);
        }
        return raids;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt("NextAvailableID", this.nextAvailableID);
        compoundTag.putInt("Tick", this.tick);
        ListTag listTag = new ListTag();
        for (Raid raid : this.raidMap.values()) {
            CompoundTag compoundTag2 = new CompoundTag();
            raid.save(compoundTag2);
            listTag.add(compoundTag2);
        }
        compoundTag.put("Raids", listTag);
        return compoundTag;
    }

    public static String getFileId(Holder<DimensionType> holder) {
        if (holder.is(BuiltinDimensionTypes.END)) {
            return "raids_end";
        }
        return RAID_FILE_ID;
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPos blockPos, int n) {
        Raid raid = null;
        double d = n;
        for (Raid raid2 : this.raidMap.values()) {
            double d2 = raid2.getCenter().distSqr(blockPos);
            if (!raid2.isActive() || !(d2 < d)) continue;
            raid = raid2;
            d = d2;
        }
        return raid;
    }
}

