/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 */
package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VaultServerData {
    static final String TAG_NAME = "server_data";
    static Codec<VaultServerData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("rewarded_players", Set.of()).forGetter(vaultServerData -> vaultServerData.rewardedPlayers), (App)Codec.LONG.lenientOptionalFieldOf("state_updating_resumes_at", (Object)0L).forGetter(vaultServerData -> vaultServerData.stateUpdatingResumesAt), (App)ItemStack.CODEC.listOf().lenientOptionalFieldOf("items_to_eject", List.of()).forGetter(vaultServerData -> vaultServerData.itemsToEject), (App)Codec.INT.lenientOptionalFieldOf("total_ejections_needed", (Object)0).forGetter(vaultServerData -> vaultServerData.totalEjectionsNeeded)).apply((Applicative)instance, VaultServerData::new));
    private static final int MAX_REWARD_PLAYERS = 128;
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet();
    private long stateUpdatingResumesAt;
    private final List<ItemStack> itemsToEject = new ObjectArrayList();
    private long lastInsertFailTimestamp;
    private int totalEjectionsNeeded;
    boolean isDirty;

    VaultServerData(Set<UUID> set, long l, List<ItemStack> list, int n) {
        this.rewardedPlayers.addAll(set);
        this.stateUpdatingResumesAt = l;
        this.itemsToEject.addAll(list);
        this.totalEjectionsNeeded = n;
    }

    VaultServerData() {
    }

    void setLastInsertFailTimestamp(long l) {
        this.lastInsertFailTimestamp = l;
    }

    long getLastInsertFailTimestamp() {
        return this.lastInsertFailTimestamp;
    }

    Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers;
    }

    boolean hasRewardedPlayer(Player player) {
        return this.rewardedPlayers.contains(player.getUUID());
    }

    @VisibleForTesting
    public void addToRewardedPlayers(Player player) {
        Iterator<UUID> iterator;
        this.rewardedPlayers.add(player.getUUID());
        if (this.rewardedPlayers.size() > 128 && (iterator = this.rewardedPlayers.iterator()).hasNext()) {
            iterator.next();
            iterator.remove();
        }
        this.markChanged();
    }

    long stateUpdatingResumesAt() {
        return this.stateUpdatingResumesAt;
    }

    void pauseStateUpdatingUntil(long l) {
        this.stateUpdatingResumesAt = l;
        this.markChanged();
    }

    List<ItemStack> getItemsToEject() {
        return this.itemsToEject;
    }

    void markEjectionFinished() {
        this.totalEjectionsNeeded = 0;
        this.markChanged();
    }

    void setItemsToEject(List<ItemStack> list) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(list);
        this.totalEjectionsNeeded = this.itemsToEject.size();
        this.markChanged();
    }

    ItemStack getNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return Objects.requireNonNullElse(this.itemsToEject.get(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    ItemStack popNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.markChanged();
        return Objects.requireNonNullElse(this.itemsToEject.remove(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    void set(VaultServerData vaultServerData) {
        this.stateUpdatingResumesAt = vaultServerData.stateUpdatingResumesAt();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(vaultServerData.itemsToEject);
        this.rewardedPlayers.clear();
        this.rewardedPlayers.addAll(vaultServerData.rewardedPlayers);
    }

    private void markChanged() {
        this.isDirty = true;
    }

    public float ejectionProgress() {
        if (this.totalEjectionsNeeded == 1) {
            return 1.0f;
        }
        return 1.0f - Mth.inverseLerp(this.getItemsToEject().size(), 1.0f, this.totalEjectionsNeeded);
    }
}

