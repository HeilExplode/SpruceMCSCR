/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity
extends BaseContainerBlockEntity
implements WorldlyContainer,
RecipeCraftingHolder,
StackedContentsCompatible {
    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_FUEL = 1;
    protected static final int SLOT_RESULT = 2;
    public static final int DATA_LIT_TIME = 0;
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int NUM_DATA_VALUES = 4;
    public static final int BURN_TIME_STANDARD = 200;
    public static final int BURN_COOL_SPEED = 2;
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    int litTime;
    int litDuration;
    int cookingProgress;
    int cookingTotalTime;
    @Nullable
    private static volatile Map<Item, Integer> fuelCache;
    protected final ContainerData dataAccess = new ContainerData(){

        @Override
        public int get(int n) {
            switch (n) {
                case 0: {
                    return AbstractFurnaceBlockEntity.this.litTime;
                }
                case 1: {
                    return AbstractFurnaceBlockEntity.this.litDuration;
                }
                case 2: {
                    return AbstractFurnaceBlockEntity.this.cookingProgress;
                }
                case 3: {
                    return AbstractFurnaceBlockEntity.this.cookingTotalTime;
                }
            }
            return 0;
        }

        @Override
        public void set(int n, int n2) {
            switch (n) {
                case 0: {
                    AbstractFurnaceBlockEntity.this.litTime = n2;
                    break;
                }
                case 1: {
                    AbstractFurnaceBlockEntity.this.litDuration = n2;
                    break;
                }
                case 2: {
                    AbstractFurnaceBlockEntity.this.cookingProgress = n2;
                    break;
                }
                case 3: {
                    AbstractFurnaceBlockEntity.this.cookingTotalTime = n2;
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap();
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, blockPos, blockState);
        this.quickCheck = RecipeManager.createCheck(recipeType);
    }

    public static void invalidateCache() {
        fuelCache = null;
    }

    public static Map<Item, Integer> getFuel() {
        Map<Item, Integer> map = fuelCache;
        if (map != null) {
            return map;
        }
        LinkedHashMap linkedHashMap = Maps.newLinkedHashMap();
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.LAVA_BUCKET, 20000);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.COAL_BLOCK, 16000);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.BLAZE_ROD, 2400);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.COAL, 1600);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.CHARCOAL, 1600);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.LOGS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.BAMBOO_BLOCKS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.PLANKS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BAMBOO_MOSAIC, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_STAIRS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_SLABS, 150);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BAMBOO_MOSAIC_SLAB, 150);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_TRAPDOORS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_FENCES, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.FENCE_GATES, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.NOTE_BLOCK, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BOOKSHELF, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.CHISELED_BOOKSHELF, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.LECTERN, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.JUKEBOX, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.CHEST, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.TRAPPED_CHEST, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.CRAFTING_TABLE, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.DAYLIGHT_DETECTOR, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.BANNERS, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.BOW, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.FISHING_ROD, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.LADDER, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.SIGNS, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.HANGING_SIGNS, 800);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.WOODEN_SHOVEL, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.WOODEN_SWORD, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.WOODEN_HOE, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.WOODEN_AXE, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.WOODEN_PICKAXE, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_DOORS, 200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.BOATS, 1200);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOOL, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOODEN_BUTTONS, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.STICK, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.SAPLINGS, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.BOWL, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, ItemTags.WOOL_CARPETS, 67);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.DRIED_KELP_BLOCK, 4001);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Items.CROSSBOW, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BAMBOO, 50);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.DEAD_BUSH, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.SCAFFOLDING, 50);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.LOOM, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.BARREL, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.CARTOGRAPHY_TABLE, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.FLETCHING_TABLE, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.SMITHING_TABLE, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.COMPOSTER, 300);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.AZALEA, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.FLOWERING_AZALEA, 100);
        AbstractFurnaceBlockEntity.add((Map<Item, Integer>)linkedHashMap, Blocks.MANGROVE_ROOTS, 300);
        fuelCache = linkedHashMap;
        return linkedHashMap;
    }

    private static boolean isNeverAFurnaceFuel(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.NON_FLAMMABLE_WOOD);
    }

    private static void add(Map<Item, Integer> map, TagKey<Item> tagKey, int n) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
            if (AbstractFurnaceBlockEntity.isNeverAFurnaceFuel(holder.value())) continue;
            map.put(holder.value(), n);
        }
    }

    private static void add(Map<Item, Integer> map, ItemLike itemLike, int n) {
        Item item = itemLike.asItem();
        if (AbstractFurnaceBlockEntity.isNeverAFurnaceFuel(item)) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw Util.pauseInIde(new IllegalStateException("A developer tried to explicitly make fire resistant item " + item.getName(null).getString() + " a furnace fuel. That will not work!"));
            }
            return;
        }
        map.put(item, n);
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items, provider);
        this.litTime = compoundTag.getShort("BurnTime");
        this.cookingProgress = compoundTag.getShort("CookTime");
        this.cookingTotalTime = compoundTag.getShort("CookTimeTotal");
        this.litDuration = this.getBurnDuration(this.items.get(1));
        CompoundTag compoundTag2 = compoundTag.getCompound("RecipesUsed");
        for (String string : compoundTag2.getAllKeys()) {
            this.recipesUsed.put((Object)ResourceLocation.parse(string), compoundTag2.getInt(string));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putShort("BurnTime", (short)this.litTime);
        compoundTag.putShort("CookTime", (short)this.cookingProgress);
        compoundTag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
        ContainerHelper.saveAllItems(compoundTag, this.items, provider);
        CompoundTag compoundTag2 = new CompoundTag();
        this.recipesUsed.forEach((resourceLocation, n) -> compoundTag2.putInt(resourceLocation.toString(), (int)n));
        compoundTag.put("RecipesUsed", compoundTag2);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
        boolean bl;
        boolean bl2 = abstractFurnaceBlockEntity.isLit();
        boolean bl3 = false;
        if (abstractFurnaceBlockEntity.isLit()) {
            --abstractFurnaceBlockEntity.litTime;
        }
        ItemStack itemStack = abstractFurnaceBlockEntity.items.get(1);
        ItemStack itemStack2 = abstractFurnaceBlockEntity.items.get(0);
        boolean bl4 = !itemStack2.isEmpty();
        boolean bl5 = bl = !itemStack.isEmpty();
        if (abstractFurnaceBlockEntity.isLit() || bl && bl4) {
            RecipeHolder recipeHolder = bl4 ? (RecipeHolder)abstractFurnaceBlockEntity.quickCheck.getRecipeFor(new SingleRecipeInput(itemStack2), level).orElse(null) : null;
            int n = abstractFurnaceBlockEntity.getMaxStackSize();
            if (!abstractFurnaceBlockEntity.isLit() && AbstractFurnaceBlockEntity.canBurn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, n)) {
                abstractFurnaceBlockEntity.litDuration = abstractFurnaceBlockEntity.litTime = abstractFurnaceBlockEntity.getBurnDuration(itemStack);
                if (abstractFurnaceBlockEntity.isLit()) {
                    bl3 = true;
                    if (bl) {
                        Item item = itemStack.getItem();
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            Item item2 = item.getCraftingRemainingItem();
                            abstractFurnaceBlockEntity.items.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                        }
                    }
                }
            }
            if (abstractFurnaceBlockEntity.isLit() && AbstractFurnaceBlockEntity.canBurn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, n)) {
                ++abstractFurnaceBlockEntity.cookingProgress;
                if (abstractFurnaceBlockEntity.cookingProgress == abstractFurnaceBlockEntity.cookingTotalTime) {
                    abstractFurnaceBlockEntity.cookingProgress = 0;
                    abstractFurnaceBlockEntity.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(level, abstractFurnaceBlockEntity);
                    if (AbstractFurnaceBlockEntity.burn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, n)) {
                        abstractFurnaceBlockEntity.setRecipeUsed(recipeHolder);
                    }
                    bl3 = true;
                }
            } else {
                abstractFurnaceBlockEntity.cookingProgress = 0;
            }
        } else if (!abstractFurnaceBlockEntity.isLit() && abstractFurnaceBlockEntity.cookingProgress > 0) {
            abstractFurnaceBlockEntity.cookingProgress = Mth.clamp(abstractFurnaceBlockEntity.cookingProgress - 2, 0, abstractFurnaceBlockEntity.cookingTotalTime);
        }
        if (bl2 != abstractFurnaceBlockEntity.isLit()) {
            bl3 = true;
            blockState = (BlockState)blockState.setValue(AbstractFurnaceBlock.LIT, abstractFurnaceBlockEntity.isLit());
            level.setBlock(blockPos, blockState, 3);
        }
        if (bl3) {
            AbstractFurnaceBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> nonNullList, int n) {
        if (nonNullList.get(0).isEmpty() || recipeHolder == null) {
            return false;
        }
        ItemStack itemStack = recipeHolder.value().getResultItem(registryAccess);
        if (itemStack.isEmpty()) {
            return false;
        }
        ItemStack itemStack2 = nonNullList.get(2);
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(itemStack2, itemStack)) {
            return false;
        }
        if (itemStack2.getCount() < n && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
            return true;
        }
        return itemStack2.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> nonNullList, int n) {
        if (recipeHolder == null || !AbstractFurnaceBlockEntity.canBurn(registryAccess, recipeHolder, nonNullList, n)) {
            return false;
        }
        ItemStack itemStack = nonNullList.get(0);
        ItemStack itemStack2 = recipeHolder.value().getResultItem(registryAccess);
        ItemStack itemStack3 = nonNullList.get(2);
        if (itemStack3.isEmpty()) {
            nonNullList.set(2, itemStack2.copy());
        } else if (ItemStack.isSameItemSameComponents(itemStack3, itemStack2)) {
            itemStack3.grow(1);
        }
        if (itemStack.is(Blocks.WET_SPONGE.asItem()) && !nonNullList.get(1).isEmpty() && nonNullList.get(1).is(Items.BUCKET)) {
            nonNullList.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        itemStack.shrink(1);
        return true;
    }

    protected int getBurnDuration(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        Item item = itemStack.getItem();
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
    }

    private static int getTotalCookTime(Level level, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
        SingleRecipeInput singleRecipeInput = new SingleRecipeInput(abstractFurnaceBlockEntity.getItem(0));
        return abstractFurnaceBlockEntity.quickCheck.getRecipeFor(singleRecipeInput, level).map(recipeHolder -> ((AbstractCookingRecipe)recipeHolder.value()).getCookingTime()).orElse(200);
    }

    public static boolean isFuel(ItemStack itemStack) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(itemStack.getItem());
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(n, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
        if (direction == Direction.DOWN && n == 1) {
            return itemStack.is(Items.WATER_BUCKET) || itemStack.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        ItemStack itemStack2 = this.items.get(n);
        boolean bl = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack2, itemStack);
        this.items.set(n, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
        if (n == 0 && !bl) {
            this.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(this.level, this);
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (n == 2) {
            return false;
        }
        if (n == 1) {
            ItemStack itemStack2 = this.items.get(1);
            return AbstractFurnaceBlockEntity.isFuel(itemStack) || itemStack.is(Items.BUCKET) && !itemStack2.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
        if (recipeHolder != null) {
            ResourceLocation resourceLocation = recipeHolder.id();
            this.recipesUsed.addTo((Object)resourceLocation, 1);
        }
    }

    @Override
    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> list) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer) {
        List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience(serverPlayer.serverLevel(), serverPlayer.position());
        serverPlayer.awardRecipes(list);
        for (RecipeHolder<?> recipeHolder : list) {
            if (recipeHolder == null) continue;
            serverPlayer.triggerRecipeCrafted(recipeHolder, this.items);
        }
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel serverLevel, Vec3 vec3) {
        ArrayList arrayList = Lists.newArrayList();
        for (Object2IntMap.Entry entry : this.recipesUsed.object2IntEntrySet()) {
            serverLevel.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent(recipeHolder -> {
                arrayList.add(recipeHolder);
                AbstractFurnaceBlockEntity.createExperience(serverLevel, vec3, entry.getIntValue(), ((AbstractCookingRecipe)recipeHolder.value()).getExperience());
            });
        }
        return arrayList;
    }

    private static void createExperience(ServerLevel serverLevel, Vec3 vec3, int n, float f) {
        int n2 = Mth.floor((float)n * f);
        float f2 = Mth.frac((float)n * f);
        if (f2 != 0.0f && Math.random() < (double)f2) {
            ++n2;
        }
        ExperienceOrb.award(serverLevel, vec3, n2);
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (ItemStack itemStack : this.items) {
            stackedContents.accountStack(itemStack);
        }
    }
}

