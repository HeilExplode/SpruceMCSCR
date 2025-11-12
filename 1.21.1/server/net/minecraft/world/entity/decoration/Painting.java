/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Painting
extends HangingEntity
implements VariantHolder<Holder<PaintingVariant>> {
    private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
    public static final MapCodec<Holder<PaintingVariant>> VARIANT_MAP_CODEC = PaintingVariant.CODEC.fieldOf("variant");
    public static final Codec<Holder<PaintingVariant>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();
    public static final float DEPTH = 0.0625f;

    public Painting(EntityType<? extends Painting> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PAINTING_VARIANT_ID, (Holder)this.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getAny().orElseThrow());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_PAINTING_VARIANT_ID.equals(entityDataAccessor)) {
            this.recalculateBoundingBox();
        }
    }

    @Override
    public void setVariant(Holder<PaintingVariant> holder) {
        this.entityData.set(DATA_PAINTING_VARIANT_ID, holder);
    }

    @Override
    public Holder<PaintingVariant> getVariant() {
        return this.entityData.get(DATA_PAINTING_VARIANT_ID);
    }

    public static Optional<Painting> create(Level level, BlockPos blockPos, Direction direction) {
        Painting painting = new Painting(level, blockPos);
        ArrayList<Holder> arrayList = new ArrayList<Holder>();
        level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(arrayList::add);
        if (arrayList.isEmpty()) {
            return Optional.empty();
        }
        painting.setDirection(direction);
        arrayList.removeIf(holder -> {
            painting.setVariant((Holder<PaintingVariant>)holder);
            return !painting.survives();
        });
        if (arrayList.isEmpty()) {
            return Optional.empty();
        }
        int n = arrayList.stream().mapToInt(Painting::variantArea).max().orElse(0);
        arrayList.removeIf(holder -> Painting.variantArea(holder) < n);
        Optional optional = Util.getRandomSafe(arrayList, painting.random);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        painting.setVariant((Holder)optional.get());
        painting.setDirection(direction);
        return Optional.of(painting);
    }

    private static int variantArea(Holder<PaintingVariant> holder) {
        return holder.value().area();
    }

    private Painting(Level level, BlockPos blockPos) {
        super((EntityType<? extends HangingEntity>)EntityType.PAINTING, level, blockPos);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction, Holder<PaintingVariant> holder) {
        this(level, blockPos);
        this.setVariant(holder);
        this.setDirection(direction);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        VARIANT_CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.getVariant()).ifSuccess(tag -> compoundTag.merge((CompoundTag)tag));
        compoundTag.putByte("facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        VARIANT_CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), (Object)compoundTag).ifSuccess(this::setVariant);
        this.direction = Direction.from2DDataValue(compoundTag.getByte("facing"));
        super.readAdditionalSaveData(compoundTag);
        this.setDirection(this.direction);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
        float f = 0.46875f;
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        PaintingVariant paintingVariant = (PaintingVariant)this.getVariant().value();
        double d = this.offsetForPaintingSize(paintingVariant.width());
        double d2 = this.offsetForPaintingSize(paintingVariant.height());
        Direction direction2 = direction.getCounterClockWise();
        Vec3 vec32 = vec3.relative(direction2, d).relative(Direction.UP, d2);
        Direction.Axis axis = direction.getAxis();
        double d3 = axis == Direction.Axis.X ? 0.0625 : (double)paintingVariant.width();
        double d4 = paintingVariant.height();
        double d5 = axis == Direction.Axis.Z ? 0.0625 : (double)paintingVariant.width();
        return AABB.ofSize(vec32, d3, d4, d5);
    }

    private double offsetForPaintingSize(int n) {
        return n % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        Player player;
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof Player && (player = (Player)entity).hasInfiniteMaterials()) {
            return;
        }
        this.spawnAtLocation(Items.PAINTING);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void moveTo(double d, double d2, double d3, float f, float f2) {
        this.setPos(d, d2, d3);
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        this.setPos(d, d2, d3);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    @Override
    public /* synthetic */ void setVariant(Object object) {
        this.setVariant((Holder)object);
    }
}

