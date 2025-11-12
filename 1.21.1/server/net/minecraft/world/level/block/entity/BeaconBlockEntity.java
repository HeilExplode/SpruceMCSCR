/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity
extends BlockEntity
implements MenuProvider,
Nameable {
    private static final int MAX_LEVELS = 4;
    public static final List<List<Holder<MobEffect>>> BEACON_EFFECTS = List.of(List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED), List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP), List.of(MobEffects.DAMAGE_BOOST), List.of(MobEffects.REGENERATION));
    private static final Set<Holder<MobEffect>> VALID_EFFECTS = BEACON_EFFECTS.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    public static final int DATA_LEVELS = 0;
    public static final int DATA_PRIMARY = 1;
    public static final int DATA_SECONDARY = 2;
    public static final int NUM_DATA_VALUES = 3;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
    private static final String TAG_PRIMARY = "primary_effect";
    private static final String TAG_SECONDARY = "secondary_effect";
    List<BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    int levels;
    private int lastCheckY;
    @Nullable
    Holder<MobEffect> primaryPower;
    @Nullable
    Holder<MobEffect> secondaryPower;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData(){

        @Override
        public int get(int n) {
            return switch (n) {
                case 0 -> BeaconBlockEntity.this.levels;
                case 1 -> BeaconMenu.encodeEffect(BeaconBlockEntity.this.primaryPower);
                case 2 -> BeaconMenu.encodeEffect(BeaconBlockEntity.this.secondaryPower);
                default -> 0;
            };
        }

        @Override
        public void set(int n, int n2) {
            switch (n) {
                case 0: {
                    BeaconBlockEntity.this.levels = n2;
                    break;
                }
                case 1: {
                    if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                        BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }
                    BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(n2));
                    break;
                }
                case 2: {
                    BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(n2));
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Nullable
    static Holder<MobEffect> filterEffect(@Nullable Holder<MobEffect> holder) {
        return VALID_EFFECTS.contains(holder) ? holder : null;
    }

    public BeaconBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BEACON, blockPos, blockState);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, BeaconBlockEntity beaconBlockEntity) {
        int n;
        BlockPos blockPos2;
        int n2 = blockPos.getX();
        int n3 = blockPos.getY();
        int n4 = blockPos.getZ();
        if (beaconBlockEntity.lastCheckY < n3) {
            blockPos2 = blockPos;
            beaconBlockEntity.checkingBeamSections = Lists.newArrayList();
            beaconBlockEntity.lastCheckY = blockPos2.getY() - 1;
        } else {
            blockPos2 = new BlockPos(n2, beaconBlockEntity.lastCheckY + 1, n4);
        }
        BeaconBeamSection beaconBeamSection = beaconBlockEntity.checkingBeamSections.isEmpty() ? null : beaconBlockEntity.checkingBeamSections.get(beaconBlockEntity.checkingBeamSections.size() - 1);
        int n5 = level.getHeight(Heightmap.Types.WORLD_SURFACE, n2, n4);
        for (n = 0; n < 10 && blockPos2.getY() <= n5; ++n) {
            block18: {
                BlockState blockState2;
                block16: {
                    int n6;
                    block17: {
                        blockState2 = level.getBlockState(blockPos2);
                        Block block = blockState2.getBlock();
                        if (!(block instanceof BeaconBeamBlock)) break block16;
                        BeaconBeamBlock beaconBeamBlock = (BeaconBeamBlock)((Object)block);
                        n6 = beaconBeamBlock.getColor().getTextureDiffuseColor();
                        if (beaconBlockEntity.checkingBeamSections.size() > 1) break block17;
                        beaconBeamSection = new BeaconBeamSection(n6);
                        beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
                        break block18;
                    }
                    if (beaconBeamSection == null) break block18;
                    if (n6 == beaconBeamSection.color) {
                        beaconBeamSection.increaseHeight();
                    } else {
                        beaconBeamSection = new BeaconBeamSection(FastColor.ARGB32.average(beaconBeamSection.color, n6));
                        beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
                    }
                    break block18;
                }
                if (beaconBeamSection != null && (blockState2.getLightBlock(level, blockPos2) < 15 || blockState2.is(Blocks.BEDROCK))) {
                    beaconBeamSection.increaseHeight();
                } else {
                    beaconBlockEntity.checkingBeamSections.clear();
                    beaconBlockEntity.lastCheckY = n5;
                    break;
                }
            }
            blockPos2 = blockPos2.above();
            ++beaconBlockEntity.lastCheckY;
        }
        n = beaconBlockEntity.levels;
        if (level.getGameTime() % 80L == 0L) {
            if (!beaconBlockEntity.beamSections.isEmpty()) {
                beaconBlockEntity.levels = BeaconBlockEntity.updateBase(level, n2, n3, n4);
            }
            if (beaconBlockEntity.levels > 0 && !beaconBlockEntity.beamSections.isEmpty()) {
                BeaconBlockEntity.applyEffects(level, blockPos, beaconBlockEntity.levels, beaconBlockEntity.primaryPower, beaconBlockEntity.secondaryPower);
                BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_AMBIENT);
            }
        }
        if (beaconBlockEntity.lastCheckY >= n5) {
            beaconBlockEntity.lastCheckY = level.getMinBuildHeight() - 1;
            boolean bl = n > 0;
            beaconBlockEntity.beamSections = beaconBlockEntity.checkingBeamSections;
            if (!level.isClientSide) {
                boolean bl2;
                boolean bl3 = bl2 = beaconBlockEntity.levels > 0;
                if (!bl && bl2) {
                    BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_ACTIVATE);
                    for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(n2, n3, n4, n2, n3 - 4, n4).inflate(10.0, 5.0, 10.0))) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverPlayer, beaconBlockEntity.levels);
                    }
                } else if (bl && !bl2) {
                    BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level level, int n, int n2, int n3) {
        int n4;
        int n5 = 0;
        int n6 = 1;
        while (n6 <= 4 && (n4 = n2 - n6) >= level.getMinBuildHeight()) {
            boolean bl = true;
            block1: for (int i = n - n6; i <= n + n6 && bl; ++i) {
                for (int j = n3 - n6; j <= n3 + n6; ++j) {
                    if (level.getBlockState(new BlockPos(i, n4, j)).is(BlockTags.BEACON_BASE_BLOCKS)) continue;
                    bl = false;
                    continue block1;
                }
            }
            if (!bl) break;
            n5 = n6++;
        }
        return n5;
    }

    @Override
    public void setRemoved() {
        BeaconBlockEntity.playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level level, BlockPos blockPos, int n, @Nullable Holder<MobEffect> holder, @Nullable Holder<MobEffect> holder2) {
        if (level.isClientSide || holder == null) {
            return;
        }
        double d = n * 10 + 10;
        int n2 = 0;
        if (n >= 4 && Objects.equals(holder, holder2)) {
            n2 = 1;
        }
        int n3 = (9 + n * 2) * 20;
        AABB aABB = new AABB(blockPos).inflate(d).expandTowards(0.0, level.getHeight(), 0.0);
        List<Player> list = level.getEntitiesOfClass(Player.class, aABB);
        for (Player player : list) {
            player.addEffect(new MobEffectInstance(holder, n3, n2, true, true));
        }
        if (n >= 4 && !Objects.equals(holder, holder2) && holder2 != null) {
            for (Player player : list) {
                player.addEffect(new MobEffectInstance(holder2, n3, 0, true, true));
            }
        }
    }

    public static void playSound(Level level, BlockPos blockPos, SoundEvent soundEvent) {
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public List<BeaconBeamSection> getBeamSections() {
        return this.levels == 0 ? ImmutableList.of() : this.beamSections;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    private static void storeEffect(CompoundTag compoundTag, String string, @Nullable Holder<MobEffect> holder) {
        if (holder != null) {
            holder.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(string, resourceKey.location().toString()));
        }
    }

    @Nullable
    private static Holder<MobEffect> loadEffect(CompoundTag compoundTag, String string) {
        if (compoundTag.contains(string, 8)) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString(string));
            if (resourceLocation == null) {
                return null;
            }
            return BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation).map(BeaconBlockEntity::filterEffect).orElse(null);
        }
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.primaryPower = BeaconBlockEntity.loadEffect(compoundTag, TAG_PRIMARY);
        this.secondaryPower = BeaconBlockEntity.loadEffect(compoundTag, TAG_SECONDARY);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = BeaconBlockEntity.parseCustomNameSafe(compoundTag.getString("CustomName"), provider);
        }
        this.lockKey = LockCode.fromTag(compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        BeaconBlockEntity.storeEffect(compoundTag, TAG_PRIMARY, this.primaryPower);
        BeaconBlockEntity.storeEffect(compoundTag, TAG_SECONDARY, this.secondaryPower);
        compoundTag.putInt("Levels", this.levels);
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
        }
        this.lockKey.addToTag(compoundTag);
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
    @Nullable
    public AbstractContainerMenu createMenu(int n, Inventory inventory, Player player) {
        if (BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName())) {
            return new BeaconMenu(n, inventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()));
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return DEFAULT_NAME;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        this.name = dataComponentInput.get(DataComponents.CUSTOM_NAME);
        this.lockKey = dataComponentInput.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
        if (!this.lockKey.equals(LockCode.NO_LOCK)) {
            builder.set(DataComponents.LOCK, this.lockKey);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        compoundTag.remove("CustomName");
        compoundTag.remove("Lock");
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.lastCheckY = level.getMinBuildHeight() - 1;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public static class BeaconBeamSection {
        final int color;
        private int height;

        public BeaconBeamSection(int n) {
            this.color = n;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public int getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}

