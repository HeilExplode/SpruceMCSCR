/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SkullBlockEntity
extends BlockEntity {
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    private static final String TAG_CUSTOM_NAME = "custom_name";
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static Executor mainThreadExecutor;
    @Nullable
    private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> profileCacheByName;
    @Nullable
    private static LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> profileCacheById;
    public static final Executor CHECKED_MAIN_THREAD_EXECUTOR;
    @Nullable
    private ResolvableProfile owner;
    @Nullable
    private ResourceLocation noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    @Nullable
    private Component customName;

    public SkullBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SKULL, blockPos, blockState);
    }

    public static void setup(final Services services, Executor executor) {
        mainThreadExecutor = executor;
        final BooleanSupplier booleanSupplier = () -> profileCacheById == null;
        profileCacheByName = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>(){

            public CompletableFuture<Optional<GameProfile>> load(String string) {
                return SkullBlockEntity.fetchProfileByName(string, services);
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((String)object);
            }
        });
        profileCacheById = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<UUID, CompletableFuture<Optional<GameProfile>>>(){

            public CompletableFuture<Optional<GameProfile>> load(UUID uUID) {
                return SkullBlockEntity.fetchProfileById(uUID, services, booleanSupplier);
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((UUID)object);
            }
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileByName(String string, Services services) {
        return services.profileCache().getAsync(string).thenCompose(optional -> {
            LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheById;
            if (loadingCache == null || optional.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return ((CompletableFuture)loadingCache.getUnchecked((Object)((GameProfile)optional.get()).getId())).thenApply(optional2 -> optional2.or(() -> optional));
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileById(UUID uUID, Services services, BooleanSupplier booleanSupplier) {
        return CompletableFuture.supplyAsync(() -> {
            if (booleanSupplier.getAsBoolean()) {
                return Optional.empty();
            }
            ProfileResult profileResult = services.sessionService().fetchProfile(uUID, true);
            return Optional.ofNullable(profileResult).map(ProfileResult::profile);
        }, Util.backgroundExecutor());
    }

    public static void clear() {
        mainThreadExecutor = null;
        profileCacheByName = null;
        profileCacheById = null;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (this.owner != null) {
            compoundTag.put(TAG_PROFILE, (Tag)ResolvableProfile.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.owner).getOrThrow());
        }
        if (this.noteBlockSound != null) {
            compoundTag.putString(TAG_NOTE_BLOCK_SOUND, this.noteBlockSound.toString());
        }
        if (this.customName != null) {
            compoundTag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.customName, provider));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (compoundTag.contains(TAG_PROFILE)) {
            ResolvableProfile.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_PROFILE)).resultOrPartial(string -> LOGGER.error("Failed to load profile from player head: {}", string)).ifPresent(this::setOwner);
        }
        if (compoundTag.contains(TAG_NOTE_BLOCK_SOUND, 8)) {
            this.noteBlockSound = ResourceLocation.tryParse(compoundTag.getString(TAG_NOTE_BLOCK_SOUND));
        }
        this.customName = compoundTag.contains(TAG_CUSTOM_NAME, 8) ? SkullBlockEntity.parseCustomNameSafe(compoundTag.getString(TAG_CUSTOM_NAME), provider) : null;
    }

    public static void animation(Level level, BlockPos blockPos, BlockState blockState, SkullBlockEntity skullBlockEntity) {
        if (blockState.hasProperty(SkullBlock.POWERED) && blockState.getValue(SkullBlock.POWERED).booleanValue()) {
            skullBlockEntity.isAnimating = true;
            ++skullBlockEntity.animationTickCount;
        } else {
            skullBlockEntity.isAnimating = false;
        }
    }

    public float getAnimation(float f) {
        if (this.isAnimating) {
            return (float)this.animationTickCount + f;
        }
        return this.animationTickCount;
    }

    @Nullable
    public ResolvableProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    public ResourceLocation getNoteBlockSound() {
        return this.noteBlockSound;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOwner(@Nullable ResolvableProfile resolvableProfile) {
        SkullBlockEntity skullBlockEntity = this;
        synchronized (skullBlockEntity) {
            this.owner = resolvableProfile;
        }
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        if (this.owner == null || this.owner.isResolved()) {
            this.setChanged();
            return;
        }
        this.owner.resolve().thenAcceptAsync(resolvableProfile -> {
            this.owner = resolvableProfile;
            this.setChanged();
        }, CHECKED_MAIN_THREAD_EXECUTOR);
    }

    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String string) {
        LoadingCache<String, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheByName;
        if (loadingCache != null && StringUtil.isValidPlayerName(string)) {
            return (CompletableFuture)loadingCache.getUnchecked((Object)string);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(UUID uUID) {
        LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheById;
        if (loadingCache != null) {
            return (CompletableFuture)loadingCache.getUnchecked((Object)uUID);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        this.setOwner(dataComponentInput.get(DataComponents.PROFILE));
        this.noteBlockSound = dataComponentInput.get(DataComponents.NOTE_BLOCK_SOUND);
        this.customName = dataComponentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.PROFILE, this.owner);
        builder.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        super.removeComponentsFromTag(compoundTag);
        compoundTag.remove(TAG_PROFILE);
        compoundTag.remove(TAG_NOTE_BLOCK_SOUND);
        compoundTag.remove(TAG_CUSTOM_NAME);
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    static {
        CHECKED_MAIN_THREAD_EXECUTOR = runnable -> {
            Executor executor = mainThreadExecutor;
            if (executor != null) {
                executor.execute(runnable);
            }
        };
    }
}

