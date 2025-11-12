/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record SpawnParticlesEffect(ParticleOptions particle, PositionSource horizontalPosition, PositionSource verticalPosition, VelocitySource horizontalVelocity, VelocitySource verticalVelocity, FloatProvider speed) implements EnchantmentEntityEffect
{
    public static final MapCodec<SpawnParticlesEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticlesEffect::particle), (App)PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEffect::horizontalPosition), (App)PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEffect::verticalPosition), (App)VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEffect::horizontalVelocity), (App)VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEffect::verticalVelocity), (App)FloatProvider.CODEC.optionalFieldOf("speed", (Object)ConstantFloat.ZERO).forGetter(SpawnParticlesEffect::speed)).apply((Applicative)instance, SpawnParticlesEffect::new));

    public static PositionSource offsetFromEntityPosition(float f) {
        return new PositionSource(PositionSourceType.ENTITY_POSITION, f, 1.0f);
    }

    public static PositionSource inBoundingBox() {
        return new PositionSource(PositionSourceType.BOUNDING_BOX, 0.0f, 1.0f);
    }

    public static VelocitySource movementScaled(float f) {
        return new VelocitySource(f, ConstantFloat.ZERO);
    }

    public static VelocitySource fixedVelocity(FloatProvider floatProvider) {
        return new VelocitySource(0.0f, floatProvider);
    }

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        RandomSource randomSource = entity.getRandom();
        Vec3 vec32 = entity.getKnownMovement();
        float f = entity.getBbWidth();
        float f2 = entity.getBbHeight();
        serverLevel.sendParticles(this.particle, this.horizontalPosition.getCoordinate(vec3.x(), vec3.x(), f, randomSource), this.verticalPosition.getCoordinate(vec3.y(), vec3.y() + (double)(f2 / 2.0f), f2, randomSource), this.horizontalPosition.getCoordinate(vec3.z(), vec3.z(), f, randomSource), 0, this.horizontalVelocity.getVelocity(vec32.x(), randomSource), this.verticalVelocity.getVelocity(vec32.y(), randomSource), this.horizontalVelocity.getVelocity(vec32.z(), randomSource), this.speed.sample(randomSource));
    }

    public MapCodec<SpawnParticlesEffect> codec() {
        return CODEC;
    }

    public record PositionSource(PositionSourceType type, float offset, float scale) {
        public static final MapCodec<PositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PositionSourceType.CODEC.fieldOf("type").forGetter(PositionSource::type), (App)Codec.FLOAT.optionalFieldOf("offset", (Object)Float.valueOf(0.0f)).forGetter(PositionSource::offset), (App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", (Object)Float.valueOf(1.0f)).forGetter(PositionSource::scale)).apply((Applicative)instance, PositionSource::new)).validate(positionSource -> {
            if (positionSource.type() == PositionSourceType.ENTITY_POSITION && positionSource.scale() != 1.0f) {
                return DataResult.error(() -> "Cannot scale an entity position coordinate source");
            }
            return DataResult.success((Object)positionSource);
        });

        public double getCoordinate(double d, double d2, float f, RandomSource randomSource) {
            return this.type.getCoordinate(d, d2, f * this.scale, randomSource) + (double)this.offset;
        }
    }

    public record VelocitySource(float movementScale, FloatProvider base) {
        public static final MapCodec<VelocitySource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.optionalFieldOf("movement_scale", (Object)Float.valueOf(0.0f)).forGetter(VelocitySource::movementScale), (App)FloatProvider.CODEC.optionalFieldOf("base", (Object)ConstantFloat.ZERO).forGetter(VelocitySource::base)).apply((Applicative)instance, VelocitySource::new));

        public double getVelocity(double d, RandomSource randomSource) {
            return d * (double)this.movementScale + (double)this.base.sample(randomSource);
        }
    }

    public static enum PositionSourceType implements StringRepresentable
    {
        ENTITY_POSITION("entity_position", (d, d2, f, randomSource) -> d),
        BOUNDING_BOX("in_bounding_box", (d, d2, f, randomSource) -> d2 + (randomSource.nextDouble() - 0.5) * (double)f);

        public static final Codec<PositionSourceType> CODEC;
        private final String id;
        private final CoordinateSource source;

        private PositionSourceType(String string2, CoordinateSource coordinateSource) {
            this.id = string2;
            this.source = coordinateSource;
        }

        public double getCoordinate(double d, double d2, float f, RandomSource randomSource) {
            return this.source.getCoordinate(d, d2, f, randomSource);
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(PositionSourceType::values);
        }

        @FunctionalInterface
        static interface CoordinateSource {
            public double getCoordinate(double var1, double var3, float var5, RandomSource var6);
        }
    }
}

