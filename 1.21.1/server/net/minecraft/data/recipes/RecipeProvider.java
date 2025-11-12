/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider
implements DataProvider {
    final PackOutput.PathProvider recipePathProvider;
    final PackOutput.PathProvider advancementPathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.builder().put((Object)BlockFamily.Variant.BUTTON, (itemLike, itemLike2) -> RecipeProvider.buttonBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CHISELED, (itemLike, itemLike2) -> RecipeProvider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUT, (itemLike, itemLike2) -> RecipeProvider.cutBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.DOOR, (itemLike, itemLike2) -> RecipeProvider.doorBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUSTOM_FENCE, (itemLike, itemLike2) -> RecipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.FENCE, (itemLike, itemLike2) -> RecipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUSTOM_FENCE_GATE, (itemLike, itemLike2) -> RecipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.FENCE_GATE, (itemLike, itemLike2) -> RecipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.SIGN, (itemLike, itemLike2) -> RecipeProvider.signBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.SLAB, (itemLike, itemLike2) -> RecipeProvider.slabBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.STAIRS, (itemLike, itemLike2) -> RecipeProvider.stairBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.PRESSURE_PLATE, (itemLike, itemLike2) -> RecipeProvider.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.POLISHED, (itemLike, itemLike2) -> RecipeProvider.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.TRAPDOOR, (itemLike, itemLike2) -> RecipeProvider.trapdoorBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.WALL, (itemLike, itemLike2) -> RecipeProvider.wallBuilder(RecipeCategory.DECORATIONS, itemLike, Ingredient.of(itemLike2))).build();

    public RecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.recipePathProvider = packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
        this.advancementPathProvider = packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registries = completableFuture;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> this.run(cachedOutput, (HolderLookup.Provider)provider));
    }

    protected CompletableFuture<?> run(final CachedOutput cachedOutput, final HolderLookup.Provider provider) {
        final HashSet hashSet = Sets.newHashSet();
        final ArrayList arrayList = new ArrayList();
        this.buildRecipes(new RecipeOutput(){

            @Override
            public void accept(ResourceLocation resourceLocation, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                if (!hashSet.add(resourceLocation)) {
                    throw new IllegalStateException("Duplicate recipe " + String.valueOf(resourceLocation));
                }
                arrayList.add(DataProvider.saveStable(cachedOutput, provider, Recipe.CODEC, recipe, RecipeProvider.this.recipePathProvider.json(resourceLocation)));
                if (advancementHolder != null) {
                    arrayList.add(DataProvider.saveStable(cachedOutput, provider, Advancement.CODEC, advancementHolder.value(), RecipeProvider.this.advancementPathProvider.json(advancementHolder.id())));
                }
            }

            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }
        });
        return CompletableFuture.allOf((CompletableFuture[])arrayList.toArray(CompletableFuture[]::new));
    }

    protected CompletableFuture<?> buildAdvancement(CachedOutput cachedOutput, HolderLookup.Provider provider, AdvancementHolder advancementHolder) {
        return DataProvider.saveStable(cachedOutput, provider, Advancement.CODEC, advancementHolder.value(), this.advancementPathProvider.json(advancementHolder.id()));
    }

    protected abstract void buildRecipes(RecipeOutput var1);

    protected static void generateForEnabledBlockFamilies(RecipeOutput recipeOutput, FeatureFlagSet featureFlagSet) {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateRecipe).forEach(blockFamily -> RecipeProvider.generateRecipes(recipeOutput, blockFamily, featureFlagSet));
    }

    protected static void oneToOneConversionRecipe(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
        RecipeProvider.oneToOneConversionRecipe(recipeOutput, itemLike, itemLike2, string, 1);
    }

    protected static void oneToOneConversionRecipe(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int n) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemLike, n).requires(itemLike2).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput, RecipeProvider.getConversionRecipeName(itemLike, itemLike2));
    }

    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int n, String string) {
        RecipeProvider.oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, list, recipeCategory, itemLike, f, n, string, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int n, String string) {
        RecipeProvider.oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, list, recipeCategory, itemLike, f, n, string, "_from_blasting");
    }

    private static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int n, String string, String string2) {
        for (ItemLike itemLike2 : list) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike2), recipeCategory, itemLike, f, n, recipeSerializer, factory).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput, RecipeProvider.getItemName(itemLike) + string2 + "_" + RecipeProvider.getItemName(itemLike2));
        }
    }

    protected static void netheriteSmithing(RecipeOutput recipeOutput, Item item, RecipeCategory recipeCategory, Item item2) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), recipeCategory, item2).unlocks("has_netherite_ingot", RecipeProvider.has(Items.NETHERITE_INGOT)).save(recipeOutput, RecipeProvider.getItemName(item2) + "_smithing");
    }

    protected static void trimSmithing(RecipeOutput recipeOutput, Item item, ResourceLocation resourceLocation) {
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(item), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).unlocks("has_smithing_trim_template", RecipeProvider.has(item)).save(recipeOutput, resourceLocation);
    }

    protected static void twoByTwoPacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, String string) {
        ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike).requires(itemLike2, 9).unlockedBy(string, (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.threeByThreePacker(recipeOutput, recipeCategory, itemLike, itemLike2, RecipeProvider.getHasName(itemLike2));
    }

    protected static void planksFromLog(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey, int n) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, n).requires(tagKey).group("planks").unlockedBy("has_log", (Criterion)RecipeProvider.has(tagKey)).save(recipeOutput);
    }

    protected static void planksFromLogs(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey, int n) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, n).requires(tagKey).group("planks").unlockedBy("has_logs", (Criterion)RecipeProvider.has(tagKey)).save(recipeOutput);
    }

    protected static void woodFromLogs(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").group("bark").unlockedBy("has_log", (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void woodenBoat(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, itemLike).define(Character.valueOf('#'), itemLike2).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", (Criterion)RecipeProvider.insideOf(Blocks.WATER)).save(recipeOutput);
    }

    protected static void chestBoat(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, itemLike).requires(Blocks.CHEST).requires(itemLike2).group("chest_boat").unlockedBy("has_boat", (Criterion)RecipeProvider.has(ItemTags.BOATS)).save(recipeOutput);
    }

    private static RecipeBuilder buttonBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, itemLike).requires(ingredient);
    }

    protected static RecipeBuilder doorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 3).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##").pattern("##");
    }

    private static RecipeBuilder fenceBuilder(ItemLike itemLike, Ingredient ingredient) {
        int n = itemLike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item item = itemLike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, n).define(Character.valueOf('W'), ingredient).define(Character.valueOf('#'), item).pattern("W#W").pattern("W#W");
    }

    private static RecipeBuilder fenceGateBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('W'), ingredient).pattern("#W#").pattern("#W#");
    }

    protected static void pressurePlate(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    private static RecipeBuilder pressurePlateBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("##");
    }

    protected static void slab(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.slabBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static RecipeBuilder slabBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###");
    }

    protected static RecipeBuilder stairBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("#  ").pattern("## ").pattern("###");
    }

    protected static RecipeBuilder trapdoorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 2).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    private static RecipeBuilder signBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3).group("sign").define(Character.valueOf('#'), ingredient).define(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    protected static void hangingSign(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 6).group("hanging_sign").define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void colorBlockWithDye(RecipeOutput recipeOutput, List<Item> list, List<Item> list2, String string) {
        for (int i = 0; i < list.size(); ++i) {
            Item item = list.get(i);
            Item item3 = list2.get(i);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, item3).requires(item).requires(Ingredient.of(list2.stream().filter(item2 -> !item2.equals(item3)).map(ItemStack::new))).group(string).unlockedBy("has_needed_dye", (Criterion)RecipeProvider.has(item)).save(recipeOutput, "dye_" + RecipeProvider.getItemName(item3));
        }
    }

    protected static void carpet(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").group("carpet").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void bedFromPlanksAndWool(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void banner(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void stainedGlassFromGlassAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", (Criterion)RecipeProvider.has(Blocks.GLASS)).save(recipeOutput);
    }

    protected static void stainedGlassPaneFromStainedGlass(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 16).define(Character.valueOf('#'), itemLike2).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void stainedGlassPaneFromGlassPaneAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ((ShapedRecipeBuilder)ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS_PANE).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", (Criterion)RecipeProvider.has(Blocks.GLASS_PANE))).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput, RecipeProvider.getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
    }

    protected static void coloredTerracottaFromTerracottaAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.TERRACOTTA).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", (Criterion)RecipeProvider.has(Blocks.TERRACOTTA)).save(recipeOutput);
    }

    protected static void concretePowder(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ((ShapelessRecipeBuilder)ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).requires(itemLike2).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", (Criterion)RecipeProvider.has(Blocks.SAND))).unlockedBy("has_gravel", (Criterion)RecipeProvider.has(Blocks.GRAVEL)).save(recipeOutput);
    }

    protected static void candle(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike).requires(Blocks.CANDLE).requires(itemLike2).group("dyed_candle").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void wall(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.wallBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    private static RecipeBuilder wallBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    protected static void polished(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.polishedBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    private static RecipeBuilder polishedBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('S'), ingredient).pattern("SS").pattern("SS");
    }

    protected static void cut(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.cutBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    private static ShapedRecipeBuilder cutBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##");
    }

    protected static void chiseled(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.chiseledBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void mosaicBuilder(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), itemLike2).pattern("#").pattern("#").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("#").pattern("#");
    }

    protected static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.stonecutterResultFromBase(recipeOutput, recipeCategory, itemLike, itemLike2, 1);
    }

    protected static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, int n) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), recipeCategory, itemLike, n).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput, RecipeProvider.getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
    }

    private static void smeltingResultFromBase(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), RecipeCategory.BUILDING_BLOCKS, itemLike, 0.1f, 200).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2)).save(recipeOutput);
    }

    protected static void nineBlockStorageRecipes(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2) {
        RecipeProvider.nineBlockStorageRecipes(recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected static void nineBlockStorageRecipesWithCustomPacking(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, string, string2, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, string, string2);
    }

    private static void nineBlockStorageRecipes(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, @Nullable String string2, String string3, @Nullable String string4) {
        ((ShapelessRecipeBuilder)ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike, 9).requires(itemLike2).group(string4).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)RecipeProvider.has(itemLike2))).save(recipeOutput, ResourceLocation.parse(string3));
        ((ShapedRecipeBuilder)ShapedRecipeBuilder.shaped(recipeCategory2, itemLike2).define(Character.valueOf('#'), itemLike).pattern("###").pattern("###").pattern("###").group(string2).unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)RecipeProvider.has(itemLike))).save(recipeOutput, ResourceLocation.parse(string));
    }

    protected static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2).define(Character.valueOf('#'), Items.DIAMOND).define(Character.valueOf('C'), tagKey).define(Character.valueOf('S'), itemLike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)RecipeProvider.has(itemLike)).save(recipeOutput);
    }

    protected static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2).define(Character.valueOf('#'), Items.DIAMOND).define(Character.valueOf('C'), itemLike2).define(Character.valueOf('S'), itemLike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)RecipeProvider.has(itemLike)).save(recipeOutput);
    }

    protected static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike itemLike, Ingredient ingredient) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2).define(Character.valueOf('#'), Items.DIAMOND).define(Character.valueOf('C'), ingredient).define(Character.valueOf('S'), itemLike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)RecipeProvider.has(itemLike)).save(recipeOutput);
    }

    protected static <T extends AbstractCookingRecipe> void cookRecipes(RecipeOutput recipeOutput, String string, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, int n) {
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.COD, Items.COOKED_COD, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.KELP, Items.DRIED_KELP, 0.1f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        RecipeProvider.simpleCookingRecipe(recipeOutput, string, recipeSerializer, factory, n, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    private static <T extends AbstractCookingRecipe> void simpleCookingRecipe(RecipeOutput recipeOutput, String string, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, int n, ItemLike itemLike, ItemLike itemLike2, float f) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), RecipeCategory.FOOD, itemLike2, f, n, recipeSerializer, factory).unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)RecipeProvider.has(itemLike)).save(recipeOutput, RecipeProvider.getItemName(itemLike2) + "_from_" + string);
    }

    protected static void waxRecipes(RecipeOutput recipeOutput, FeatureFlagSet featureFlagSet) {
        HoneycombItem.WAXABLES.get().forEach((block, block2) -> {
            if (!block2.requiredFeatures().isSubsetOf(featureFlagSet)) {
                return;
            }
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block2).requires((ItemLike)block).requires(Items.HONEYCOMB).group(RecipeProvider.getItemName(block2)).unlockedBy(RecipeProvider.getHasName(block), (Criterion)RecipeProvider.has(block)).save(recipeOutput, RecipeProvider.getConversionRecipeName(block2, Items.HONEYCOMB));
        });
    }

    protected static void grate(RecipeOutput recipeOutput, Block block, Block block2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block, 4).define(Character.valueOf('M'), block2).pattern(" M ").pattern("M M").pattern(" M ").unlockedBy(RecipeProvider.getHasName(block2), (Criterion)RecipeProvider.has(block2)).save(recipeOutput);
    }

    protected static void copperBulb(RecipeOutput recipeOutput, Block block, Block block2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block, 4).define(Character.valueOf('C'), block2).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('B'), Items.BLAZE_ROD).pattern(" C ").pattern("CBC").pattern(" R ").unlockedBy(RecipeProvider.getHasName(block2), (Criterion)RecipeProvider.has(block2)).save(recipeOutput);
    }

    protected static void generateRecipes(RecipeOutput recipeOutput, BlockFamily blockFamily, FeatureFlagSet featureFlagSet) {
        blockFamily.getVariants().forEach((variant, block) -> {
            if (!block.requiredFeatures().isSubsetOf(featureFlagSet)) {
                return;
            }
            BiFunction<ItemLike, ItemLike, RecipeBuilder> biFunction = SHAPE_BUILDERS.get(variant);
            Block block2 = RecipeProvider.getBaseBlock(blockFamily, variant);
            if (biFunction != null) {
                RecipeBuilder recipeBuilder = biFunction.apply((ItemLike)block, block2);
                blockFamily.getRecipeGroupPrefix().ifPresent(string -> recipeBuilder.group(string + (String)(variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getRecipeGroup())));
                recipeBuilder.unlockedBy(blockFamily.getRecipeUnlockedBy().orElseGet(() -> RecipeProvider.getHasName(block2)), RecipeProvider.has(block2));
                recipeBuilder.save(recipeOutput);
            }
            if (variant == BlockFamily.Variant.CRACKED) {
                RecipeProvider.smeltingResultFromBase(recipeOutput, block, block2);
            }
        });
    }

    private static Block getBaseBlock(BlockFamily blockFamily, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!blockFamily.getVariants().containsKey((Object)BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            }
            return blockFamily.get(BlockFamily.Variant.SLAB);
        }
        return blockFamily.getBaseBlock();
    }

    private static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return CriteriaTriggers.ENTER_BLOCK.createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints ints, ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).withCount(ints));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tagKey) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(tagKey));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder ... builderArray) {
        return RecipeProvider.inventoryTrigger((ItemPredicate[])Arrays.stream(builderArray).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate ... itemPredicateArray) {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(itemPredicateArray)));
    }

    protected static String getHasName(ItemLike itemLike) {
        return "has_" + RecipeProvider.getItemName(itemLike);
    }

    protected static String getItemName(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
    }

    protected static String getSimpleRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike);
    }

    protected static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
        return RecipeProvider.getItemName(itemLike) + "_from_" + RecipeProvider.getItemName(itemLike2);
    }

    protected static String getSmeltingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_smelting";
    }

    protected static String getBlastingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_blasting";
    }

    @Override
    public final String getName() {
        return "Recipes";
    }
}

