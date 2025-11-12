/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingTableBlockEntity
extends BlockEntity
implements Nameable {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    @Nullable
    private Component name;

    public EnchantingTableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.ENCHANTING_TABLE, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (this.hasCustomName()) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = EnchantingTableBlockEntity.parseCustomNameSafe(compoundTag.getString("CustomName"), provider);
        }
    }

    public static void bookAnimationTick(Level level, BlockPos blockPos, BlockState blockState, EnchantingTableBlockEntity enchantingTableBlockEntity) {
        float f;
        enchantingTableBlockEntity.oOpen = enchantingTableBlockEntity.open;
        enchantingTableBlockEntity.oRot = enchantingTableBlockEntity.rot;
        Player player = level.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double d = player.getX() - ((double)blockPos.getX() + 0.5);
            double d2 = player.getZ() - ((double)blockPos.getZ() + 0.5);
            enchantingTableBlockEntity.tRot = (float)Mth.atan2(d2, d);
            enchantingTableBlockEntity.open += 0.1f;
            if (enchantingTableBlockEntity.open < 0.5f || RANDOM.nextInt(40) == 0) {
                float f2 = enchantingTableBlockEntity.flipT;
                do {
                    enchantingTableBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f2 == enchantingTableBlockEntity.flipT);
            }
        } else {
            enchantingTableBlockEntity.tRot += 0.02f;
            enchantingTableBlockEntity.open -= 0.1f;
        }
        while (enchantingTableBlockEntity.rot >= (float)Math.PI) {
            enchantingTableBlockEntity.rot -= (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.rot < (float)(-Math.PI)) {
            enchantingTableBlockEntity.rot += (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.tRot >= (float)Math.PI) {
            enchantingTableBlockEntity.tRot -= (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.tRot < (float)(-Math.PI)) {
            enchantingTableBlockEntity.tRot += (float)Math.PI * 2;
        }
        for (f = enchantingTableBlockEntity.tRot - enchantingTableBlockEntity.rot; f >= (float)Math.PI; f -= (float)Math.PI * 2) {
        }
        while (f < (float)(-Math.PI)) {
            f += (float)Math.PI * 2;
        }
        enchantingTableBlockEntity.rot += f * 0.4f;
        enchantingTableBlockEntity.open = Mth.clamp(enchantingTableBlockEntity.open, 0.0f, 1.0f);
        ++enchantingTableBlockEntity.time;
        enchantingTableBlockEntity.oFlip = enchantingTableBlockEntity.flip;
        float f3 = (enchantingTableBlockEntity.flipT - enchantingTableBlockEntity.flip) * 0.4f;
        float f4 = 0.2f;
        f3 = Mth.clamp(f3, -0.2f, 0.2f);
        enchantingTableBlockEntity.flipA += (f3 - enchantingTableBlockEntity.flipA) * 0.9f;
        enchantingTableBlockEntity.flip += enchantingTableBlockEntity.flipA;
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return Component.translatable("container.enchant");
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        this.name = dataComponentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        compoundTag.remove("CustomName");
    }
}

