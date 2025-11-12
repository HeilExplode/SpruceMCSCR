/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnowledgeBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        List list = itemStack.getOrDefault(DataComponents.RECIPES, List.of());
        itemStack.consume(1, player);
        if (list.isEmpty()) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!level.isClientSide) {
            RecipeManager recipeManager = level.getServer().getRecipeManager();
            ArrayList arrayList = new ArrayList(list.size());
            for (ResourceLocation resourceLocation : list) {
                Optional<RecipeHolder<?>> optional = recipeManager.byKey(resourceLocation);
                if (optional.isPresent()) {
                    arrayList.add(optional.get());
                    continue;
                }
                LOGGER.error("Invalid recipe: {}", (Object)resourceLocation);
                return InteractionResultHolder.fail(itemStack);
            }
            player.awardRecipes(arrayList);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}

