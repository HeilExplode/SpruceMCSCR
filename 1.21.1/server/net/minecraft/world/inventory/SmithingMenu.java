/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu
extends ItemCombinerMenu {
    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final Level level;
    @Nullable
    private RecipeHolder<SmithingRecipe> selectedRecipe;
    private final List<RecipeHolder<SmithingRecipe>> recipes;

    public SmithingMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int n, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.SMITHING, n, inventory, containerLevelAccess);
        this.level = inventory.player.level();
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, itemStack -> this.recipes.stream().anyMatch(recipeHolder -> ((SmithingRecipe)recipeHolder.value()).isTemplateIngredient((ItemStack)itemStack))).withSlot(1, 26, 48, itemStack -> this.recipes.stream().anyMatch(recipeHolder -> ((SmithingRecipe)recipeHolder.value()).isBaseIngredient((ItemStack)itemStack))).withSlot(2, 44, 48, itemStack -> this.recipes.stream().anyMatch(recipeHolder -> ((SmithingRecipe)recipeHolder.value()).isAdditionIngredient((ItemStack)itemStack))).withResultSlot(3, 98, 48).build();
    }

    @Override
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean mayPickup(Player player, boolean bl) {
        return this.selectedRecipe != null && this.selectedRecipe.value().matches(this.createRecipeInput(), this.level);
    }

    @Override
    protected void onTake(Player player, ItemStack itemStack) {
        itemStack.onCraftedBy(player.level(), player, itemStack.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((level, blockPos) -> level.levelEvent(1044, (BlockPos)blockPos, 0));
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private SmithingRecipeInput createRecipeInput() {
        return new SmithingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private void shrinkStackInSlot(int n) {
        ItemStack itemStack = this.inputSlots.getItem(n);
        if (!itemStack.isEmpty()) {
            itemStack.shrink(1);
            this.inputSlots.setItem(n, itemStack);
        }
    }

    @Override
    public void createResult() {
        SmithingRecipeInput smithingRecipeInput = this.createRecipeInput();
        List<RecipeHolder<SmithingRecipe>> list = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, smithingRecipeInput, this.level);
        if (list.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            RecipeHolder<SmithingRecipe> recipeHolder = list.get(0);
            ItemStack itemStack = recipeHolder.value().assemble(smithingRecipeInput, this.level.registryAccess());
            if (itemStack.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = recipeHolder;
                this.resultSlots.setRecipeUsed(recipeHolder);
                this.resultSlots.setItem(0, itemStack);
            }
        }
    }

    @Override
    public int getSlotToQuickMoveTo(ItemStack itemStack) {
        return this.findSlotToQuickMoveTo(itemStack).orElse(0);
    }

    private static OptionalInt findSlotMatchingIngredient(SmithingRecipe smithingRecipe, ItemStack itemStack) {
        if (smithingRecipe.isTemplateIngredient(itemStack)) {
            return OptionalInt.of(0);
        }
        if (smithingRecipe.isBaseIngredient(itemStack)) {
            return OptionalInt.of(1);
        }
        if (smithingRecipe.isAdditionIngredient(itemStack)) {
            return OptionalInt.of(2);
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack itemStack) {
        return this.findSlotToQuickMoveTo(itemStack).isPresent();
    }

    private OptionalInt findSlotToQuickMoveTo(ItemStack itemStack) {
        return this.recipes.stream().flatMapToInt(recipeHolder -> SmithingMenu.findSlotMatchingIngredient((SmithingRecipe)recipeHolder.value(), itemStack).stream()).filter(n -> !this.getSlot(n).hasItem()).findFirst();
    }
}

