/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class LoomMenu
extends AbstractContainerMenu {
    private static final int PATTERN_NOT_SET = -1;
    private static final int INV_SLOT_START = 4;
    private static final int INV_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;
    private final ContainerLevelAccess access;
    final DataSlot selectedBannerPatternIndex = DataSlot.standalone();
    private List<Holder<BannerPattern>> selectablePatterns = List.of();
    Runnable slotUpdateListener = () -> {};
    private final HolderGetter<BannerPattern> patternGetter;
    final Slot bannerSlot;
    final Slot dyeSlot;
    private final Slot patternSlot;
    private final Slot resultSlot;
    long lastSoundTime;
    private final Container inputContainer = new SimpleContainer(3){

        @Override
        public void setChanged() {
            super.setChanged();
            LoomMenu.this.slotsChanged(this);
            LoomMenu.this.slotUpdateListener.run();
        }
    };
    private final Container outputContainer = new SimpleContainer(1){

        @Override
        public void setChanged() {
            super.setChanged();
            LoomMenu.this.slotUpdateListener.run();
        }
    };

    public LoomMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public LoomMenu(int n, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(MenuType.LOOM, n);
        int n2;
        this.access = containerLevelAccess;
        this.bannerSlot = this.addSlot(new Slot(this, this.inputContainer, 0, 13, 26){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() instanceof BannerItem;
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this, this.inputContainer, 1, 33, 26){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() instanceof DyeItem;
            }
        });
        this.patternSlot = this.addSlot(new Slot(this, this.inputContainer, 2, 23, 45){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() instanceof BannerPatternItem;
            }
        });
        this.resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 143, 57){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                LoomMenu.this.bannerSlot.remove(1);
                LoomMenu.this.dyeSlot.remove(1);
                if (!LoomMenu.this.bannerSlot.hasItem() || !LoomMenu.this.dyeSlot.hasItem()) {
                    LoomMenu.this.selectedBannerPatternIndex.set(-1);
                }
                containerLevelAccess.execute((level, blockPos) -> {
                    long l = level.getGameTime();
                    if (LoomMenu.this.lastSoundTime != l) {
                        level.playSound(null, (BlockPos)blockPos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);
                        LoomMenu.this.lastSoundTime = l;
                    }
                });
                super.onTake(player, itemStack);
            }
        });
        for (n2 = 0; n2 < 3; ++n2) {
            for (int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(inventory, i + n2 * 9 + 9, 8 + i * 18, 84 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.addSlot(new Slot(inventory, n2, 8 + n2 * 18, 142));
        }
        this.addDataSlot(this.selectedBannerPatternIndex);
        this.patternGetter = inventory.player.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
    }

    @Override
    public boolean stillValid(Player player) {
        return LoomMenu.stillValid(this.access, player, Blocks.LOOM);
    }

    @Override
    public boolean clickMenuButton(Player player, int n) {
        if (n >= 0 && n < this.selectablePatterns.size()) {
            this.selectedBannerPatternIndex.set(n);
            this.setupResultSlot(this.selectablePatterns.get(n));
            return true;
        }
        return false;
    }

    private List<Holder<BannerPattern>> getSelectablePatterns(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return (List)this.patternGetter.get(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        }
        Item item = itemStack.getItem();
        if (item instanceof BannerPatternItem) {
            BannerPatternItem bannerPatternItem = (BannerPatternItem)item;
            return (List)this.patternGetter.get(bannerPatternItem.getBannerPattern()).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        }
        return List.of();
    }

    private boolean isValidPatternIndex(int n) {
        return n >= 0 && n < this.selectablePatterns.size();
    }

    @Override
    public void slotsChanged(Container container) {
        int n;
        Object object;
        Holder<BannerPattern> holder;
        ItemStack itemStack = this.bannerSlot.getItem();
        ItemStack itemStack2 = this.dyeSlot.getItem();
        ItemStack itemStack3 = this.patternSlot.getItem();
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            this.resultSlot.set(ItemStack.EMPTY);
            this.selectablePatterns = List.of();
            this.selectedBannerPatternIndex.set(-1);
            return;
        }
        int n2 = this.selectedBannerPatternIndex.get();
        boolean bl = this.isValidPatternIndex(n2);
        List<Holder<BannerPattern>> list = this.selectablePatterns;
        this.selectablePatterns = this.getSelectablePatterns(itemStack3);
        if (this.selectablePatterns.size() == 1) {
            this.selectedBannerPatternIndex.set(0);
            holder = this.selectablePatterns.get(0);
        } else if (!bl) {
            this.selectedBannerPatternIndex.set(-1);
            holder = null;
        } else {
            object = list.get(n2);
            n = this.selectablePatterns.indexOf(object);
            if (n != -1) {
                holder = object;
                this.selectedBannerPatternIndex.set(n);
            } else {
                holder = null;
                this.selectedBannerPatternIndex.set(-1);
            }
        }
        if (holder != null) {
            object = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
            int n3 = n = ((BannerPatternLayers)object).layers().size() >= 6 ? 1 : 0;
            if (n != 0) {
                this.selectedBannerPatternIndex.set(-1);
                this.resultSlot.set(ItemStack.EMPTY);
            } else {
                this.setupResultSlot(holder);
            }
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }
        this.broadcastChanges();
    }

    public List<Holder<BannerPattern>> getSelectablePatterns() {
        return this.selectablePatterns;
    }

    public int getSelectedBannerPatternIndex() {
        return this.selectedBannerPatternIndex.get();
    }

    public void registerUpdateListener(Runnable runnable) {
        this.slotUpdateListener = runnable;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n == this.resultSlot.index) {
                if (!this.moveItemStackTo(itemStack2, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (n == this.dyeSlot.index || n == this.bannerSlot.index || n == this.patternSlot.index ? !this.moveItemStackTo(itemStack2, 4, 40, false) : (itemStack2.getItem() instanceof BannerItem ? !this.moveItemStackTo(itemStack2, this.bannerSlot.index, this.bannerSlot.index + 1, false) : (itemStack2.getItem() instanceof DyeItem ? !this.moveItemStackTo(itemStack2, this.dyeSlot.index, this.dyeSlot.index + 1, false) : (itemStack2.getItem() instanceof BannerPatternItem ? !this.moveItemStackTo(itemStack2, this.patternSlot.index, this.patternSlot.index + 1, false) : (n >= 4 && n < 31 ? !this.moveItemStackTo(itemStack2, 31, 40, false) : n >= 31 && n < 40 && !this.moveItemStackTo(itemStack2, 4, 31, false)))))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.inputContainer));
    }

    private void setupResultSlot(Holder<BannerPattern> holder) {
        ItemStack itemStack = this.bannerSlot.getItem();
        ItemStack itemStack2 = this.dyeSlot.getItem();
        ItemStack itemStack3 = ItemStack.EMPTY;
        if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            itemStack3 = itemStack.copyWithCount(1);
            DyeColor dyeColor = ((DyeItem)itemStack2.getItem()).getDyeColor();
            itemStack3.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, bannerPatternLayers -> new BannerPatternLayers.Builder().addAll((BannerPatternLayers)bannerPatternLayers).add(holder, dyeColor).build());
        }
        if (!ItemStack.matches(itemStack3, this.resultSlot.getItem())) {
            this.resultSlot.set(itemStack3);
        }
    }

    public Slot getBannerSlot() {
        return this.bannerSlot;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getPatternSlot() {
        return this.patternSlot;
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }
}

