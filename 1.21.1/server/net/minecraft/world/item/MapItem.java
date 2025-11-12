/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.LinkedHashMultiset
 *  com.google.common.collect.Multiset
 *  com.google.common.collect.Multisets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem
extends ComplexItem {
    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;

    public MapItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int n, int n2, byte by, boolean bl, boolean bl2) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapId mapId = MapItem.createNewSavedData(level, n, n2, by, bl, bl2, level.dimension());
        itemStack.set(DataComponents.MAP_ID, mapId);
        return itemStack;
    }

    @Nullable
    public static MapItemSavedData getSavedData(@Nullable MapId mapId, Level level) {
        return mapId == null ? null : level.getMapData(mapId);
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        return MapItem.getSavedData(mapId, level);
    }

    private static MapId createNewSavedData(Level level, int n, int n2, int n3, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        MapItemSavedData mapItemSavedData = MapItemSavedData.createFresh(n, n2, (byte)n3, bl, bl2, resourceKey);
        MapId mapId = level.getFreeMapId();
        level.setMapData(mapId, mapItemSavedData);
        return mapId;
    }

    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        if (level.dimension() != mapItemSavedData.dimension || !(entity instanceof Player)) {
            return;
        }
        int n = 1 << mapItemSavedData.scale;
        int n2 = mapItemSavedData.centerX;
        int n3 = mapItemSavedData.centerZ;
        int n4 = Mth.floor(entity.getX() - (double)n2) / n + 64;
        int n5 = Mth.floor(entity.getZ() - (double)n3) / n + 64;
        int n6 = 128 / n;
        if (level.dimensionType().hasCeiling()) {
            n6 /= 2;
        }
        MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
        ++holdingPlayer.step;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        boolean bl = false;
        for (int i = n4 - n6 + 1; i < n4 + n6; ++i) {
            if ((i & 0xF) != (holdingPlayer.step & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int j = n5 - n6 - 1; j < n5 + n6; ++j) {
                double d2;
                if (i < 0 || j < -1 || i >= 128 || j >= 128) continue;
                int n7 = Mth.square(i - n4) + Mth.square(j - n5);
                boolean bl2 = n7 > (n6 - 2) * (n6 - 2);
                int n8 = (n2 / n + i - 64) * n;
                int n9 = (n3 / n + j - 64) * n;
                LinkedHashMultiset linkedHashMultiset = LinkedHashMultiset.create();
                LevelChunk levelChunk = level.getChunk(SectionPos.blockToSectionCoord(n8), SectionPos.blockToSectionCoord(n9));
                if (levelChunk.isEmpty()) continue;
                int n10 = 0;
                double d3 = 0.0;
                if (level.dimensionType().hasCeiling()) {
                    var27_26 = n8 + n9 * 231871;
                    if (((var27_26 = var27_26 * var27_26 * 31287121 + var27_26 * 11) >> 20 & 1) == 0) {
                        linkedHashMultiset.add((Object)Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                    } else {
                        linkedHashMultiset.add((Object)Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                    }
                    d3 = 100.0;
                } else {
                    for (var27_26 = 0; var27_26 < n; ++var27_26) {
                        for (int k = 0; k < n; ++k) {
                            BlockState blockState;
                            mutableBlockPos.set(n8 + var27_26, 0, n9 + k);
                            int n11 = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, mutableBlockPos.getX(), mutableBlockPos.getZ()) + 1;
                            if (n11 > level.getMinBuildHeight() + 1) {
                                do {
                                    mutableBlockPos.setY(--n11);
                                } while ((blockState = levelChunk.getBlockState(mutableBlockPos)).getMapColor(level, mutableBlockPos) == MapColor.NONE && n11 > level.getMinBuildHeight());
                                if (n11 > level.getMinBuildHeight() && !blockState.getFluidState().isEmpty()) {
                                    BlockState blockState2;
                                    int n12 = n11 - 1;
                                    mutableBlockPos2.set(mutableBlockPos);
                                    do {
                                        mutableBlockPos2.setY(n12--);
                                        blockState2 = levelChunk.getBlockState(mutableBlockPos2);
                                        ++n10;
                                    } while (n12 > level.getMinBuildHeight() && !blockState2.getFluidState().isEmpty());
                                    blockState = this.getCorrectStateForFluidBlock(level, blockState, mutableBlockPos);
                                }
                            } else {
                                blockState = Blocks.BEDROCK.defaultBlockState();
                            }
                            mapItemSavedData.checkBanners(level, mutableBlockPos.getX(), mutableBlockPos.getZ());
                            d3 += (double)n11 / (double)(n * n);
                            linkedHashMultiset.add((Object)blockState.getMapColor(level, mutableBlockPos));
                        }
                    }
                }
                MapColor mapColor = (MapColor)Iterables.getFirst((Iterable)Multisets.copyHighestCountFirst((Multiset)linkedHashMultiset), (Object)MapColor.NONE);
                MapColor.Brightness brightness = mapColor == MapColor.WATER ? ((d2 = (double)(n10 /= n * n) * 0.1 + (double)(i + j & 1) * 0.2) < 0.5 ? MapColor.Brightness.HIGH : (d2 > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL)) : ((d2 = (d3 - d) * 4.0 / (double)(n + 4) + ((double)(i + j & 1) - 0.5) * 0.4) > 0.6 ? MapColor.Brightness.HIGH : (d2 < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL));
                d = d3;
                if (j < 0 || n7 >= n6 * n6 || bl2 && (i + j & 1) == 0) continue;
                bl |= mapItemSavedData.updateColor(i, j, mapColor.getPackedId(brightness));
            }
        }
    }

    private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP)) {
            return fluidState.createLegacyBlock();
        }
        return blockState;
    }

    private static boolean isBiomeWatery(boolean[] blArray, int n, int n2) {
        return blArray[n2 * 128 + n];
    }

    public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
        int n;
        int n2;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, (Level)serverLevel);
        if (mapItemSavedData == null) {
            return;
        }
        if (serverLevel.dimension() != mapItemSavedData.dimension) {
            return;
        }
        int n3 = 1 << mapItemSavedData.scale;
        int n4 = mapItemSavedData.centerX;
        int n5 = mapItemSavedData.centerZ;
        boolean[] blArray = new boolean[16384];
        int n6 = n4 / n3 - 64;
        int n7 = n5 / n3 - 64;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n2 = 0; n2 < 128; ++n2) {
            for (n = 0; n < 128; ++n) {
                Holder<Biome> holder = serverLevel.getBiome(mutableBlockPos.set((n6 + n) * n3, 0, (n7 + n2) * n3));
                blArray[n2 * 128 + n] = holder.is(BiomeTags.WATER_ON_MAP_OUTLINES);
            }
        }
        for (n2 = 1; n2 < 127; ++n2) {
            for (n = 1; n < 127; ++n) {
                int n8 = 0;
                for (int i = -1; i < 2; ++i) {
                    for (int j = -1; j < 2; ++j) {
                        if (i == 0 && j == 0 || !MapItem.isBiomeWatery(blArray, n2 + i, n + j)) continue;
                        ++n8;
                    }
                }
                MapColor.Brightness brightness = MapColor.Brightness.LOWEST;
                MapColor mapColor = MapColor.NONE;
                if (MapItem.isBiomeWatery(blArray, n2, n)) {
                    mapColor = MapColor.COLOR_ORANGE;
                    if (n8 > 7 && n % 2 == 0) {
                        switch ((n2 + (int)(Mth.sin((float)n + 0.0f) * 7.0f)) / 8 % 5) {
                            case 0: 
                            case 4: {
                                brightness = MapColor.Brightness.LOW;
                                break;
                            }
                            case 1: 
                            case 3: {
                                brightness = MapColor.Brightness.NORMAL;
                                break;
                            }
                            case 2: {
                                brightness = MapColor.Brightness.HIGH;
                            }
                        }
                    } else if (n8 > 7) {
                        mapColor = MapColor.NONE;
                    } else if (n8 > 5) {
                        brightness = MapColor.Brightness.NORMAL;
                    } else if (n8 > 3) {
                        brightness = MapColor.Brightness.LOW;
                    } else if (n8 > 1) {
                        brightness = MapColor.Brightness.LOW;
                    }
                } else if (n8 > 0) {
                    mapColor = MapColor.COLOR_BROWN;
                    brightness = n8 > 3 ? MapColor.Brightness.NORMAL : MapColor.Brightness.LOWEST;
                }
                if (mapColor == MapColor.NONE) continue;
                mapItemSavedData.setColor(n2, n, mapColor.getPackedId(brightness));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int n, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData == null) {
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            mapItemSavedData.tickCarriedBy(player, itemStack);
        }
        if (!mapItemSavedData.locked && (bl || entity instanceof Player && ((Player)entity).getOffhandItem() == itemStack)) {
            this.update(level, entity, mapItemSavedData);
        }
    }

    @Override
    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, level);
        if (mapItemSavedData != null) {
            return mapItemSavedData.getUpdatePacket(mapId, player);
        }
        return null;
    }

    @Override
    public void onCraftedPostProcess(ItemStack itemStack, Level level) {
        MapPostProcessing mapPostProcessing = itemStack.remove(DataComponents.MAP_POST_PROCESSING);
        if (mapPostProcessing == null) {
            return;
        }
        switch (mapPostProcessing) {
            case LOCK: {
                MapItem.lockMap(level, itemStack);
                break;
            }
            case SCALE: {
                MapItem.scaleMap(itemStack, level);
            }
        }
    }

    private static void scaleMap(ItemStack itemStack, Level level) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            MapId mapId = level.getFreeMapId();
            level.setMapData(mapId, mapItemSavedData.scaled());
            itemStack.set(DataComponents.MAP_ID, mapId);
        }
    }

    public static void lockMap(Level level, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            MapId mapId = level.getFreeMapId();
            MapItemSavedData mapItemSavedData2 = mapItemSavedData.locked();
            level.setMapData(mapId, mapItemSavedData2);
            itemStack.set(DataComponents.MAP_ID, mapId);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData mapItemSavedData = mapId != null ? tooltipContext.mapData(mapId) : null;
        MapPostProcessing mapPostProcessing = itemStack.get(DataComponents.MAP_POST_PROCESSING);
        if (mapItemSavedData != null && (mapItemSavedData.locked || mapPostProcessing == MapPostProcessing.LOCK)) {
            list.add(Component.translatable("filled_map.locked", mapId.id()).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipFlag.isAdvanced()) {
            if (mapItemSavedData != null) {
                if (mapPostProcessing == null) {
                    list.add(MapItem.getTooltipForId(mapId));
                }
                byte by = mapPostProcessing == MapPostProcessing.SCALE ? (byte)1 : 0;
                int n = Math.min(mapItemSavedData.scale + by, 4);
                list.add(Component.translatable("filled_map.scale", 1 << n).withStyle(ChatFormatting.GRAY));
                list.add(Component.translatable("filled_map.level", n, 4).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static Component getTooltipForId(MapId mapId) {
        return Component.translatable("filled_map.id", mapId.id()).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockState blockState = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            MapItemSavedData mapItemSavedData;
            if (!useOnContext.getLevel().isClientSide && (mapItemSavedData = MapItem.getSavedData(useOnContext.getItemInHand(), useOnContext.getLevel())) != null && !mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.sidedSuccess(useOnContext.getLevel().isClientSide);
        }
        return super.useOn(useOnContext);
    }
}

