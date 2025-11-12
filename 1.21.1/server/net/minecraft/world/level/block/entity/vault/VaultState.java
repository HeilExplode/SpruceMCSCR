/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.phys.Vec3;

public enum VaultState implements StringRepresentable
{
    INACTIVE("inactive", LightLevel.HALF_LIT){

        @Override
        protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
            vaultSharedData.setDisplayItem(ItemStack.EMPTY);
            serverLevel.levelEvent(3016, blockPos, bl ? 1 : 0);
        }
    }
    ,
    ACTIVE("active", LightLevel.LIT){

        @Override
        protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
            if (!vaultSharedData.hasDisplayItem()) {
                VaultBlockEntity.Server.cycleDisplayItemFromLootTable(serverLevel, this, vaultConfig, vaultSharedData, blockPos);
            }
            serverLevel.levelEvent(3015, blockPos, bl ? 1 : 0);
        }
    }
    ,
    UNLOCKING("unlocking", LightLevel.LIT){

        @Override
        protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
            serverLevel.playSound(null, blockPos, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
        }
    }
    ,
    EJECTING("ejecting", LightLevel.LIT){

        @Override
        protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
            serverLevel.playSound(null, blockPos, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
        }

        @Override
        protected void onExit(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
            serverLevel.playSound(null, blockPos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final LightLevel lightLevel;

    VaultState(String string2, LightLevel lightLevel) {
        this.stateName = string2;
        this.lightLevel = lightLevel;
    }

    @Override
    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel.value;
    }

    public VaultState tickAndGetNext(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> VaultState.updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.activationRange());
            case 1 -> VaultState.updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.deactivationRange());
            case 2 -> {
                vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 20L);
                yield EJECTING;
            }
            case 3 -> {
                if (vaultServerData.getItemsToEject().isEmpty()) {
                    vaultServerData.markEjectionFinished();
                    yield VaultState.updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.deactivationRange());
                }
                float var6_6 = vaultServerData.ejectionProgress();
                this.ejectResultItem(serverLevel, blockPos, vaultServerData.popNextItemToEject(), var6_6);
                vaultSharedData.setDisplayItem(vaultServerData.getNextItemToEject());
                boolean var7_7 = vaultServerData.getItemsToEject().isEmpty();
                int var8_8 = var7_7 ? 20 : 20;
                vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + (long)var8_8);
                yield EJECTING;
            }
        };
    }

    private static VaultState updateStateForConnectedPlayers(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, double d) {
        vaultSharedData.updateConnectedPlayersWithinRange(serverLevel, blockPos, vaultServerData, vaultConfig, d);
        vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 20L);
        return vaultSharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onTransition(ServerLevel serverLevel, BlockPos blockPos, VaultState vaultState, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
        this.onExit(serverLevel, blockPos, vaultConfig, vaultSharedData);
        vaultState.onEnter(serverLevel, blockPos, vaultConfig, vaultSharedData, bl);
    }

    protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData, boolean bl) {
    }

    protected void onExit(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
    }

    private void ejectResultItem(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, float f) {
        DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack, 2, Direction.UP, Vec3.atBottomCenterOf(blockPos).relative(Direction.UP, 1.2));
        serverLevel.levelEvent(3017, blockPos, 0);
        serverLevel.playSound(null, blockPos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0f, 0.8f + 0.4f * f);
    }

    static enum LightLevel {
        HALF_LIT(6),
        LIT(12);

        final int value;

        private LightLevel(int n2) {
            this.value = n2;
        }
    }
}

