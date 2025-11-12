/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

public class BeehiveBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_FLOWER_POS = "flower_pos";
    private static final String BEES = "bees";
    static final List<String> IGNORED_BEE_TAGS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "SleepingX", "SleepingY", "SleepingZ", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "hive_pos", "Passengers", "leash", "UUID");
    public static final int MAX_OCCUPANTS = 3;
    private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
    private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
    private final List<BeeData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos;

    public BeehiveBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BEEHIVE, blockPos, blockState);
    }

    @Override
    public void setChanged() {
        if (this.isFireNearby()) {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeeReleaseStatus.EMERGENCY);
        }
        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
            if (!(this.level.getBlockState(blockPos).getBlock() instanceof FireBlock)) continue;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player player, BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        List<Entity> list = this.releaseAllOccupants(blockState, beeReleaseStatus);
        if (player != null) {
            for (Entity entity : list) {
                if (!(entity instanceof Bee)) continue;
                Bee bee = (Bee)entity;
                if (!(player.position().distanceToSqr(entity.position()) <= 16.0)) continue;
                if (!this.isSedated()) {
                    bee.setTarget(player);
                    continue;
                }
                bee.setStayOutOfHiveCountdown(400);
            }
        }
    }

    private List<Entity> releaseAllOccupants(BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        ArrayList arrayList = Lists.newArrayList();
        this.stored.removeIf(beeData -> BeehiveBlockEntity.releaseOccupant(this.level, this.worldPosition, blockState, beeData.toOccupant(), arrayList, beeReleaseStatus, this.savedFlowerPos));
        if (!arrayList.isEmpty()) {
            super.setChanged();
        }
        return arrayList;
    }

    @VisibleForDebug
    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState blockState) {
        return blockState.getValue(BeehiveBlock.HONEY_LEVEL);
    }

    @VisibleForDebug
    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public void addOccupant(Entity entity) {
        if (this.stored.size() >= 3) {
            return;
        }
        entity.stopRiding();
        entity.ejectPassengers();
        this.storeBee(Occupant.of(entity));
        if (this.level != null) {
            Object object;
            if (entity instanceof Bee && ((Bee)(object = (Bee)entity)).hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                this.savedFlowerPos = ((Bee)object).getSavedFlowerPos();
            }
            object = this.getBlockPos();
            this.level.playSound(null, (double)((Vec3i)object).getX(), (double)((Vec3i)object).getY(), (double)((Vec3i)object).getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, (BlockPos)object, GameEvent.Context.of(entity, this.getBlockState()));
        }
        entity.discard();
        super.setChanged();
    }

    public void storeBee(Occupant occupant) {
        this.stored.add(new BeeData(occupant));
    }

    private static boolean releaseOccupant(Level level, BlockPos blockPos, BlockState blockState, Occupant occupant, @Nullable List<Entity> list, BeeReleaseStatus beeReleaseStatus, @Nullable BlockPos blockPos2) {
        boolean bl;
        if ((level.isNight() || level.isRaining()) && beeReleaseStatus != BeeReleaseStatus.EMERGENCY) {
            return false;
        }
        Direction direction = blockState.getValue(BeehiveBlock.FACING);
        BlockPos blockPos3 = blockPos.relative(direction);
        boolean bl2 = bl = !level.getBlockState(blockPos3).getCollisionShape(level, blockPos3).isEmpty();
        if (bl && beeReleaseStatus != BeeReleaseStatus.EMERGENCY) {
            return false;
        }
        Entity entity = occupant.createEntity(level, blockPos);
        if (entity != null) {
            if (entity instanceof Bee) {
                Bee bee = (Bee)entity;
                if (blockPos2 != null && !bee.hasSavedFlowerPos() && level.random.nextFloat() < 0.9f) {
                    bee.setSavedFlowerPos(blockPos2);
                }
                if (beeReleaseStatus == BeeReleaseStatus.HONEY_DELIVERED) {
                    int n;
                    bee.dropOffNectar();
                    if (blockState.is(BlockTags.BEEHIVES, blockStateBase -> blockStateBase.hasProperty(BeehiveBlock.HONEY_LEVEL)) && (n = BeehiveBlockEntity.getHoneyLevel(blockState)) < 5) {
                        int n2;
                        int n3 = n2 = level.random.nextInt(100) == 0 ? 2 : 1;
                        if (n + n2 > 5) {
                            --n2;
                        }
                        level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(BeehiveBlock.HONEY_LEVEL, n + n2));
                    }
                }
                if (list != null) {
                    list.add(bee);
                }
                float f = entity.getBbWidth();
                double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
                double d2 = (double)blockPos.getX() + 0.5 + d * (double)direction.getStepX();
                double d3 = (double)blockPos.getY() + 0.5 - (double)(entity.getBbHeight() / 2.0f);
                double d4 = (double)blockPos.getZ() + 0.5 + d * (double)direction.getStepZ();
                entity.moveTo(d2, d3, d4, entity.getYRot(), entity.getXRot());
            }
            level.playSound(null, blockPos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, level.getBlockState(blockPos)));
            return level.addFreshEntity(entity);
        }
        return false;
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private static void tickOccupants(Level level, BlockPos blockPos, BlockState blockState, List<BeeData> list, @Nullable BlockPos blockPos2) {
        boolean bl = false;
        Iterator<BeeData> iterator = list.iterator();
        while (iterator.hasNext()) {
            BeeReleaseStatus beeReleaseStatus;
            BeeData beeData = iterator.next();
            if (!beeData.tick()) continue;
            BeeReleaseStatus beeReleaseStatus2 = beeReleaseStatus = beeData.hasNectar() ? BeeReleaseStatus.HONEY_DELIVERED : BeeReleaseStatus.BEE_RELEASED;
            if (!BeehiveBlockEntity.releaseOccupant(level, blockPos, blockState, beeData.toOccupant(), null, beeReleaseStatus, blockPos2)) continue;
            bl = true;
            iterator.remove();
        }
        if (bl) {
            BeehiveBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
        BeehiveBlockEntity.tickOccupants(level, blockPos, blockState, beehiveBlockEntity.stored, beehiveBlockEntity.savedFlowerPos);
        if (!beehiveBlockEntity.stored.isEmpty() && level.getRandom().nextDouble() < 0.005) {
            double d = (double)blockPos.getX() + 0.5;
            double d2 = blockPos.getY();
            double d3 = (double)blockPos.getZ() + 0.5;
            level.playSound(null, d, d2, d3, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        DebugPackets.sendHiveInfo(level, blockPos, blockState, beehiveBlockEntity);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.stored.clear();
        if (compoundTag.contains(BEES)) {
            Occupant.LIST_CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(BEES)).resultOrPartial(string -> LOGGER.error("Failed to parse bees: '{}'", string)).ifPresent(list -> list.forEach(this::storeBee));
        }
        this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag, TAG_FLOWER_POS).orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.put(BEES, (Tag)Occupant.LIST_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, this.getBees()).getOrThrow());
        if (this.hasSavedFlowerPos()) {
            compoundTag.put(TAG_FLOWER_POS, NbtUtils.writeBlockPos(this.savedFlowerPos));
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);
        this.stored.clear();
        List list = dataComponentInput.getOrDefault(DataComponents.BEES, List.of());
        list.forEach(this::storeBee);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.BEES, this.getBees());
    }

    @Override
    public void removeComponentsFromTag(CompoundTag compoundTag) {
        super.removeComponentsFromTag(compoundTag);
        compoundTag.remove(BEES);
    }

    private List<Occupant> getBees() {
        return this.stored.stream().map(BeeData::toOccupant).toList();
    }

    public static enum BeeReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY;

    }

    public static final class Occupant
    extends Record {
        final CustomData entityData;
        private final int ticksInHive;
        final int minTicksInHive;
        public static final Codec<Occupant> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CustomData.CODEC.optionalFieldOf("entity_data", (Object)CustomData.EMPTY).forGetter(Occupant::entityData), (App)Codec.INT.fieldOf("ticks_in_hive").forGetter(Occupant::ticksInHive), (App)Codec.INT.fieldOf("min_ticks_in_hive").forGetter(Occupant::minTicksInHive)).apply((Applicative)instance, Occupant::new));
        public static final Codec<List<Occupant>> LIST_CODEC = CODEC.listOf();
        public static final StreamCodec<ByteBuf, Occupant> STREAM_CODEC = StreamCodec.composite(CustomData.STREAM_CODEC, Occupant::entityData, ByteBufCodecs.VAR_INT, Occupant::ticksInHive, ByteBufCodecs.VAR_INT, Occupant::minTicksInHive, Occupant::new);

        public Occupant(CustomData customData, int n, int n2) {
            this.entityData = customData;
            this.ticksInHive = n;
            this.minTicksInHive = n2;
        }

        public static Occupant of(Entity entity) {
            CompoundTag compoundTag = new CompoundTag();
            entity.save(compoundTag);
            IGNORED_BEE_TAGS.forEach(compoundTag::remove);
            boolean bl = compoundTag.getBoolean("HasNectar");
            return new Occupant(CustomData.of(compoundTag), 0, bl ? 2400 : 600);
        }

        public static Occupant create(int n) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
            return new Occupant(CustomData.of(compoundTag), n, 600);
        }

        @Nullable
        public Entity createEntity(Level level, BlockPos blockPos) {
            CompoundTag compoundTag = this.entityData.copyTag();
            IGNORED_BEE_TAGS.forEach(compoundTag::remove);
            Entity entity2 = EntityType.loadEntityRecursive(compoundTag, level, entity -> entity);
            if (entity2 == null || !entity2.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                return null;
            }
            entity2.setNoGravity(true);
            if (entity2 instanceof Bee) {
                Bee bee = (Bee)entity2;
                bee.setHivePos(blockPos);
                Occupant.setBeeReleaseData(this.ticksInHive, bee);
            }
            return entity2;
        }

        private static void setBeeReleaseData(int n, Bee bee) {
            int n2 = bee.getAge();
            if (n2 < 0) {
                bee.setAge(Math.min(0, n2 + n));
            } else if (n2 > 0) {
                bee.setAge(Math.max(0, n2 - n));
            }
            bee.setInLoveTime(Math.max(0, bee.getInLoveTime() - n));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Occupant.class, "entityData;ticksInHive;minTicksInHive", "entityData", "ticksInHive", "minTicksInHive"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Occupant.class, "entityData;ticksInHive;minTicksInHive", "entityData", "ticksInHive", "minTicksInHive"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Occupant.class, "entityData;ticksInHive;minTicksInHive", "entityData", "ticksInHive", "minTicksInHive"}, this, object);
        }

        public CustomData entityData() {
            return this.entityData;
        }

        public int ticksInHive() {
            return this.ticksInHive;
        }

        public int minTicksInHive() {
            return this.minTicksInHive;
        }
    }

    static class BeeData {
        private final Occupant occupant;
        private int ticksInHive;

        BeeData(Occupant occupant) {
            this.occupant = occupant;
            this.ticksInHive = occupant.ticksInHive();
        }

        public boolean tick() {
            return this.ticksInHive++ > this.occupant.minTicksInHive;
        }

        public Occupant toOccupant() {
            return new Occupant(this.occupant.entityData, this.ticksInHive, this.occupant.minTicksInHive);
        }

        public boolean hasNectar() {
            return this.occupant.entityData.getUnsafe().getBoolean("HasNectar");
        }
    }
}

