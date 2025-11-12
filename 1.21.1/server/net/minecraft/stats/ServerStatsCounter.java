/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonPrimitive
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonReader
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class ServerStatsCounter
extends StatsCounter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    public ServerStatsCounter(MinecraftServer minecraftServer, File file) {
        this.server = minecraftServer;
        this.file = file;
        if (file.isFile()) {
            try {
                this.parseLocal(minecraftServer.getFixerUpper(), FileUtils.readFileToString((File)file));
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't read statistics file {}", (Object)file, (Object)iOException);
            }
            catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)file, (Object)jsonParseException);
            }
        }
    }

    public void save() {
        try {
            FileUtils.writeStringToFile((File)this.file, (String)this.toJson());
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't save stats", (Throwable)iOException);
        }
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int n) {
        super.setValue(player, stat, n);
        this.dirty.add(stat);
    }

    private Set<Stat<?>> getDirty() {
        HashSet hashSet = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return hashSet;
    }

    public void parseLocal(DataFixer dataFixer, String string) {
        try (JsonReader jsonReader = new JsonReader((Reader)new StringReader(string));){
            jsonReader.setLenient(false);
            JsonElement jsonElement = Streams.parse((JsonReader)jsonReader);
            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
                return;
            }
            CompoundTag compoundTag = ServerStatsCounter.fromJson(jsonElement.getAsJsonObject());
            if ((compoundTag = DataFixTypes.STATS.updateToCurrentVersion(dataFixer, compoundTag, NbtUtils.getDataVersion(compoundTag, 1343))).contains("stats", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("stats");
                for (String string2 : compoundTag2.getAllKeys()) {
                    if (!compoundTag2.contains(string2, 10)) continue;
                    Util.ifElse(BuiltInRegistries.STAT_TYPE.getOptional(ResourceLocation.parse(string2)), statType -> {
                        CompoundTag compoundTag2 = compoundTag2.getCompound(string2);
                        for (String string2 : compoundTag2.getAllKeys()) {
                            if (compoundTag2.contains(string2, 99)) {
                                Util.ifElse(this.getStat((StatType)statType, string2), stat -> this.stats.put(stat, compoundTag2.getInt(string2)), () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                                continue;
                            }
                            LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", new Object[]{this.file, compoundTag2.get(string2), string2});
                        }
                    }, () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                }
            }
        }
        catch (JsonParseException | IOException throwable) {
            LOGGER.error("Unable to parse Stat data from {}", (Object)this.file, (Object)throwable);
        }
    }

    private <T> Optional<Stat<T>> getStat(StatType<T> statType, String string) {
        return Optional.ofNullable(ResourceLocation.tryParse(string)).flatMap(statType.getRegistry()::getOptional).map(statType::get);
    }

    private static CompoundTag fromJson(JsonObject jsonObject) {
        CompoundTag compoundTag = new CompoundTag();
        for (Map.Entry entry : jsonObject.entrySet()) {
            JsonPrimitive jsonPrimitive;
            JsonElement jsonElement = (JsonElement)entry.getValue();
            if (jsonElement.isJsonObject()) {
                compoundTag.put((String)entry.getKey(), ServerStatsCounter.fromJson(jsonElement.getAsJsonObject()));
                continue;
            }
            if (!jsonElement.isJsonPrimitive() || !(jsonPrimitive = jsonElement.getAsJsonPrimitive()).isNumber()) continue;
            compoundTag.putInt((String)entry.getKey(), jsonPrimitive.getAsInt());
        }
        return compoundTag;
    }

    protected String toJson() {
        Object object22;
        HashMap hashMap = Maps.newHashMap();
        for (Object object22 : this.stats.object2IntEntrySet()) {
            Stat object3 = (Stat)object22.getKey();
            hashMap.computeIfAbsent(object3.getType(), statType -> new JsonObject()).addProperty(ServerStatsCounter.getKey(object3).toString(), (Number)object22.getIntValue());
        }
        ObjectIterator objectIterator = new JsonObject();
        for (Map.Entry entry : hashMap.entrySet()) {
            objectIterator.add(BuiltInRegistries.STAT_TYPE.getKey((StatType)entry.getKey()).toString(), (JsonElement)entry.getValue());
        }
        object22 = new JsonObject();
        object22.add("stats", (JsonElement)objectIterator);
        object22.addProperty("DataVersion", (Number)SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return object22.toString();
    }

    private static <T> ResourceLocation getKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    public void markAllDirty() {
        this.dirty.addAll((Collection<Stat<?>>)this.stats.keySet());
    }

    public void sendStats(ServerPlayer serverPlayer) {
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (Stat<?> stat : this.getDirty()) {
            object2IntOpenHashMap.put(stat, this.getValue(stat));
        }
        serverPlayer.connection.send(new ClientboundAwardStatsPacket((Object2IntMap<Stat<?>>)object2IntOpenHashMap));
    }
}

