/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.slf4j.Logger;

public class MapItemSavedData
extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    private static final String FRAME_PREFIX = "frame-";
    public final int centerX;
    public final int centerZ;
    public final ResourceKey<Level> dimension;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    public static SavedData.Factory<MapItemSavedData> factory() {
        return new SavedData.Factory<MapItemSavedData>(() -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, MapItemSavedData::load, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private MapItemSavedData(int n, int n2, byte by, boolean bl, boolean bl2, boolean bl3, ResourceKey<Level> resourceKey) {
        this.scale = by;
        this.centerX = n;
        this.centerZ = n2;
        this.dimension = resourceKey;
        this.trackingPosition = bl;
        this.unlimitedTracking = bl2;
        this.locked = bl3;
        this.setDirty();
    }

    public static MapItemSavedData createFresh(double d, double d2, byte by, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int n = 128 * (1 << by);
        int n2 = Mth.floor((d + 64.0) / (double)n);
        int n3 = Mth.floor((d2 + 64.0) / (double)n);
        int n4 = n2 * n + n / 2 - 64;
        int n5 = n3 * n + n / 2 - 64;
        return new MapItemSavedData(n4, n5, by, bl, bl2, false, resourceKey);
    }

    public static MapItemSavedData createForClient(byte by, boolean bl, ResourceKey<Level> resourceKey) {
        return new MapItemSavedData(0, 0, by, false, false, bl, resourceKey);
    }

    public static MapItemSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ResourceKey resourceKey = (ResourceKey)DimensionType.parseLegacy(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("dimension"))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + String.valueOf(compoundTag.get("dimension"))));
        int n = compoundTag.getInt("xCenter");
        int n2 = compoundTag.getInt("zCenter");
        byte by = (byte)Mth.clamp(compoundTag.getByte("scale"), 0, 4);
        boolean bl = !compoundTag.contains("trackingPosition", 1) || compoundTag.getBoolean("trackingPosition");
        boolean bl2 = compoundTag.getBoolean("unlimitedTracking");
        boolean bl3 = compoundTag.getBoolean("locked");
        MapItemSavedData mapItemSavedData = new MapItemSavedData(n, n2, by, bl, bl2, bl3, resourceKey);
        byte[] byArray = compoundTag.getByteArray("colors");
        if (byArray.length == 16384) {
            mapItemSavedData.colors = byArray;
        }
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        List list = MapBanner.LIST_CODEC.parse(registryOps, (Object)compoundTag.get("banners")).resultOrPartial(string -> LOGGER.warn("Failed to parse map banner: '{}'", string)).orElse(List.of());
        for (MapBanner mapBanner : list) {
            mapItemSavedData.bannerMarkers.put(mapBanner.getId(), mapBanner);
            mapItemSavedData.addDecoration(mapBanner.getDecoration(), null, mapBanner.getId(), mapBanner.pos().getX(), mapBanner.pos().getZ(), 180.0, mapBanner.name().orElse(null));
        }
        ListTag listTag = compoundTag.getList("frames", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            MapFrame mapFrame = MapFrame.load(listTag.getCompound(i));
            if (mapFrame == null) continue;
            mapItemSavedData.frameMarkers.put(mapFrame.getId(), mapFrame);
            mapItemSavedData.addDecoration(MapDecorationTypes.FRAME, null, MapItemSavedData.getFrameKey(mapFrame.getEntityId()), mapFrame.getPos().getX(), mapFrame.getPos().getZ(), mapFrame.getRotation(), null);
        }
        return mapItemSavedData;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ResourceLocation.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.dimension.location()).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("dimension", (Tag)tag));
        compoundTag.putInt("xCenter", this.centerX);
        compoundTag.putInt("zCenter", this.centerZ);
        compoundTag.putByte("scale", this.scale);
        compoundTag.putByteArray("colors", this.colors);
        compoundTag.putBoolean("trackingPosition", this.trackingPosition);
        compoundTag.putBoolean("unlimitedTracking", this.unlimitedTracking);
        compoundTag.putBoolean("locked", this.locked);
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        compoundTag.put("banners", (Tag)MapBanner.LIST_CODEC.encodeStart(registryOps, List.copyOf(this.bannerMarkers.values())).getOrThrow());
        ListTag listTag = new ListTag();
        for (MapFrame mapFrame : this.frameMarkers.values()) {
            listTag.add(mapFrame.save());
        }
        compoundTag.put("frames", listTag);
        return compoundTag;
    }

    public MapItemSavedData locked() {
        MapItemSavedData mapItemSavedData = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
        mapItemSavedData.bannerMarkers.putAll(this.bannerMarkers);
        mapItemSavedData.decorations.putAll(this.decorations);
        mapItemSavedData.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, mapItemSavedData.colors, 0, this.colors.length);
        mapItemSavedData.setDirty();
        return mapItemSavedData;
    }

    public MapItemSavedData scaled() {
        return MapItemSavedData.createFresh(this.centerX, this.centerZ, (byte)Mth.clamp(this.scale + 1, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> mapMatcher(ItemStack itemStack) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        return itemStack2 -> {
            if (itemStack2 == itemStack) {
                return true;
            }
            return itemStack2.is(itemStack.getItem()) && Objects.equals(mapId, itemStack2.get(DataComponents.MAP_ID));
        };
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        Object object;
        Object object2;
        Object object3;
        if (!this.carriedByPlayers.containsKey(player)) {
            object3 = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, (HoldingPlayer)object3);
            this.carriedBy.add((HoldingPlayer)object3);
        }
        object3 = MapItemSavedData.mapMatcher(itemStack);
        if (!player.getInventory().contains((Predicate<ItemStack>)object3)) {
            this.removeDecoration(player.getName().getString());
        }
        for (int i = 0; i < this.carriedBy.size(); ++i) {
            object2 = this.carriedBy.get(i);
            object = ((HoldingPlayer)object2).player.getName().getString();
            if (((HoldingPlayer)object2).player.isRemoved() || !((HoldingPlayer)object2).player.getInventory().contains((Predicate<ItemStack>)object3) && !itemStack.isFramed()) {
                this.carriedByPlayers.remove(((HoldingPlayer)object2).player);
                this.carriedBy.remove(object2);
                this.removeDecoration((String)object);
                continue;
            }
            if (itemStack.isFramed() || ((HoldingPlayer)object2).player.level().dimension() != this.dimension || !this.trackingPosition) continue;
            this.addDecoration(MapDecorationTypes.PLAYER, ((HoldingPlayer)object2).player.level(), (String)object, ((HoldingPlayer)object2).player.getX(), ((HoldingPlayer)object2).player.getZ(), ((HoldingPlayer)object2).player.getYRot(), null);
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame itemFrame = itemStack.getFrame();
            object2 = itemFrame.getPos();
            object = this.frameMarkers.get(MapFrame.frameId((BlockPos)object2));
            if (object != null && itemFrame.getId() != ((MapFrame)object).getEntityId() && this.frameMarkers.containsKey(((MapFrame)object).getId())) {
                this.removeDecoration(MapItemSavedData.getFrameKey(((MapFrame)object).getEntityId()));
            }
            MapFrame mapFrame = new MapFrame((BlockPos)object2, itemFrame.getDirection().get2DDataValue() * 90, itemFrame.getId());
            this.addDecoration(MapDecorationTypes.FRAME, player.level(), MapItemSavedData.getFrameKey(itemFrame.getId()), ((Vec3i)object2).getX(), ((Vec3i)object2).getZ(), itemFrame.getDirection().get2DDataValue() * 90, null);
            this.frameMarkers.put(mapFrame.getId(), mapFrame);
        }
        MapDecorations mapDecorations = itemStack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        if (!this.decorations.keySet().containsAll(mapDecorations.decorations().keySet())) {
            mapDecorations.decorations().forEach((string, entry) -> {
                if (!this.decorations.containsKey(string)) {
                    this.addDecoration(entry.type(), player.level(), (String)string, entry.x(), entry.z(), entry.rotation(), null);
                }
            });
        }
    }

    private void removeDecoration(String string) {
        MapDecoration mapDecoration = this.decorations.remove(string);
        if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
            --this.trackedDecorationCount;
        }
        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String string, Holder<MapDecorationType> holder) {
        MapDecorations.Entry entry = new MapDecorations.Entry(holder, blockPos.getX(), blockPos.getZ(), 180.0f);
        itemStack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, mapDecorations -> mapDecorations.withDecoration(string, entry));
        if (holder.value().hasMapColor()) {
            itemStack.set(DataComponents.MAP_COLOR, new MapItemColor(holder.value().mapColor()));
        }
    }

    private void addDecoration(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, String string, double d, double d2, double d3, @Nullable Component component) {
        MapDecoration mapDecoration;
        MapDecoration mapDecoration2;
        byte by;
        int n = 1 << this.scale;
        float f = (float)(d - (double)this.centerX) / (float)n;
        float f2 = (float)(d2 - (double)this.centerZ) / (float)n;
        byte by2 = (byte)((double)(f * 2.0f) + 0.5);
        byte by3 = (byte)((double)(f2 * 2.0f) + 0.5);
        int n2 = 63;
        if (f >= -63.0f && f2 >= -63.0f && f <= 63.0f && f2 <= 63.0f) {
            by = (byte)((d3 += d3 < 0.0 ? -8.0 : 8.0) * 16.0 / 360.0);
            if (this.dimension == Level.NETHER && levelAccessor != null) {
                var18_15 = (int)(levelAccessor.getLevelData().getDayTime() / 10L);
                by = (byte)(var18_15 * var18_15 * 34187121 + var18_15 * 121 >> 15 & 0xF);
            }
        } else if (holder.is(MapDecorationTypes.PLAYER)) {
            var18_15 = 320;
            if (Math.abs(f) < 320.0f && Math.abs(f2) < 320.0f) {
                holder = MapDecorationTypes.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                holder = MapDecorationTypes.PLAYER_OFF_LIMITS;
            } else {
                this.removeDecoration(string);
                return;
            }
            by = 0;
            if (f <= -63.0f) {
                by2 = -128;
            }
            if (f2 <= -63.0f) {
                by3 = -128;
            }
            if (f >= 63.0f) {
                by2 = 127;
            }
            if (f2 >= 63.0f) {
                by3 = 127;
            }
        } else {
            this.removeDecoration(string);
            return;
        }
        if (!(mapDecoration2 = new MapDecoration(holder, by2, by3, by, Optional.ofNullable(component))).equals(mapDecoration = this.decorations.put(string, mapDecoration2))) {
            if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
                --this.trackedDecorationCount;
            }
            if (holder.value().trackCount()) {
                ++this.trackedDecorationCount;
            }
            this.setDecorationsDirty();
        }
    }

    @Nullable
    public Packet<?> getUpdatePacket(MapId mapId, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(mapId);
    }

    private void setColorsDirty(int n, int n2) {
        this.setDirty();
        for (HoldingPlayer holdingPlayer : this.carriedBy) {
            holdingPlayer.markColorsDirty(n, n2);
        }
    }

    private void setDecorationsDirty() {
        this.setDirty();
        this.carriedBy.forEach(HoldingPlayer::markDecorationsDirty);
    }

    public HoldingPlayer getHoldingPlayer(Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        return holdingPlayer;
    }

    public boolean toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5;
        double d2 = (double)blockPos.getZ() + 0.5;
        int n = 1 << this.scale;
        double d3 = (d - (double)this.centerX) / (double)n;
        double d4 = (d2 - (double)this.centerZ) / (double)n;
        int n2 = 63;
        if (d3 >= -63.0 && d4 >= -63.0 && d3 <= 63.0 && d4 <= 63.0) {
            MapBanner mapBanner = MapBanner.fromWorld(levelAccessor, blockPos);
            if (mapBanner == null) {
                return false;
            }
            if (this.bannerMarkers.remove(mapBanner.getId(), mapBanner)) {
                this.removeDecoration(mapBanner.getId());
                return true;
            }
            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(mapBanner.getId(), mapBanner);
                this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, d2, 180.0, mapBanner.name().orElse(null));
                return true;
            }
        }
        return false;
    }

    public void checkBanners(BlockGetter blockGetter, int n, int n2) {
        Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();
        while (iterator.hasNext()) {
            MapBanner mapBanner;
            MapBanner mapBanner2 = iterator.next();
            if (mapBanner2.pos().getX() != n || mapBanner2.pos().getZ() != n2 || mapBanner2.equals(mapBanner = MapBanner.fromWorld(blockGetter, mapBanner2.pos()))) continue;
            iterator.remove();
            this.removeDecoration(mapBanner2.getId());
        }
    }

    public Collection<MapBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPos blockPos, int n) {
        this.removeDecoration(MapItemSavedData.getFrameKey(n));
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
    }

    public boolean updateColor(int n, int n2, byte by) {
        byte by2 = this.colors[n + n2 * 128];
        if (by2 != by) {
            this.setColor(n, n2, by);
            return true;
        }
        return false;
    }

    public void setColor(int n, int n2, byte by) {
        this.colors[n + n2 * 128] = by;
        this.setColorsDirty(n, n2);
    }

    public boolean isExplorationMap() {
        for (MapDecoration mapDecoration : this.decorations.values()) {
            if (!mapDecoration.type().value().explorationMapElement()) continue;
            return true;
        }
        return false;
    }

    public void addClientSideDecorations(List<MapDecoration> list) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;
        for (int i = 0; i < list.size(); ++i) {
            MapDecoration mapDecoration = list.get(i);
            this.decorations.put("icon-" + i, mapDecoration);
            if (!mapDecoration.type().value().trackCount()) continue;
            ++this.trackedDecorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int n) {
        return this.trackedDecorationCount >= n;
    }

    private static String getFrameKey(int n) {
        return FRAME_PREFIX + n;
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        HoldingPlayer(Player player) {
            this.player = player;
        }

        private MapPatch createPatch() {
            int n = this.minDirtyX;
            int n2 = this.minDirtyY;
            int n3 = this.maxDirtyX + 1 - this.minDirtyX;
            int n4 = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] byArray = new byte[n3 * n4];
            for (int i = 0; i < n3; ++i) {
                for (int j = 0; j < n4; ++j) {
                    byArray[i + j * n3] = MapItemSavedData.this.colors[n + i + (n2 + j) * 128];
                }
            }
            return new MapPatch(n, n2, n3, n4, byArray);
        }

        @Nullable
        Packet<?> nextUpdatePacket(MapId mapId) {
            Collection<MapDecoration> collection;
            MapPatch mapPatch;
            if (this.dirtyData) {
                this.dirtyData = false;
                mapPatch = this.createPatch();
            } else {
                mapPatch = null;
            }
            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                collection = MapItemSavedData.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || mapPatch != null) {
                return new ClientboundMapItemDataPacket(mapId, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapPatch);
            }
            return null;
        }

        void markColorsDirty(int n, int n2) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, n);
                this.minDirtyY = Math.min(this.minDirtyY, n2);
                this.maxDirtyX = Math.max(this.maxDirtyX, n);
                this.maxDirtyY = Math.max(this.maxDirtyY, n2);
            } else {
                this.dirtyData = true;
                this.minDirtyX = n;
                this.minDirtyY = n2;
                this.maxDirtyX = n;
                this.maxDirtyY = n2;
            }
        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    public record MapPatch(int startX, int startY, int width, int height, byte[] mapColors) {
        public static final StreamCodec<ByteBuf, Optional<MapPatch>> STREAM_CODEC = StreamCodec.of(MapPatch::write, MapPatch::read);

        private static void write(ByteBuf byteBuf, Optional<MapPatch> optional) {
            if (optional.isPresent()) {
                MapPatch mapPatch = optional.get();
                byteBuf.writeByte(mapPatch.width);
                byteBuf.writeByte(mapPatch.height);
                byteBuf.writeByte(mapPatch.startX);
                byteBuf.writeByte(mapPatch.startY);
                FriendlyByteBuf.writeByteArray(byteBuf, mapPatch.mapColors);
            } else {
                byteBuf.writeByte(0);
            }
        }

        private static Optional<MapPatch> read(ByteBuf byteBuf) {
            short s = byteBuf.readUnsignedByte();
            if (s > 0) {
                short s2 = byteBuf.readUnsignedByte();
                short s3 = byteBuf.readUnsignedByte();
                short s4 = byteBuf.readUnsignedByte();
                byte[] byArray = FriendlyByteBuf.readByteArray(byteBuf);
                return Optional.of(new MapPatch(s3, s4, s, s2, byArray));
            }
            return Optional.empty();
        }

        public void applyToMap(MapItemSavedData mapItemSavedData) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapItemSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
                }
            }
        }
    }
}

