/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource
implements PositionSource {
    public static final MapCodec<EntityPositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid), (App)Codec.FLOAT.fieldOf("y_offset").orElse((Object)Float.valueOf(0.0f)).forGetter(entityPositionSource -> Float.valueOf(entityPositionSource.yOffset))).apply((Applicative)instance, (uUID, f) -> new EntityPositionSource((Either<Entity, Either<UUID, Integer>>)Either.right((Object)Either.left((Object)uUID)), f.floatValue())));
    public static final StreamCodec<ByteBuf, EntityPositionSource> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, EntityPositionSource::getId, ByteBufCodecs.FLOAT, entityPositionSource -> Float.valueOf(entityPositionSource.yOffset), (n, f) -> new EntityPositionSource((Either<Entity, Either<UUID, Integer>>)Either.right((Object)Either.right((Object)n)), f.floatValue()));
    private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
    private final float yOffset;

    public EntityPositionSource(Entity entity, float f) {
        this((Either<Entity, Either<UUID, Integer>>)Either.left((Object)entity), f);
    }

    private EntityPositionSource(Either<Entity, Either<UUID, Integer>> either, float f) {
        this.entityOrUuidOrId = either;
        this.yOffset = f;
    }

    @Override
    public Optional<Vec3> getPosition(Level level) {
        if (this.entityOrUuidOrId.left().isEmpty()) {
            this.resolveEntity(level);
        }
        return this.entityOrUuidOrId.left().map(entity -> entity.position().add(0.0, this.yOffset, 0.0));
    }

    private void resolveEntity(Level level) {
        ((Optional)this.entityOrUuidOrId.map(Optional::of, either -> Optional.ofNullable((Entity)either.map(uUID -> {
            Entity entity;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                entity = serverLevel.getEntity((UUID)uUID);
            } else {
                entity = null;
            }
            return entity;
        }, level::getEntity)))).ifPresent(entity -> {
            this.entityOrUuidOrId = Either.left((Object)entity);
        });
    }

    private UUID getUuid() {
        return (UUID)this.entityOrUuidOrId.map(Entity::getUUID, either -> (UUID)either.map(Function.identity(), n -> {
            throw new RuntimeException("Unable to get entityId from uuid");
        }));
    }

    private int getId() {
        return (Integer)this.entityOrUuidOrId.map(Entity::getId, either -> (Integer)either.map(uUID -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
        }, Function.identity()));
    }

    public PositionSourceType<EntityPositionSource> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type
    implements PositionSourceType<EntityPositionSource> {
        @Override
        public MapCodec<EntityPositionSource> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, EntityPositionSource> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

