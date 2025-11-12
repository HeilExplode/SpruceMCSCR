/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeCraftingHolder {
    public void setRecipeUsed(@Nullable RecipeHolder<?> var1);

    @Nullable
    public RecipeHolder<?> getRecipeUsed();

    default public void awardUsedRecipes(Player player, List<ItemStack> list) {
        RecipeHolder<?> recipeHolder = this.getRecipeUsed();
        if (recipeHolder != null) {
            player.triggerRecipeCrafted(recipeHolder, list);
            if (!recipeHolder.value().isSpecial()) {
                player.awardRecipes(Collections.singleton(recipeHolder));
                this.setRecipeUsed(null);
            }
        }
    }

    default public boolean setRecipeUsed(Level level, ServerPlayer serverPlayer, RecipeHolder<?> recipeHolder) {
        if (recipeHolder.value().isSpecial() || !level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || serverPlayer.getRecipeBook().contains(recipeHolder)) {
            this.setRecipeUsed(recipeHolder);
            return true;
        }
        return false;
    }
}

