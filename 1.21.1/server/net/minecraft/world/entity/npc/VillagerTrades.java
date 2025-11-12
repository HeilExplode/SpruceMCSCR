/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.TradeRebalanceEnchantmentProviders;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.tuple.Pair;

public class VillagerTrades {
    private static final int DEFAULT_SUPPLY = 12;
    private static final int COMMON_ITEMS_SUPPLY = 16;
    private static final int UNCOMMON_ITEMS_SUPPLY = 3;
    private static final int XP_LEVEL_1_SELL = 1;
    private static final int XP_LEVEL_1_BUY = 2;
    private static final int XP_LEVEL_2_SELL = 5;
    private static final int XP_LEVEL_2_BUY = 10;
    private static final int XP_LEVEL_3_SELL = 10;
    private static final int XP_LEVEL_3_BUY = 20;
    private static final int XP_LEVEL_4_SELL = 15;
    private static final int XP_LEVEL_4_BUY = 30;
    private static final int XP_LEVEL_5_TRADE = 30;
    private static final float LOW_TIER_PRICE_MULTIPLIER = 0.05f;
    private static final float HIGH_TIER_PRICE_MULTIPLIER = 0.2f;
    public static final Map<VillagerProfession, Int2ObjectMap<ItemListing[]>> TRADES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(VillagerProfession.FARMER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.WHEAT, 20, 16, 2), new EmeraldForItems(Items.POTATO, 26, 16, 2), new EmeraldForItems(Items.CARROT, 22, 16, 2), new EmeraldForItems(Items.BEETROOT, 15, 16, 2), new ItemsForEmeralds(Items.BREAD, 1, 6, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Blocks.PUMPKIN, 6, 12, 10), new ItemsForEmeralds(Items.PUMPKIN_PIE, 1, 4, 5), new ItemsForEmeralds(Items.APPLE, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new ItemsForEmeralds(Items.COOKIE, 3, 18, 10), new EmeraldForItems(Blocks.MELON, 4, 12, 20)}, (Object)4, (Object)new ItemListing[]{new ItemsForEmeralds(Blocks.CAKE, 1, 1, 12, 15), new SuspiciousStewForEmerald(MobEffects.NIGHT_VISION, 100, 15), new SuspiciousStewForEmerald(MobEffects.JUMP, 160, 15), new SuspiciousStewForEmerald(MobEffects.WEAKNESS, 140, 15), new SuspiciousStewForEmerald(MobEffects.BLINDNESS, 120, 15), new SuspiciousStewForEmerald(MobEffects.POISON, 280, 15), new SuspiciousStewForEmerald(MobEffects.SATURATION, 7, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.GOLDEN_CARROT, 3, 3, 30), new ItemsForEmeralds(Items.GLISTERING_MELON_SLICE, 4, 3, 30)})));
        hashMap.put(VillagerProfession.FISHERMAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.STRING, 20, 16, 2), new EmeraldForItems(Items.COAL, 10, 16, 2), new ItemsAndEmeraldsToItems((ItemLike)Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05f), new ItemsForEmeralds(Items.COD_BUCKET, 3, 1, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.COD, 15, 16, 10), new ItemsAndEmeraldsToItems((ItemLike)Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05f), new ItemsForEmeralds(Items.CAMPFIRE, 2, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.SALMON, 13, 16, 20), new EnchantedItemForEmeralds(Items.FISHING_ROD, 3, 3, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TROPICAL_FISH, 6, 12, 30)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.PUFFERFISH, 4, 12, 30), new EmeraldsForVillagerTypeItem(1, 12, 30, (Map<VillagerType, Item>)ImmutableMap.builder().put((Object)VillagerType.PLAINS, (Object)Items.OAK_BOAT).put((Object)VillagerType.TAIGA, (Object)Items.SPRUCE_BOAT).put((Object)VillagerType.SNOW, (Object)Items.SPRUCE_BOAT).put((Object)VillagerType.DESERT, (Object)Items.JUNGLE_BOAT).put((Object)VillagerType.JUNGLE, (Object)Items.JUNGLE_BOAT).put((Object)VillagerType.SAVANNA, (Object)Items.ACACIA_BOAT).put((Object)VillagerType.SWAMP, (Object)Items.DARK_OAK_BOAT).build())})));
        hashMap.put(VillagerProfession.SHEPHERD, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Blocks.WHITE_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.BROWN_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.BLACK_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.GRAY_WOOL, 18, 16, 2), new ItemsForEmeralds(Items.SHEARS, 2, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.WHITE_DYE, 12, 16, 10), new EmeraldForItems(Items.GRAY_DYE, 12, 16, 10), new EmeraldForItems(Items.BLACK_DYE, 12, 16, 10), new EmeraldForItems(Items.LIGHT_BLUE_DYE, 12, 16, 10), new EmeraldForItems(Items.LIME_DYE, 12, 16, 10), new ItemsForEmeralds(Blocks.WHITE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.ORANGE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.MAGENTA_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.YELLOW_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIME_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.PINK_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.GRAY_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.CYAN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.PURPLE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BLUE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BROWN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.GREEN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.RED_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BLACK_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.WHITE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.ORANGE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.MAGENTA_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.YELLOW_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIME_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.PINK_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.GRAY_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.CYAN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.PURPLE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BLUE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BROWN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.GREEN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.RED_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BLACK_CARPET, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.YELLOW_DYE, 12, 16, 20), new EmeraldForItems(Items.LIGHT_GRAY_DYE, 12, 16, 20), new EmeraldForItems(Items.ORANGE_DYE, 12, 16, 20), new EmeraldForItems(Items.RED_DYE, 12, 16, 20), new EmeraldForItems(Items.PINK_DYE, 12, 16, 20), new ItemsForEmeralds(Blocks.WHITE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.YELLOW_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.RED_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BLACK_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BLUE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BROWN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.CYAN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.GRAY_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.GREEN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIME_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.MAGENTA_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.ORANGE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.PINK_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.PURPLE_BED, 3, 1, 12, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.BROWN_DYE, 12, 16, 30), new EmeraldForItems(Items.PURPLE_DYE, 12, 16, 30), new EmeraldForItems(Items.BLUE_DYE, 12, 16, 30), new EmeraldForItems(Items.GREEN_DYE, 12, 16, 30), new EmeraldForItems(Items.MAGENTA_DYE, 12, 16, 30), new EmeraldForItems(Items.CYAN_DYE, 12, 16, 30), new ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.RED_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.PAINTING, 2, 3, 30)})));
        hashMap.put(VillagerProfession.FLETCHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.STICK, 32, 16, 2), new ItemsForEmeralds(Items.ARROW, 1, 16, 1), new ItemsAndEmeraldsToItems((ItemLike)Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 26, 12, 10), new ItemsForEmeralds(Items.BOW, 2, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.STRING, 14, 16, 20), new ItemsForEmeralds(Items.CROSSBOW, 3, 1, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.FEATHER, 24, 16, 30), new EnchantedItemForEmeralds(Items.BOW, 2, 3, 15)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.TRIPWIRE_HOOK, 8, 12, 30), new EnchantedItemForEmeralds(Items.CROSSBOW, 3, 3, 15), new TippedArrowForItemsAndEmeralds(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)})));
        hashMap.put(VillagerProfession.LIBRARIAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), new EnchantBookForEmeralds(1, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put((Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.BOOK, 4, 12, 10), new EnchantBookForEmeralds(5, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.LANTERN, 1, 1, 5)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.INK_SAC, 5, 12, 20), new EnchantBookForEmeralds(10, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.GLASS, 1, 4, 10)}).put((Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30), new EnchantBookForEmeralds(15, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.CLOCK, 5, 1, 15), new ItemsForEmeralds(Items.COMPASS, 4, 1, 15)}).put((Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)}).build()));
        hashMap.put(VillagerProfession.CARTOGRAPHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), new ItemsForEmeralds(Items.MAP, 7, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.GLASS_PANE, 11, 16, 10), new TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.COMPASS, 1, 12, 20), new TreasureMapForEmeralds(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 12, 10), new TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, (Object)4, (Object)new ItemListing[]{new ItemsForEmeralds(Items.ITEM_FRAME, 7, 1, 15), new ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.RED_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.GLOBE_BANNER_PATTERN, 8, 1, 30)})));
        hashMap.put(VillagerProfession.CLERIC, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.ROTTEN_FLESH, 32, 16, 2), new ItemsForEmeralds(Items.REDSTONE, 1, 2, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.GOLD_INGOT, 3, 12, 10), new ItemsForEmeralds(Items.LAPIS_LAZULI, 1, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.RABBIT_FOOT, 2, 12, 20), new ItemsForEmeralds(Blocks.GLOWSTONE, 4, 1, 12, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30), new EmeraldForItems(Items.GLASS_BOTTLE, 9, 12, 30), new ItemsForEmeralds(Items.ENDER_PEARL, 5, 1, 15)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.NETHER_WART, 22, 12, 30), new ItemsForEmeralds(Items.EXPERIENCE_BOTTLE, 3, 1, 30)})));
        hashMap.put(VillagerProfession.ARMORER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20), new EmeraldForItems(Items.DIAMOND, 1, 12, 20), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_HELMET, 8, 3, 30, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.WEAPONSMITH, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2f), new EnchantedItemForEmeralds(Items.IRON_SWORD, 2, 3, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 24, 12, 20)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DIAMOND, 1, 12, 30), new EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_SWORD, 8, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.TOOLSMITH, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 30, 12, 20), new EnchantedItemForEmeralds(Items.IRON_AXE, 1, 3, 10, 0.2f), new EnchantedItemForEmeralds(Items.IRON_SHOVEL, 2, 3, 10, 0.2f), new EnchantedItemForEmeralds(Items.IRON_PICKAXE, 3, 3, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DIAMOND, 1, 12, 30), new EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.BUTCHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.CHICKEN, 14, 16, 2), new EmeraldForItems(Items.PORKCHOP, 7, 16, 2), new EmeraldForItems(Items.RABBIT, 4, 16, 2), new ItemsForEmeralds(Items.RABBIT_STEW, 1, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new ItemsForEmeralds(Items.COOKED_CHICKEN, 1, 8, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.MUTTON, 7, 16, 20), new EmeraldForItems(Items.BEEF, 10, 16, 20)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.SWEET_BERRIES, 10, 12, 30)})));
        hashMap.put(VillagerProfession.LEATHERWORKER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.LEATHER, 6, 16, 2), new DyedArmorForEmeralds(Items.LEATHER_LEGGINGS, 3), new DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 26, 12, 10), new DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 5), new DyedArmorForEmeralds(Items.LEATHER_BOOTS, 4, 12, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.RABBIT_HIDE, 9, 12, 20), new DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30), new DyedArmorForEmeralds(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2f), new DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 30)})));
        hashMap.put(VillagerProfession.MASON, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.CLAY_BALL, 10, 16, 2), new ItemsForEmeralds(Items.BRICK, 1, 10, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Blocks.STONE, 20, 16, 10), new ItemsForEmeralds(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Blocks.GRANITE, 16, 16, 20), new EmeraldForItems(Blocks.ANDESITE, 16, 16, 20), new EmeraldForItems(Blocks.DIORITE, 16, 16, 20), new ItemsForEmeralds(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_DIORITE, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.QUARTZ, 12, 12, 30), new ItemsForEmeralds(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.RED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new ItemsForEmeralds(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)})));
    });
    public static final Int2ObjectMap<ItemListing[]> WANDERING_TRADER_TRADES = VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1), new ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1), new ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1), new ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new ItemsForEmeralds(Items.FERN, 1, 1, 12, 1), new ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1), new ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1), new ItemsForEmeralds(Items.KELP, 3, 1, 12, 1), new ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1), new ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1), new ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1), new ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1), new ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1), new ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1), new ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1), new ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1), new ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.VINE, 1, 1, 12, 1), new ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 1, 12, 1), new ItemsForEmeralds(Items.RED_MUSHROOM, 1, 1, 12, 1), new ItemsForEmeralds(Items.LILY_PAD, 1, 2, 5, 1), new ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new ItemsForEmeralds(Items.SAND, 1, 8, 8, 1), new ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1), new ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1), new ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1)}, (Object)2, (Object)new ItemListing[]{new ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1), new ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1), new ItemsForEmeralds(Items.PACKED_ICE, 3, 1, 6, 1), new ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1), new ItemsForEmeralds(Items.GUNPOWDER, 1, 1, 8, 1), new ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1)}));
    private static final TreasureMapForEmeralds DESERT_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_DESERT_VILLAGE_MAPS, "filled_map.village_desert", MapDecorationTypes.DESERT_VILLAGE, 12, 5);
    private static final TreasureMapForEmeralds SAVANNA_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_SAVANNA_VILLAGE_MAPS, "filled_map.village_savanna", MapDecorationTypes.SAVANNA_VILLAGE, 12, 5);
    private static final TreasureMapForEmeralds PLAINS_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_PLAINS_VILLAGE_MAPS, "filled_map.village_plains", MapDecorationTypes.PLAINS_VILLAGE, 12, 5);
    private static final TreasureMapForEmeralds TAIGA_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_TAIGA_VILLAGE_MAPS, "filled_map.village_taiga", MapDecorationTypes.TAIGA_VILLAGE, 12, 5);
    private static final TreasureMapForEmeralds SNOWY_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_SNOWY_VILLAGE_MAPS, "filled_map.village_snowy", MapDecorationTypes.SNOWY_VILLAGE, 12, 5);
    private static final TreasureMapForEmeralds JUNGLE_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_JUNGLE_EXPLORER_MAPS, "filled_map.explorer_jungle", MapDecorationTypes.JUNGLE_TEMPLE, 12, 5);
    private static final TreasureMapForEmeralds SWAMP_MAP = new TreasureMapForEmeralds(8, StructureTags.ON_SWAMP_EXPLORER_MAPS, "filled_map.explorer_swamp", MapDecorationTypes.SWAMP_HUT, 12, 5);
    public static final Map<VillagerProfession, Int2ObjectMap<ItemListing[]>> EXPERIMENTAL_TRADES = Map.of(VillagerProfession.LIBRARIAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), VillagerTrades.commonBooks(1), new ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put((Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.BOOK, 4, 12, 10), VillagerTrades.commonBooks(5), new ItemsForEmeralds(Items.LANTERN, 1, 1, 5)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.INK_SAC, 5, 12, 20), VillagerTrades.commonBooks(10), new ItemsForEmeralds(Items.GLASS, 1, 4, 10)}).put((Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30), new ItemsForEmeralds(Items.CLOCK, 5, 1, 15), new ItemsForEmeralds(Items.COMPASS, 4, 1, 15)}).put((Object)5, (Object)new ItemListing[]{VillagerTrades.specialBooks(), new ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)}).build()), VillagerProfession.ARMORER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 12, 2), new EmeraldForItems(Items.IRON_INGOT, 5, 12, 2)}).put((Object)2, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20), new ItemsForEmeralds(Items.SHIELD, 5, 1, 12, 10, 0.05f), new ItemsForEmeralds(Items.BELL, 36, 1, 12, 10, 0.2f)}).put((Object)4, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_BOOTS_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_HELMET_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_LEGGINGS_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_CHESTPLATE_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_BOOTS_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_HELMET_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_LEGGINGS_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_CHESTPLATE_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 2, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_BOOTS_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 3, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_HELMET_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 5, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_LEGGINGS_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 7, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_CHESTPLATE_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_BOOTS_4), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_HELMET_4), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_BOOTS_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_HELMET_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_LEGGINGS_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_CHESTPLATE_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_BOOTS_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_HELMET_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_LEGGINGS_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_CHESTPLATE_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_BOOTS, 1, 4, Items.DIAMOND_LEGGINGS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_LEGGINGS, 1, 4, Items.DIAMOND_CHESTPLATE, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_HELMET, 1, 4, Items.DIAMOND_BOOTS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_CHESTPLATE, 1, 2, Items.DIAMOND_HELMET, 1, 3, 15, 0.05f), VillagerType.TAIGA)}).put((Object)5, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 16, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_CHESTPLATE_5), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_LEGGINGS_5), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_LEGGINGS_5), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_BOOTS_5), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 6, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_HELMET_5), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 8, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_CHESTPLATE_5), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_BOOTS_5), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 12, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_HELMET_5), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_HELMET_5), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_BOOTS_5), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_HELMET_5), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_BOOTS_5), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 18, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_TAIGA_ARMORER_CHESTPLATE_5), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 18, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_TAIGA_ARMORER_LEGGINGS_5), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new EmeraldForItems(Items.DIAMOND_BLOCK, 1, 12, 30, 42), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new EmeraldForItems(Items.IRON_BLOCK, 1, 12, 30, 4), VillagerType.DESERT, VillagerType.JUNGLE, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.SWAMP)}).build()), VillagerProfession.CARTOGRAPHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), new ItemsForEmeralds(Items.MAP, 7, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.GLASS_PANE, 11, 16, 10), new TypeSpecificTrade((Map<VillagerType, ItemListing>)ImmutableMap.builder().put((Object)VillagerType.DESERT, (Object)SAVANNA_MAP).put((Object)VillagerType.SAVANNA, (Object)PLAINS_MAP).put((Object)VillagerType.PLAINS, (Object)TAIGA_MAP).put((Object)VillagerType.TAIGA, (Object)SNOWY_MAP).put((Object)VillagerType.SNOW, (Object)PLAINS_MAP).put((Object)VillagerType.JUNGLE, (Object)SAVANNA_MAP).put((Object)VillagerType.SWAMP, (Object)SNOWY_MAP).build()), new TypeSpecificTrade((Map<VillagerType, ItemListing>)ImmutableMap.builder().put((Object)VillagerType.DESERT, (Object)PLAINS_MAP).put((Object)VillagerType.SAVANNA, (Object)DESERT_MAP).put((Object)VillagerType.PLAINS, (Object)SAVANNA_MAP).put((Object)VillagerType.TAIGA, (Object)PLAINS_MAP).put((Object)VillagerType.SNOW, (Object)TAIGA_MAP).put((Object)VillagerType.JUNGLE, (Object)DESERT_MAP).put((Object)VillagerType.SWAMP, (Object)TAIGA_MAP).build()), new TypeSpecificTrade((Map<VillagerType, ItemListing>)ImmutableMap.builder().put((Object)VillagerType.DESERT, (Object)JUNGLE_MAP).put((Object)VillagerType.SAVANNA, (Object)JUNGLE_MAP).put((Object)VillagerType.PLAINS, (Object)new FailureItemListing()).put((Object)VillagerType.TAIGA, (Object)SWAMP_MAP).put((Object)VillagerType.SNOW, (Object)SWAMP_MAP).put((Object)VillagerType.JUNGLE, (Object)SWAMP_MAP).put((Object)VillagerType.SWAMP, (Object)JUNGLE_MAP).build())}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.COMPASS, 1, 12, 20), new TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10), new TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, (Object)4, (Object)new ItemListing[]{new ItemsForEmeralds(Items.ITEM_FRAME, 7, 1, 15), new ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.RED_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 15), new ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.GLOBE_BANNER_PATTERN, 8, 1, 30), new TreasureMapForEmeralds(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 1, 30)})));
    public static final List<Pair<ItemListing[], Integer>> EXPERIMENTAL_WANDERING_TRADER_TRADES = ImmutableList.builder().add((Object)Pair.of((Object)new ItemListing[]{new EmeraldForItems(VillagerTrades.potionCost(Potions.WATER), 1, 1, 1), new EmeraldForItems(Items.WATER_BUCKET, 1, 1, 1, 2), new EmeraldForItems(Items.MILK_BUCKET, 1, 1, 1, 2), new EmeraldForItems(Items.FERMENTED_SPIDER_EYE, 1, 1, 1, 3), new EmeraldForItems(Items.BAKED_POTATO, 4, 1, 1), new EmeraldForItems(Items.HAY_BLOCK, 1, 1, 1)}, (Object)2)).add((Object)Pair.of((Object)new ItemListing[]{new ItemsForEmeralds(Items.PACKED_ICE, 1, 1, 6, 1), new ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1), new ItemsForEmeralds(Items.GUNPOWDER, 1, 4, 2, 1), new ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1), new ItemsForEmeralds(Blocks.ACACIA_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.BIRCH_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.DARK_OAK_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.JUNGLE_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.OAK_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.SPRUCE_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.CHERRY_LOG, 1, 8, 4, 1), new EnchantedItemForEmeralds(Items.IRON_PICKAXE, 1, 1, 1, 0.2f), new ItemsForEmeralds(VillagerTrades.potion(Potions.LONG_INVISIBILITY), 5, 1, 1, 1)}, (Object)2)).add((Object)Pair.of((Object)new ItemListing[]{new ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1), new ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1), new ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1), new ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1), new ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1), new ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new ItemsForEmeralds(Items.FERN, 1, 1, 12, 1), new ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1), new ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1), new ItemsForEmeralds(Items.KELP, 3, 1, 12, 1), new ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1), new ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1), new ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1), new ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1), new ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1), new ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1), new ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1), new ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1), new ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.VINE, 1, 3, 4, 1), new ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 3, 4, 1), new ItemsForEmeralds(Items.RED_MUSHROOM, 1, 3, 4, 1), new ItemsForEmeralds(Items.LILY_PAD, 1, 5, 2, 1), new ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new ItemsForEmeralds(Items.SAND, 1, 8, 8, 1), new ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1), new ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1), new ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1)}, (Object)5)).build();

    private static ItemListing commonBooks(int n) {
        return new TypeSpecificTrade((Map<VillagerType, ItemListing>)ImmutableMap.builder().put((Object)VillagerType.DESERT, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_DESERT_COMMON)).put((Object)VillagerType.JUNGLE, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_JUNGLE_COMMON)).put((Object)VillagerType.PLAINS, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_PLAINS_COMMON)).put((Object)VillagerType.SAVANNA, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_SAVANNA_COMMON)).put((Object)VillagerType.SNOW, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_SNOW_COMMON)).put((Object)VillagerType.SWAMP, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_SWAMP_COMMON)).put((Object)VillagerType.TAIGA, (Object)new EnchantBookForEmeralds(n, EnchantmentTags.TRADES_TAIGA_COMMON)).build());
    }

    private static ItemListing specialBooks() {
        return new TypeSpecificTrade((Map<VillagerType, ItemListing>)ImmutableMap.builder().put((Object)VillagerType.DESERT, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_DESERT_SPECIAL)).put((Object)VillagerType.JUNGLE, (Object)new EnchantBookForEmeralds(30, 2, 2, EnchantmentTags.TRADES_JUNGLE_SPECIAL)).put((Object)VillagerType.PLAINS, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_PLAINS_SPECIAL)).put((Object)VillagerType.SAVANNA, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_SAVANNA_SPECIAL)).put((Object)VillagerType.SNOW, (Object)new EnchantBookForEmeralds(30, EnchantmentTags.TRADES_SNOW_SPECIAL)).put((Object)VillagerType.SWAMP, (Object)new EnchantBookForEmeralds(30, EnchantmentTags.TRADES_SWAMP_SPECIAL)).put((Object)VillagerType.TAIGA, (Object)new EnchantBookForEmeralds(30, 2, 2, EnchantmentTags.TRADES_TAIGA_SPECIAL)).build());
    }

    private static Int2ObjectMap<ItemListing[]> toIntMap(ImmutableMap<Integer, ItemListing[]> immutableMap) {
        return new Int2ObjectOpenHashMap(immutableMap);
    }

    private static ItemCost potionCost(Holder<Potion> holder) {
        return new ItemCost(Items.POTION).withComponents(builder -> builder.expect(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
    }

    private static ItemStack potion(Holder<Potion> holder) {
        return PotionContents.createItemStack(Items.POTION, holder);
    }

    record TypeSpecificTrade(Map<VillagerType, ItemListing> trades) implements ItemListing
    {
        public static TypeSpecificTrade oneTradeInBiomes(ItemListing itemListing, VillagerType ... villagerTypeArray) {
            return new TypeSpecificTrade(Arrays.stream(villagerTypeArray).collect(Collectors.toMap(villagerType -> villagerType, villagerType -> itemListing)));
        }

        @Override
        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            if (entity instanceof VillagerDataHolder) {
                VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
                VillagerType villagerType = villagerDataHolder.getVillagerData().getType();
                ItemListing itemListing = this.trades.get(villagerType);
                if (itemListing == null) {
                    return null;
                }
                return itemListing.getOffer(entity, randomSource);
            }
            return null;
        }
    }

    static class EnchantBookForEmeralds
    implements ItemListing {
        private final int villagerXp;
        private final TagKey<Enchantment> tradeableEnchantments;
        private final int minLevel;
        private final int maxLevel;

        public EnchantBookForEmeralds(int n, TagKey<Enchantment> tagKey) {
            this(n, 0, Integer.MAX_VALUE, tagKey);
        }

        public EnchantBookForEmeralds(int n, int n2, int n3, TagKey<Enchantment> tagKey) {
            this.minLevel = n2;
            this.maxLevel = n3;
            this.villagerXp = n;
            this.tradeableEnchantments = tagKey;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            int n;
            ItemStack itemStack;
            Optional<Holder<Enchantment>> optional = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getRandomElementOf(this.tradeableEnchantments, randomSource);
            if (!optional.isEmpty()) {
                Holder<Enchantment> holder = optional.get();
                Enchantment enchantment = holder.value();
                int n2 = Math.max(enchantment.getMinLevel(), this.minLevel);
                int n3 = Math.min(enchantment.getMaxLevel(), this.maxLevel);
                int n4 = Mth.nextInt(randomSource, n2, n3);
                itemStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, n4));
                n = 2 + randomSource.nextInt(5 + n4 * 10) + 3 * n4;
                if (holder.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    n *= 2;
                }
                if (n > 64) {
                    n = 64;
                }
            } else {
                n = 1;
                itemStack = new ItemStack(Items.BOOK);
            }
            return new MerchantOffer(new ItemCost(Items.EMERALD, n), Optional.of(new ItemCost(Items.BOOK)), itemStack, 12, this.villagerXp, 0.2f);
        }
    }

    public static interface ItemListing {
        @Nullable
        public MerchantOffer getOffer(Entity var1, RandomSource var2);
    }

    static class EmeraldForItems
    implements ItemListing {
        private final ItemCost itemStack;
        private final int maxUses;
        private final int villagerXp;
        private final int emeraldAmount;
        private final float priceMultiplier;

        public EmeraldForItems(ItemLike itemLike, int n, int n2, int n3) {
            this(itemLike, n, n2, n3, 1);
        }

        public EmeraldForItems(ItemLike itemLike, int n, int n2, int n3, int n4) {
            this(new ItemCost(itemLike.asItem(), n), n2, n3, n4);
        }

        public EmeraldForItems(ItemCost itemCost, int n, int n2, int n3) {
            this.itemStack = itemCost;
            this.maxUses = n;
            this.villagerXp = n2;
            this.emeraldAmount = n3;
            this.priceMultiplier = 0.05f;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            return new MerchantOffer(this.itemStack, new ItemStack(Items.EMERALD, this.emeraldAmount), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class ItemsForEmeralds
    implements ItemListing {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;
        private final Optional<ResourceKey<EnchantmentProvider>> enchantmentProvider;

        public ItemsForEmeralds(Block block, int n, int n2, int n3, int n4) {
            this(new ItemStack(block), n, n2, n3, n4);
        }

        public ItemsForEmeralds(Item item, int n, int n2, int n3) {
            this(new ItemStack(item), n, n2, 12, n3);
        }

        public ItemsForEmeralds(Item item, int n, int n2, int n3, int n4) {
            this(new ItemStack(item), n, n2, n3, n4);
        }

        public ItemsForEmeralds(ItemStack itemStack, int n, int n2, int n3, int n4) {
            this(itemStack, n, n2, n3, n4, 0.05f);
        }

        public ItemsForEmeralds(Item item, int n, int n2, int n3, int n4, float f) {
            this(new ItemStack(item), n, n2, n3, n4, f);
        }

        public ItemsForEmeralds(Item item, int n, int n2, int n3, int n4, float f, ResourceKey<EnchantmentProvider> resourceKey) {
            this(new ItemStack(item), n, n2, n3, n4, f, Optional.of(resourceKey));
        }

        public ItemsForEmeralds(ItemStack itemStack, int n, int n2, int n3, int n4, float f) {
            this(itemStack, n, n2, n3, n4, f, Optional.empty());
        }

        public ItemsForEmeralds(ItemStack itemStack, int n, int n2, int n3, int n4, float f, Optional<ResourceKey<EnchantmentProvider>> optional) {
            this.itemStack = itemStack;
            this.emeraldCost = n;
            this.itemStack.setCount(n2);
            this.maxUses = n3;
            this.villagerXp = n4;
            this.priceMultiplier = f;
            this.enchantmentProvider = optional;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            ItemStack itemStack = this.itemStack.copy();
            Level level = entity.level();
            this.enchantmentProvider.ifPresent(resourceKey -> EnchantmentHelper.enchantItemFromProvider(itemStack, level.registryAccess(), resourceKey, level.getCurrentDifficultyAt(entity.blockPosition()), randomSource));
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class SuspiciousStewForEmerald
    implements ItemListing {
        private final SuspiciousStewEffects effects;
        private final int xp;
        private final float priceMultiplier;

        public SuspiciousStewForEmerald(Holder<MobEffect> holder, int n, int n2) {
            this(new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(holder, n))), n2, 0.05f);
        }

        public SuspiciousStewForEmerald(SuspiciousStewEffects suspiciousStewEffects, int n, float f) {
            this.effects = suspiciousStewEffects;
            this.xp = n;
            this.priceMultiplier = f;
        }

        @Override
        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            itemStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.effects);
            return new MerchantOffer(new ItemCost(Items.EMERALD), itemStack, 12, this.xp, this.priceMultiplier);
        }
    }

    static class ItemsAndEmeraldsToItems
    implements ItemListing {
        private final ItemCost fromItem;
        private final int emeraldCost;
        private final ItemStack toItem;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;
        private final Optional<ResourceKey<EnchantmentProvider>> enchantmentProvider;

        public ItemsAndEmeraldsToItems(ItemLike itemLike, int n, int n2, Item item, int n3, int n4, int n5, float f) {
            this(itemLike, n, n2, new ItemStack(item), n3, n4, n5, f);
        }

        private ItemsAndEmeraldsToItems(ItemLike itemLike, int n, int n2, ItemStack itemStack, int n3, int n4, int n5, float f) {
            this(new ItemCost(itemLike, n), n2, itemStack.copyWithCount(n3), n4, n5, f, Optional.empty());
        }

        ItemsAndEmeraldsToItems(ItemLike itemLike, int n, int n2, ItemLike itemLike2, int n3, int n4, int n5, float f, ResourceKey<EnchantmentProvider> resourceKey) {
            this(new ItemCost(itemLike, n), n2, new ItemStack(itemLike2, n3), n4, n5, f, Optional.of(resourceKey));
        }

        public ItemsAndEmeraldsToItems(ItemCost itemCost, int n, ItemStack itemStack, int n2, int n3, float f, Optional<ResourceKey<EnchantmentProvider>> optional) {
            this.fromItem = itemCost;
            this.emeraldCost = n;
            this.toItem = itemStack;
            this.maxUses = n2;
            this.villagerXp = n3;
            this.priceMultiplier = f;
            this.enchantmentProvider = optional;
        }

        @Override
        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            ItemStack itemStack = this.toItem.copy();
            Level level = entity.level();
            this.enchantmentProvider.ifPresent(resourceKey -> EnchantmentHelper.enchantItemFromProvider(itemStack, level.registryAccess(), resourceKey, level.getCurrentDifficultyAt(entity.blockPosition()), randomSource));
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(this.fromItem), itemStack, 0, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class EnchantedItemForEmeralds
    implements ItemListing {
        private final ItemStack itemStack;
        private final int baseEmeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public EnchantedItemForEmeralds(Item item, int n, int n2, int n3) {
            this(item, n, n2, n3, 0.05f);
        }

        public EnchantedItemForEmeralds(Item item, int n, int n2, int n3, float f) {
            this.itemStack = new ItemStack(item);
            this.baseEmeraldCost = n;
            this.maxUses = n2;
            this.villagerXp = n3;
            this.priceMultiplier = f;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            int n = 5 + randomSource.nextInt(15);
            RegistryAccess registryAccess = entity.level().registryAccess();
            Optional<HolderSet.Named<Enchantment>> optional = registryAccess.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.ON_TRADED_EQUIPMENT);
            ItemStack itemStack = EnchantmentHelper.enchantItem(randomSource, new ItemStack(this.itemStack.getItem()), n, registryAccess, optional);
            int n2 = Math.min(this.baseEmeraldCost + n, 64);
            ItemCost itemCost = new ItemCost(Items.EMERALD, n2);
            return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class EmeraldsForVillagerTypeItem
    implements ItemListing {
        private final Map<VillagerType, Item> trades;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;

        public EmeraldsForVillagerTypeItem(int n, int n2, int n3, Map<VillagerType, Item> map) {
            BuiltInRegistries.VILLAGER_TYPE.stream().filter(villagerType -> !map.containsKey(villagerType)).findAny().ifPresent(villagerType -> {
                throw new IllegalStateException("Missing trade for villager type: " + String.valueOf(BuiltInRegistries.VILLAGER_TYPE.getKey((VillagerType)villagerType)));
            });
            this.trades = map;
            this.cost = n;
            this.maxUses = n2;
            this.villagerXp = n3;
        }

        @Override
        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            if (entity instanceof VillagerDataHolder) {
                VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
                ItemCost itemCost = new ItemCost(this.trades.get(villagerDataHolder.getVillagerData().getType()), this.cost);
                return new MerchantOffer(itemCost, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05f);
            }
            return null;
        }
    }

    static class TippedArrowForItemsAndEmeralds
    implements ItemListing {
        private final ItemStack toItem;
        private final int toCount;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final Item fromItem;
        private final int fromCount;
        private final float priceMultiplier;

        public TippedArrowForItemsAndEmeralds(Item item, int n, Item item2, int n2, int n3, int n4, int n5) {
            this.toItem = new ItemStack(item2);
            this.emeraldCost = n3;
            this.maxUses = n4;
            this.villagerXp = n5;
            this.fromItem = item;
            this.fromCount = n;
            this.toCount = n2;
            this.priceMultiplier = 0.05f;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            ItemCost itemCost = new ItemCost(Items.EMERALD, this.emeraldCost);
            List list = BuiltInRegistries.POTION.holders().filter(reference -> !((Potion)reference.value()).getEffects().isEmpty() && entity.level().potionBrewing().isBrewablePotion((Holder<Potion>)reference)).collect(Collectors.toList());
            Holder holder = (Holder)Util.getRandom(list, randomSource);
            ItemStack itemStack = new ItemStack(this.toItem.getItem(), this.toCount);
            itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
            return new MerchantOffer(itemCost, Optional.of(new ItemCost(this.fromItem, this.fromCount)), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class TreasureMapForEmeralds
    implements ItemListing {
        private final int emeraldCost;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final Holder<MapDecorationType> destinationType;
        private final int maxUses;
        private final int villagerXp;

        public TreasureMapForEmeralds(int n, TagKey<Structure> tagKey, String string, Holder<MapDecorationType> holder, int n2, int n3) {
            this.emeraldCost = n;
            this.destination = tagKey;
            this.displayName = string;
            this.destinationType = holder;
            this.maxUses = n2;
            this.villagerXp = n3;
        }

        @Override
        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            if (!(entity.level() instanceof ServerLevel)) {
                return null;
            }
            ServerLevel serverLevel = (ServerLevel)entity.level();
            BlockPos blockPos = serverLevel.findNearestMapStructure(this.destination, entity.blockPosition(), 100, true);
            if (blockPos != null) {
                ItemStack itemStack = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                MapItem.renderBiomePreviewMap(serverLevel, itemStack);
                MapItemSavedData.addTargetDecoration(itemStack, blockPos, "+", this.destinationType);
                itemStack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));
                return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemStack, this.maxUses, this.villagerXp, 0.2f);
            }
            return null;
        }
    }

    static class DyedArmorForEmeralds
    implements ItemListing {
        private final Item item;
        private final int value;
        private final int maxUses;
        private final int villagerXp;

        public DyedArmorForEmeralds(Item item, int n) {
            this(item, n, 12, 1);
        }

        public DyedArmorForEmeralds(Item item, int n, int n2, int n3) {
            this.item = item;
            this.value = n;
            this.maxUses = n2;
            this.villagerXp = n3;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            ItemCost itemCost = new ItemCost(Items.EMERALD, this.value);
            ItemStack itemStack = new ItemStack(this.item);
            if (itemStack.is(ItemTags.DYEABLE)) {
                ArrayList arrayList = Lists.newArrayList();
                arrayList.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                if (randomSource.nextFloat() > 0.7f) {
                    arrayList.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                }
                if (randomSource.nextFloat() > 0.8f) {
                    arrayList.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                }
                itemStack = DyedItemColor.applyDyes(itemStack, arrayList);
            }
            return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, 0.2f);
        }

        private static DyeItem getRandomDye(RandomSource randomSource) {
            return DyeItem.byColor(DyeColor.byId(randomSource.nextInt(16)));
        }
    }

    static class FailureItemListing
    implements ItemListing {
        FailureItemListing() {
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            return null;
        }
    }
}

