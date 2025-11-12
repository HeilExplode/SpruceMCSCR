/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected final BlockPos worldPosition;
    protected boolean remove;
    private BlockState blockState;
    private DataComponentMap components = DataComponentMap.EMPTY;

    public BlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        this.type = blockEntityType;
        this.worldPosition = blockPos.immutable();
        this.validateBlockState(blockState);
        this.blockState = blockState;
    }

    private void validateBlockState(BlockState blockState) {
        if (!this.isValidBlockState(blockState)) {
            throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + String.valueOf(this.worldPosition) + ", got " + String.valueOf(blockState));
        }
    }

    public boolean isValidBlockState(BlockState blockState) {
        return this.type.isValid(blockState);
    }

    public static BlockPos getPosFromTag(CompoundTag compoundTag) {
        return new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
    }

    public final void loadWithComponents(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.loadAdditional(compoundTag, provider);
        ComponentHelper.COMPONENTS_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), (Object)compoundTag).resultOrPartial(string -> LOGGER.warn("Failed to load components: {}", string)).ifPresent(dataComponentMap -> {
            this.components = dataComponentMap;
        });
    }

    public final void loadCustomOnly(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.loadAdditional(compoundTag, provider);
    }

    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
    }

    public final CompoundTag saveWithFullMetadata(HolderLookup.Provider provider) {
        CompoundTag compoundTag = this.saveWithoutMetadata(provider);
        this.saveMetadata(compoundTag);
        return compoundTag;
    }

    public final CompoundTag saveWithId(HolderLookup.Provider provider) {
        CompoundTag compoundTag = this.saveWithoutMetadata(provider);
        this.saveId(compoundTag);
        return compoundTag;
    }

    public final CompoundTag saveWithoutMetadata(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        this.saveAdditional(compoundTag, provider);
        ComponentHelper.COMPONENTS_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), (Object)this.components).resultOrPartial(string -> LOGGER.warn("Failed to save components: {}", string)).ifPresent(tag -> compoundTag.merge((CompoundTag)tag));
        return compoundTag;
    }

    public final CompoundTag saveCustomOnly(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        this.saveAdditional(compoundTag, provider);
        return compoundTag;
    }

    public final CompoundTag saveCustomAndMetadata(HolderLookup.Provider provider) {
        CompoundTag compoundTag = this.saveCustomOnly(provider);
        this.saveMetadata(compoundTag);
        return compoundTag;
    }

    private void saveId(CompoundTag compoundTag) {
        ResourceLocation resourceLocation = BlockEntityType.getKey(this.getType());
        if (resourceLocation == null) {
            throw new RuntimeException(String.valueOf(this.getClass()) + " is missing a mapping! This is a bug!");
        }
        compoundTag.putString("id", resourceLocation.toString());
    }

    public static void addEntityType(CompoundTag compoundTag, BlockEntityType<?> blockEntityType) {
        compoundTag.putString("id", BlockEntityType.getKey(blockEntityType).toString());
    }

    public void saveToItem(ItemStack itemStack, HolderLookup.Provider provider) {
        CompoundTag compoundTag = this.saveCustomOnly(provider);
        this.removeComponentsFromTag(compoundTag);
        BlockItem.setBlockEntityData(itemStack, this.getType(), compoundTag);
        itemStack.applyComponents(this.collectComponents());
    }

    private void saveMetadata(CompoundTag compoundTag) {
        this.saveId(compoundTag);
        compoundTag.putInt("x", this.worldPosition.getX());
        compoundTag.putInt("y", this.worldPosition.getY());
        compoundTag.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static BlockEntity loadStatic(BlockPos blockPos, BlockState blockState, CompoundTag compoundTag, HolderLookup.Provider provider) {
        String string = compoundTag.getString("id");
        ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
        if (resourceLocation == null) {
            LOGGER.error("Block entity has invalid type: {}", (Object)string);
            return null;
        }
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourceLocation).map(blockEntityType -> {
            try {
                return blockEntityType.create(blockPos, blockState);
            }
            catch (Throwable throwable) {
                LOGGER.error("Failed to create block entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).map(blockEntity -> {
            try {
                blockEntity.loadWithComponents(compoundTag, provider);
                return blockEntity;
            }
            catch (Throwable throwable) {
                LOGGER.error("Failed to load data for block entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", (Object)string);
            return null;
        });
    }

    public void setChanged() {
        if (this.level != null) {
            BlockEntity.setChanged(this.level, this.worldPosition, this.blockState);
        }
    }

    protected static void setChanged(Level level, BlockPos blockPos, BlockState blockState) {
        level.blockEntityChanged(blockPos);
        if (!blockState.isAir()) {
            level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
        }
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public boolean triggerEvent(int n, int n2) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Name", this::getNameForReporting);
        if (this.level == null) {
            return;
        }
        CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, this.worldPosition, this.getBlockState());
        CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
    }

    private String getNameForReporting() {
        return String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType())) + " // " + this.getClass().getCanonicalName();
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setBlockState(BlockState blockState) {
        this.validateBlockState(blockState);
        this.blockState = blockState;
    }

    protected void applyImplicitComponents(DataComponentInput dataComponentInput) {
    }

    public final void applyComponentsFromItemStack(ItemStack itemStack) {
        this.applyComponents(itemStack.getPrototype(), itemStack.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch) {
        final HashSet<DataComponentType<CustomData>> hashSet = new HashSet<DataComponentType<CustomData>>();
        hashSet.add(DataComponents.BLOCK_ENTITY_DATA);
        final PatchedDataComponentMap patchedDataComponentMap = PatchedDataComponentMap.fromPatch(dataComponentMap, dataComponentPatch);
        this.applyImplicitComponents(new DataComponentInput(){

            @Override
            @Nullable
            public <T> T get(DataComponentType<T> dataComponentType) {
                hashSet.add(dataComponentType);
                return patchedDataComponentMap.get(dataComponentType);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T t) {
                hashSet.add(dataComponentType);
                return patchedDataComponentMap.getOrDefault(dataComponentType, t);
            }
        });
        DataComponentPatch dataComponentPatch2 = dataComponentPatch.forget(hashSet::contains);
        this.components = dataComponentPatch2.split().added();
    }

    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
    }

    @Deprecated
    public void removeComponentsFromTag(CompoundTag compoundTag) {
    }

    public final DataComponentMap collectComponents() {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        builder.addAll(this.components);
        this.collectImplicitComponents(builder);
        return builder.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap dataComponentMap) {
        this.components = dataComponentMap;
    }

    @Nullable
    public static Component parseCustomNameSafe(String string, HolderLookup.Provider provider) {
        try {
            return Component.Serializer.fromJson(string, provider);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse custom name from string '{}', discarding", (Object)string, (Object)exception);
            return null;
        }
    }

    static class ComponentHelper {
        public static final Codec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", (Object)DataComponentMap.EMPTY).codec();

        private ComponentHelper() {
        }
    }

    protected static interface DataComponentInput {
        @Nullable
        public <T> T get(DataComponentType<T> var1);

        public <T> T getOrDefault(DataComponentType<? extends T> var1, T var2);
    }
}

