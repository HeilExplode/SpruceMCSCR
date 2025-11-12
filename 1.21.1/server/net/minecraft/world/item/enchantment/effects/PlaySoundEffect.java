/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record PlaySoundEffect(Holder<SoundEvent> soundEvent, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffect
{
    public static final MapCodec<PlaySoundEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySoundEffect::soundEvent), (App)FloatProvider.codec(1.0E-5f, 10.0f).fieldOf("volume").forGetter(PlaySoundEffect::volume), (App)FloatProvider.codec(1.0E-5f, 2.0f).fieldOf("pitch").forGetter(PlaySoundEffect::pitch)).apply((Applicative)instance, PlaySoundEffect::new));

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        RandomSource randomSource = entity.getRandom();
        if (!entity.isSilent()) {
            serverLevel.playSound(null, vec3.x(), vec3.y(), vec3.z(), this.soundEvent, entity.getSoundSource(), this.volume.sample(randomSource), this.pitch.sample(randomSource));
        }
    }

    public MapCodec<PlaySoundEffect> codec() {
        return CODEC;
    }
}

