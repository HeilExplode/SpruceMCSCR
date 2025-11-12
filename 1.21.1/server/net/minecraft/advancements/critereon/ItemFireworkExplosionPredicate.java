/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;

public record ItemFireworkExplosionPredicate(FireworkPredicate predicate) implements SingleComponentItemPredicate<FireworkExplosion>
{
    public static final Codec<ItemFireworkExplosionPredicate> CODEC = FireworkPredicate.CODEC.xmap(ItemFireworkExplosionPredicate::new, ItemFireworkExplosionPredicate::predicate);

    @Override
    public DataComponentType<FireworkExplosion> componentType() {
        return DataComponents.FIREWORK_EXPLOSION;
    }

    @Override
    public boolean matches(ItemStack itemStack, FireworkExplosion fireworkExplosion) {
        return this.predicate.test(fireworkExplosion);
    }

    public record FireworkPredicate(Optional<FireworkExplosion.Shape> shape, Optional<Boolean> twinkle, Optional<Boolean> trail) implements Predicate<FireworkExplosion>
    {
        public static final Codec<FireworkPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)FireworkExplosion.Shape.CODEC.optionalFieldOf("shape").forGetter(FireworkPredicate::shape), (App)Codec.BOOL.optionalFieldOf("has_twinkle").forGetter(FireworkPredicate::twinkle), (App)Codec.BOOL.optionalFieldOf("has_trail").forGetter(FireworkPredicate::trail)).apply((Applicative)instance, FireworkPredicate::new));

        @Override
        public boolean test(FireworkExplosion fireworkExplosion) {
            if (this.shape.isPresent() && this.shape.get() != fireworkExplosion.shape()) {
                return false;
            }
            if (this.twinkle.isPresent() && this.twinkle.get().booleanValue() != fireworkExplosion.hasTwinkle()) {
                return false;
            }
            return !this.trail.isPresent() || this.trail.get().booleanValue() == fireworkExplosion.hasTrail();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((FireworkExplosion)object);
        }
    }
}

