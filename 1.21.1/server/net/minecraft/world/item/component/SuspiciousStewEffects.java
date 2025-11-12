/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record SuspiciousStewEffects(List<Entry> effects) {
    public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
    public static final Codec<SuspiciousStewEffects> CODEC = Entry.CODEC.listOf().xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
    public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = Entry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

    public SuspiciousStewEffects withEffectAdded(Entry entry) {
        return new SuspiciousStewEffects(Util.copyAndAdd(this.effects, entry));
    }

    public record Entry(Holder<MobEffect> effect, int duration) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MobEffect.CODEC.fieldOf("id").forGetter(Entry::effect), (App)Codec.INT.lenientOptionalFieldOf("duration", (Object)160).forGetter(Entry::duration)).apply((Applicative)instance, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(MobEffect.STREAM_CODEC, Entry::effect, ByteBufCodecs.VAR_INT, Entry::duration, Entry::new);

        public MobEffectInstance createEffectInstance() {
            return new MobEffectInstance(this.effect, this.duration);
        }
    }
}

