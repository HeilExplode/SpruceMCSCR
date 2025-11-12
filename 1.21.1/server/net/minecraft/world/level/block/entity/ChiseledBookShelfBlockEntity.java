/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity
extends BlockEntity
implements Container {
    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    public ChiseledBookShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CHISELED_BOOKSHELF, blockPos, blockState);
    }

    private void updateState(int n) {
        if (n < 0 || n >= 6) {
            LOGGER.error("Expected slot 0-5, got {}", (Object)n);
            return;
        }
        this.lastInteractedSlot = n;
        BlockState blockState = this.getBlockState();
        for (int i = 0; i < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++i) {
            boolean bl = !this.getItem(i).isEmpty();
            BooleanProperty booleanProperty = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i);
            blockState = (BlockState)blockState.setValue(booleanProperty, bl);
        }
        Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockState, 3);
        this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of(blockState));
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items, provider);
        this.lastInteractedSlot = compoundTag.getInt("last_interacted_slot");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ContainerHelper.saveAllItems(compoundTag, this.items, true, provider);
        compoundTag.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    public int count() {
        return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int n) {
        return this.items.get(n);
    }

    @Override
    public ItemStack removeItem(int n, int n2) {
        ItemStack itemStack = Objects.requireNonNullElse(this.items.get(n), ItemStack.EMPTY);
        this.items.set(n, ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            this.updateState(n);
        }
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int n) {
        return this.removeItem(n, 1);
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
            this.items.set(n, itemStack);
            this.updateState(n);
        } else if (itemStack.isEmpty()) {
            this.removeItem(n, 1);
        }
    }

    @Override
    public boolean canTakeItem(Container container, int n, ItemStack itemStack) {
        return container.hasAnyMatching(itemStack2 -> {
            if (itemStack2.isEmpty()) {
                return true;
            }
            return ItemStack.isSameItemSameComponents(itemStack, itemStack2) && itemStack2.getCount() + itemStack.getCount() <= container.getMaxStackSize((ItemStack)itemStack2);
        });
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int n, ItemStack itemStack) {
        return itemStack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(n).isEmpty() && itemStack.getCount() == this.getMaxStackSize();
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        dataComponentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.items);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        compoundTag.remove("Items");
    }
}

