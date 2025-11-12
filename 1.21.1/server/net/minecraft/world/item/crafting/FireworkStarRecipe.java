/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FireworkStarRecipe
extends CustomRecipe {
    private static final Ingredient SHAPE_INGREDIENT = Ingredient.of(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD);
    private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(Items.DIAMOND);
    private static final Ingredient TWINKLE_INGREDIENT = Ingredient.of(Items.GLOWSTONE_DUST);
    private static final Map<Item, FireworkExplosion.Shape> SHAPE_BY_ITEM = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(Items.FIRE_CHARGE, FireworkExplosion.Shape.LARGE_BALL);
        hashMap.put(Items.FEATHER, FireworkExplosion.Shape.BURST);
        hashMap.put(Items.GOLD_NUGGET, FireworkExplosion.Shape.STAR);
        hashMap.put(Items.SKELETON_SKULL, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.WITHER_SKELETON_SKULL, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.CREEPER_HEAD, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.PLAYER_HEAD, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.DRAGON_HEAD, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.ZOMBIE_HEAD, FireworkExplosion.Shape.CREEPER);
        hashMap.put(Items.PIGLIN_HEAD, FireworkExplosion.Shape.CREEPER);
    });
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);

    public FireworkStarRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (SHAPE_INGREDIENT.test(itemStack)) {
                if (bl3) {
                    return false;
                }
                bl3 = true;
                continue;
            }
            if (TWINKLE_INGREDIENT.test(itemStack)) {
                if (bl5) {
                    return false;
                }
                bl5 = true;
                continue;
            }
            if (TRAIL_INGREDIENT.test(itemStack)) {
                if (bl4) {
                    return false;
                }
                bl4 = true;
                continue;
            }
            if (GUNPOWDER_INGREDIENT.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (itemStack.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        FireworkExplosion.Shape shape = FireworkExplosion.Shape.SMALL_BALL;
        boolean bl = false;
        boolean bl2 = false;
        IntArrayList intArrayList = new IntArrayList();
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (SHAPE_INGREDIENT.test(itemStack)) {
                shape = SHAPE_BY_ITEM.get(itemStack.getItem());
                continue;
            }
            if (TWINKLE_INGREDIENT.test(itemStack)) {
                bl = true;
                continue;
            }
            if (TRAIL_INGREDIENT.test(itemStack)) {
                bl2 = true;
                continue;
            }
            if (!(itemStack.getItem() instanceof DyeItem)) continue;
            intArrayList.add(((DyeItem)itemStack.getItem()).getDyeColor().getFireworkColor());
        }
        ItemStack itemStack = new ItemStack(Items.FIREWORK_STAR);
        itemStack.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(shape, (IntList)intArrayList, IntList.of(), bl2, bl));
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(Items.FIREWORK_STAR);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}

