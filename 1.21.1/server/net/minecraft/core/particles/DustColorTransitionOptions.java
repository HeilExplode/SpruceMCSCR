/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustColorTransitionOptions
extends ScalableParticleOptionsBase {
    public static final Vector3f SCULK_PARTICLE_COLOR = Vec3.fromRGB24(3790560).toVector3f();
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0f);
    public static final MapCodec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.VECTOR3F.fieldOf("from_color").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.fromColor), (App)ExtraCodecs.VECTOR3F.fieldOf("to_color").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.toColor), (App)SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)).apply((Applicative)instance, DustColorTransitionOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DustColorTransitionOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VECTOR3F, dustColorTransitionOptions -> dustColorTransitionOptions.fromColor, ByteBufCodecs.VECTOR3F, dustColorTransitionOptions -> dustColorTransitionOptions.toColor, ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale, DustColorTransitionOptions::new);
    private final Vector3f fromColor;
    private final Vector3f toColor;

    public DustColorTransitionOptions(Vector3f vector3f, Vector3f vector3f2, float f) {
        super(f);
        this.fromColor = vector3f;
        this.toColor = vector3f2;
    }

    public Vector3f getFromColor() {
        return this.fromColor;
    }

    public Vector3f getToColor() {
        return this.toColor;
    }

    public ParticleType<DustColorTransitionOptions> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}

