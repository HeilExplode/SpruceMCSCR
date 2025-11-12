/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Queues
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import org.slf4j.Logger;

public class PersistentEntitySectionManager<T extends EntityAccess>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    final Set<UUID> knownUuids = Sets.newHashSet();
    final LevelCallback<T> callbacks;
    private final EntityPersistentStorage<T> permanentStorage;
    private final EntityLookup<T> visibleEntityStorage;
    final EntitySectionStorage<T> sectionStorage;
    private final LevelEntityGetter<T> entityGetter;
    private final Long2ObjectMap<Visibility> chunkVisibility = new Long2ObjectOpenHashMap();
    private final Long2ObjectMap<ChunkLoadStatus> chunkLoadStatuses = new Long2ObjectOpenHashMap();
    private final LongSet chunksToUnload = new LongOpenHashSet();
    private final Queue<ChunkEntities<T>> loadingInbox = Queues.newConcurrentLinkedQueue();

    public PersistentEntitySectionManager(Class<T> clazz, LevelCallback<T> levelCallback, EntityPersistentStorage<T> entityPersistentStorage) {
        this.visibleEntityStorage = new EntityLookup();
        this.sectionStorage = new EntitySectionStorage<T>(clazz, (Long2ObjectFunction<Visibility>)this.chunkVisibility);
        this.chunkVisibility.defaultReturnValue((Object)Visibility.HIDDEN);
        this.chunkLoadStatuses.defaultReturnValue((Object)ChunkLoadStatus.FRESH);
        this.callbacks = levelCallback;
        this.permanentStorage = entityPersistentStorage;
        this.entityGetter = new LevelEntityGetterAdapter<T>(this.visibleEntityStorage, this.sectionStorage);
    }

    void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
        if (entitySection.isEmpty()) {
            this.sectionStorage.remove(l);
        }
    }

    private boolean addEntityUuid(T t) {
        if (!this.knownUuids.add(t.getUUID())) {
            LOGGER.warn("UUID of added entity already exists: {}", t);
            return false;
        }
        return true;
    }

    public boolean addNewEntity(T t) {
        return this.addEntity(t, false);
    }

    private boolean addEntity(T t, boolean bl) {
        Visibility visibility;
        if (!this.addEntityUuid(t)) {
            return false;
        }
        long l = SectionPos.asLong(t.blockPosition());
        EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
        entitySection.add(t);
        t.setLevelCallback(new Callback(this, t, l, entitySection));
        if (!bl) {
            this.callbacks.onCreated(t);
        }
        if ((visibility = PersistentEntitySectionManager.getEffectiveStatus(t, entitySection.getStatus())).isAccessible()) {
            this.startTracking(t);
        }
        if (visibility.isTicking()) {
            this.startTicking(t);
        }
        return true;
    }

    static <T extends EntityAccess> Visibility getEffectiveStatus(T t, Visibility visibility) {
        return t.isAlwaysTicking() ? Visibility.TICKING : visibility;
    }

    public void addLegacyChunkEntities(Stream<T> stream) {
        stream.forEach(entityAccess -> this.addEntity(entityAccess, true));
    }

    public void addWorldGenChunkEntities(Stream<T> stream) {
        stream.forEach(entityAccess -> this.addEntity(entityAccess, false));
    }

    void startTicking(T t) {
        this.callbacks.onTickingStart(t);
    }

    void stopTicking(T t) {
        this.callbacks.onTickingEnd(t);
    }

    void startTracking(T t) {
        this.visibleEntityStorage.add(t);
        this.callbacks.onTrackingStart(t);
    }

    void stopTracking(T t) {
        this.callbacks.onTrackingEnd(t);
        this.visibleEntityStorage.remove(t);
    }

    public void updateChunkStatus(ChunkPos chunkPos, FullChunkStatus fullChunkStatus) {
        Visibility visibility = Visibility.fromFullChunkStatus(fullChunkStatus);
        this.updateChunkStatus(chunkPos, visibility);
    }

    public void updateChunkStatus(ChunkPos chunkPos, Visibility visibility) {
        long l = chunkPos.toLong();
        if (visibility == Visibility.HIDDEN) {
            this.chunkVisibility.remove(l);
            this.chunksToUnload.add(l);
        } else {
            this.chunkVisibility.put(l, (Object)visibility);
            this.chunksToUnload.remove(l);
            this.ensureChunkQueuedForLoad(l);
        }
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility2 = entitySection.updateChunkStatus(visibility);
            boolean bl = visibility2.isAccessible();
            boolean bl2 = visibility.isAccessible();
            boolean bl3 = visibility2.isTicking();
            boolean bl4 = visibility.isTicking();
            if (bl3 && !bl4) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTicking);
            }
            if (bl && !bl2) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTracking);
            } else if (!bl && bl2) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTracking);
            }
            if (!bl3 && bl4) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTicking);
            }
        });
    }

    private void ensureChunkQueuedForLoad(long l) {
        ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)this.chunkLoadStatuses.get(l));
        if (chunkLoadStatus == ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(l);
        }
    }

    private boolean storeChunkSections(long l, Consumer<T> consumer) {
        ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)this.chunkLoadStatuses.get(l));
        if (chunkLoadStatus == ChunkLoadStatus.PENDING) {
            return false;
        }
        List<T> list = this.sectionStorage.getExistingSectionsInChunk(l).flatMap(entitySection -> entitySection.getEntities().filter(EntityAccess::shouldBeSaved)).collect(Collectors.toList());
        if (list.isEmpty()) {
            if (chunkLoadStatus == ChunkLoadStatus.LOADED) {
                this.permanentStorage.storeEntities(new ChunkEntities(new ChunkPos(l), ImmutableList.of()));
            }
            return true;
        }
        if (chunkLoadStatus == ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(l);
            return false;
        }
        this.permanentStorage.storeEntities(new ChunkEntities(new ChunkPos(l), list));
        list.forEach(consumer);
        return true;
    }

    private void requestChunkLoad(long l) {
        this.chunkLoadStatuses.put(l, (Object)ChunkLoadStatus.PENDING);
        ChunkPos chunkPos = new ChunkPos(l);
        ((CompletableFuture)this.permanentStorage.loadEntities(chunkPos).thenAccept(this.loadingInbox::add)).exceptionally(throwable -> {
            LOGGER.error("Failed to read chunk {}", (Object)chunkPos, throwable);
            return null;
        });
    }

    private boolean processChunkUnload(long l) {
        boolean bl = this.storeChunkSections(l, entityAccess -> entityAccess.getPassengersAndSelf().forEach(this::unloadEntity));
        if (!bl) {
            return false;
        }
        this.chunkLoadStatuses.remove(l);
        return true;
    }

    private void unloadEntity(EntityAccess entityAccess) {
        entityAccess.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        entityAccess.setLevelCallback(EntityInLevelCallback.NULL);
    }

    private void processUnloads() {
        this.chunksToUnload.removeIf(l -> {
            if (this.chunkVisibility.get(l) != Visibility.HIDDEN) {
                return true;
            }
            return this.processChunkUnload(l);
        });
    }

    private void processPendingLoads() {
        ChunkEntities<T> chunkEntities;
        while ((chunkEntities = this.loadingInbox.poll()) != null) {
            chunkEntities.getEntities().forEach(entityAccess -> this.addEntity(entityAccess, true));
            this.chunkLoadStatuses.put(chunkEntities.getPos().toLong(), (Object)ChunkLoadStatus.LOADED);
        }
    }

    public void tick() {
        this.processPendingLoads();
        this.processUnloads();
    }

    private LongSet getAllChunksToSave() {
        LongSet longSet = this.sectionStorage.getAllChunksWithExistingSections();
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.chunkLoadStatuses)) {
            if (entry.getValue() != ChunkLoadStatus.LOADED) continue;
            longSet.add(entry.getLongKey());
        }
        return longSet;
    }

    public void autoSave() {
        this.getAllChunksToSave().forEach(l -> {
            boolean bl;
            boolean bl2 = bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
            if (bl) {
                this.processChunkUnload(l);
            } else {
                this.storeChunkSections(l, entityAccess -> {});
            }
        });
    }

    public void saveAll() {
        LongSet longSet = this.getAllChunksToSave();
        while (!longSet.isEmpty()) {
            this.permanentStorage.flush(false);
            this.processPendingLoads();
            longSet.removeIf(l -> {
                boolean bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
                return bl ? this.processChunkUnload(l) : this.storeChunkSections(l, entityAccess -> {});
            });
        }
        this.permanentStorage.flush(true);
    }

    @Override
    public void close() throws IOException {
        this.saveAll();
        this.permanentStorage.close();
    }

    public boolean isLoaded(UUID uUID) {
        return this.knownUuids.contains(uUID);
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public boolean canPositionTick(BlockPos blockPos) {
        return ((Visibility)((Object)this.chunkVisibility.get(ChunkPos.asLong(blockPos)))).isTicking();
    }

    public boolean canPositionTick(ChunkPos chunkPos) {
        return ((Visibility)((Object)this.chunkVisibility.get(chunkPos.toLong()))).isTicking();
    }

    public boolean areEntitiesLoaded(long l) {
        return this.chunkLoadStatuses.get(l) == ChunkLoadStatus.LOADED;
    }

    public void dumpSections(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("visibility").addColumn("load_status").addColumn("entity_count").build(writer);
        this.sectionStorage.getAllChunksWithExistingSections().forEach(l2 -> {
            ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)((Object)this.chunkLoadStatuses.get(l2)));
            this.sectionStorage.getExistingSectionPositionsInChunk(l2).forEach(l -> {
                EntitySection<T> entitySection = this.sectionStorage.getSection(l);
                if (entitySection != null) {
                    try {
                        csvOutput.writeRow(new Object[]{SectionPos.x(l), SectionPos.y(l), SectionPos.z(l), entitySection.getStatus(), chunkLoadStatus, entitySection.size()});
                    }
                    catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }
            });
        });
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.knownUuids.size() + "," + this.visibleEntityStorage.count() + "," + this.sectionStorage.count() + "," + this.chunkLoadStatuses.size() + "," + this.chunkVisibility.size() + "," + this.loadingInbox.size() + "," + this.chunksToUnload.size();
    }

    @VisibleForDebug
    public int count() {
        return this.visibleEntityStorage.count();
    }

    static enum ChunkLoadStatus {
        FRESH,
        PENDING,
        LOADED;

    }

    class Callback
    implements EntityInLevelCallback {
        private final T entity;
        private long currentSectionKey;
        private EntitySection<T> currentSection;
        final /* synthetic */ PersistentEntitySectionManager this$0;

        /*
         * WARNING - Possible parameter corruption
         * WARNING - void declaration
         */
        Callback(T t, long l2, EntitySection<T> entitySection) {
            void var3_3;
            void var2_2;
            this.this$0 = (PersistentEntitySectionManager)l;
            this.entity = var2_2;
            this.currentSectionKey = var3_3;
            this.currentSection = (EntitySection)l2;
        }

        @Override
        public void onMove() {
            BlockPos blockPos = this.entity.blockPosition();
            long l = SectionPos.asLong(blockPos);
            if (l != this.currentSectionKey) {
                Visibility visibility = this.currentSection.getStatus();
                if (!this.currentSection.remove(this.entity)) {
                    LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), l});
                }
                this.this$0.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection entitySection = this.this$0.sectionStorage.getOrCreateSection(l);
                entitySection.add(this.entity);
                this.currentSection = entitySection;
                this.currentSectionKey = l;
                this.updateStatus(visibility, entitySection.getStatus());
            }
        }

        private void updateStatus(Visibility visibility, Visibility visibility2) {
            Visibility visibility3;
            Visibility visibility4 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility);
            if (visibility4 == (visibility3 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility2))) {
                if (visibility3.isAccessible()) {
                    this.this$0.callbacks.onSectionChange(this.entity);
                }
                return;
            }
            boolean bl = visibility4.isAccessible();
            boolean bl2 = visibility3.isAccessible();
            if (bl && !bl2) {
                this.this$0.stopTracking(this.entity);
            } else if (!bl && bl2) {
                this.this$0.startTracking(this.entity);
            }
            boolean bl3 = visibility4.isTicking();
            boolean bl4 = visibility3.isTicking();
            if (bl3 && !bl4) {
                this.this$0.stopTicking(this.entity);
            } else if (!bl3 && bl4) {
                this.this$0.startTicking(this.entity);
            }
            if (bl2) {
                this.this$0.callbacks.onSectionChange(this.entity);
            }
        }

        @Override
        public void onRemove(Entity.RemovalReason removalReason) {
            Visibility visibility;
            if (!this.currentSection.remove(this.entity)) {
                LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), removalReason});
            }
            if ((visibility = PersistentEntitySectionManager.getEffectiveStatus(this.entity, this.currentSection.getStatus())).isTicking()) {
                this.this$0.stopTicking(this.entity);
            }
            if (visibility.isAccessible()) {
                this.this$0.stopTracking(this.entity);
            }
            if (removalReason.shouldDestroy()) {
                this.this$0.callbacks.onDestroyed(this.entity);
            }
            this.this$0.knownUuids.remove(this.entity.getUUID());
            this.entity.setLevelCallback(NULL);
            this.this$0.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}

