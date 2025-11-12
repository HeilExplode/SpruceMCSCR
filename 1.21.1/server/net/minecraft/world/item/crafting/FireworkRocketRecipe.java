/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe
extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        boolean bl = false;
        int n = 0;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (PAPER_INGREDIENT.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!(GUNPOWDER_INGREDIENT.test(itemStack) ? ++n > 3 : !STAR_INGREDIENT.test(itemStack))) continue;
            return false;
        }
        return bl && n >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ArrayList<FireworkExplosion> arrayList = new ArrayList<FireworkExplosion>();
        int n = 0;
        for (int i = 0; i < craftingInput.size(); ++i) {
            FireworkExplosion fireworkExplosion;
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (GUNPOWDER_INGREDIENT.test(itemStack)) {
                ++n;
                continue;
            }
            if (!STAR_INGREDIENT.test(itemStack) || (fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION)) == null) continue;
            arrayList.add(fireworkExplosion);
        }
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
        itemStack.set(DataComponents.FIREWORKS, new Fireworks(n, arrayList));
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}

