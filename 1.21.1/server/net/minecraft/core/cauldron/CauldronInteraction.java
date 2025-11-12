/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CauldronInteraction {
    public static final Map<String, InteractionMap> INTERACTIONS = new Object2ObjectArrayMap();
    public static final Codec<InteractionMap> CODEC = Codec.stringResolver(InteractionMap::name, INTERACTIONS::get);
    public static final InteractionMap EMPTY = CauldronInteraction.newInteractionMap("empty");
    public static final InteractionMap WATER = CauldronInteraction.newInteractionMap("water");
    public static final InteractionMap LAVA = CauldronInteraction.newInteractionMap("lava");
    public static final InteractionMap POWDER_SNOW = CauldronInteraction.newInteractionMap("powder_snow");
    public static final CauldronInteraction FILL_WATER = (blockState, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
    public static final CauldronInteraction FILL_LAVA = (blockState, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
    public static final CauldronInteraction FILL_POWDER_SNOW = (blockState, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, (BlockState)Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    public static final CauldronInteraction SHULKER_BOX = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
        Block block = Block.byItem(itemStack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack itemStack2 = itemStack.transmuteCopy(Blocks.SHULKER_BOX, 1);
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
            player.awardStat(Stats.CLEAN_SHULKER_BOX);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    };
    public static final CauldronInteraction BANNER = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
        BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (bannerPatternLayers.layers().isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack itemStack2 = itemStack.copyWithCount(1);
            itemStack2.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers.removeLast());
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
            player.awardStat(Stats.CLEAN_BANNER);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    };
    public static final CauldronInteraction DYED_ITEM = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
        if (!itemStack.is(ItemTags.DYEABLE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!itemStack.has(DataComponents.DYED_COLOR)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            itemStack.remove(DataComponents.DYED_COLOR);
            player.awardStat(Stats.CLEAN_ARMOR);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    };

    public static InteractionMap newInteractionMap(String string) {
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        object2ObjectOpenHashMap.defaultReturnValue((blockState, level, blockPos, player, interactionHand, itemStack) -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        InteractionMap interactionMap = new InteractionMap(string, (Map<Item, CauldronInteraction>)object2ObjectOpenHashMap);
        INTERACTIONS.put(string, interactionMap);
        return interactionMap;
    }

    public ItemInteractionResult interact(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, ItemStack var6);

    public static void bootStrap() {
        Map<Item, CauldronInteraction> map = EMPTY.map();
        CauldronInteraction.addDefaultInteractions(map);
        map.put(Items.POTION, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null || !potionContents.is(Potions.WATER)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                Item item = itemStack.getItem();
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
                level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        });
        Map<Item, CauldronInteraction> map2 = WATER.map();
        CauldronInteraction.addDefaultInteractions(map2);
        map2.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.WATER_BUCKET), blockState -> blockState.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        map2.put(Items.GLASS_BOTTLE, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide) {
                Item item = itemStack.getItem();
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        });
        map2.put(Items.POTION, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (blockState.getValue(LayeredCauldronBlock.LEVEL) == 3) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null || !potionContents.is(Potions.WATER)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.cycle(LayeredCauldronBlock.LEVEL));
                level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        });
        map2.put(Items.LEATHER_BOOTS, DYED_ITEM);
        map2.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
        map2.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
        map2.put(Items.LEATHER_HELMET, DYED_ITEM);
        map2.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
        map2.put(Items.WOLF_ARMOR, DYED_ITEM);
        map2.put(Items.WHITE_BANNER, BANNER);
        map2.put(Items.GRAY_BANNER, BANNER);
        map2.put(Items.BLACK_BANNER, BANNER);
        map2.put(Items.BLUE_BANNER, BANNER);
        map2.put(Items.BROWN_BANNER, BANNER);
        map2.put(Items.CYAN_BANNER, BANNER);
        map2.put(Items.GREEN_BANNER, BANNER);
        map2.put(Items.LIGHT_BLUE_BANNER, BANNER);
        map2.put(Items.LIGHT_GRAY_BANNER, BANNER);
        map2.put(Items.LIME_BANNER, BANNER);
        map2.put(Items.MAGENTA_BANNER, BANNER);
        map2.put(Items.ORANGE_BANNER, BANNER);
        map2.put(Items.PINK_BANNER, BANNER);
        map2.put(Items.PURPLE_BANNER, BANNER);
        map2.put(Items.RED_BANNER, BANNER);
        map2.put(Items.YELLOW_BANNER, BANNER);
        map2.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
        map2.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
        Map<Item, CauldronInteraction> map3 = LAVA.map();
        map3.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.LAVA_BUCKET), blockState -> true, SoundEvents.BUCKET_FILL_LAVA));
        CauldronInteraction.addDefaultInteractions(map3);
        Map<Item, CauldronInteraction> map4 = POWDER_SNOW.map();
        map4.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.POWDER_SNOW_BUCKET), blockState -> blockState.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
        CauldronInteraction.addDefaultInteractions(map4);
    }

    public static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
        map.put(Items.LAVA_BUCKET, FILL_LAVA);
        map.put(Items.WATER_BUCKET, FILL_WATER);
        map.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
    }

    public static ItemInteractionResult fillBucket(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack, ItemStack itemStack2, Predicate<BlockState> predicate, SoundEvent soundEvent) {
        if (!predicate.test(blockState)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            Item item = itemStack.getItem();
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static ItemInteractionResult emptyBucket(Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack, BlockState blockState, SoundEvent soundEvent) {
        if (!level.isClientSide) {
            Item item = itemStack.getItem();
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(blockPos, blockState);
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public record InteractionMap(String name, Map<Item, CauldronInteraction> map) {
    }
}

