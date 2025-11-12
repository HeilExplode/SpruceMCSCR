/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
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
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class BannerBlockEntity
extends BlockEntity
implements Nameable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_PATTERNS = 6;
    private static final String TAG_PATTERNS = "patterns";
    @Nullable
    private Component name;
    private DyeColor baseColor;
    private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;

    public BannerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BANNER, blockPos, blockState);
        this.baseColor = ((AbstractBannerBlock)blockState.getBlock()).getColor();
    }

    public BannerBlockEntity(BlockPos blockPos, BlockState blockState, DyeColor dyeColor) {
        this(blockPos, blockState);
        this.baseColor = dyeColor;
    }

    public void fromItem(ItemStack itemStack, DyeColor dyeColor) {
        this.baseColor = dyeColor;
        this.applyComponentsFromItemStack(itemStack);
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return Component.translatable("block.minecraft.banner");
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.patterns.equals(BannerPatternLayers.EMPTY)) {
            compoundTag.put(TAG_PATTERNS, (Tag)BannerPatternLayers.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), (Object)this.patterns).getOrThrow());
        }
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = BannerBlockEntity.parseCustomNameSafe(compoundTag.getString("CustomName"), provider);
        }
        if (compoundTag.contains(TAG_PATTERNS)) {
            BannerPatternLayers.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), (Object)compoundTag.get(TAG_PATTERNS)).resultOrPartial(string -> LOGGER.error("Failed to parse banner patterns: '{}'", string)).ifPresent(bannerPatternLayers -> {
                this.patterns = bannerPatternLayers;
            });
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    public BannerPatternLayers getPatterns() {
        return this.patterns;
    }

    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(BannerBlock.byColor(this.baseColor));
        itemStack.applyComponents(this.collectComponents());
        return itemStack;
    }

    public DyeColor getBaseColor() {
        return this.baseColor;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        this.patterns = dataComponentInput.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        this.name = dataComponentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.BANNER_PATTERNS, this.patterns);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        compoundTag.remove(TAG_PATTERNS);
        compoundTag.remove("CustomName");
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

