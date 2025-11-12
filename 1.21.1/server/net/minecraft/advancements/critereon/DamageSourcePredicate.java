/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public record DamageSourcePredicate(List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity, Optional<Boolean> isDirect) {
    public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(DamageSourcePredicate::tags), (App)EntityPredicate.CODEC.optionalFieldOf("direct_entity").forGetter(DamageSourcePredicate::directEntity), (App)EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamageSourcePredicate::sourceEntity), (App)Codec.BOOL.optionalFieldOf("is_direct").forGetter(DamageSourcePredicate::isDirect)).apply((Applicative)instance, DamageSourcePredicate::new));

    public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource) {
        return this.matches(serverPlayer.serverLevel(), serverPlayer.position(), damageSource);
    }

    public boolean matches(ServerLevel serverLevel, Vec3 vec3, DamageSource damageSource) {
        for (TagPredicate<DamageType> tagPredicate : this.tags) {
            if (tagPredicate.matches(damageSource.typeHolder())) continue;
            return false;
        }
        if (this.directEntity.isPresent() && !this.directEntity.get().matches(serverLevel, vec3, damageSource.getDirectEntity())) {
            return false;
        }
        if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches(serverLevel, vec3, damageSource.getEntity())) {
            return false;
        }
        return !this.isDirect.isPresent() || this.isDirect.get().booleanValue() == damageSource.isDirect();
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private Optional<EntityPredicate> directEntity = Optional.empty();
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> isDirect = Optional.empty();

        public static Builder damageType() {
            return new Builder();
        }

        public Builder tag(TagPredicate<DamageType> tagPredicate) {
            this.tags.add(tagPredicate);
            return this;
        }

        public Builder direct(EntityPredicate.Builder builder) {
            this.directEntity = Optional.of(builder.build());
            return this;
        }

        public Builder source(EntityPredicate.Builder builder) {
            this.sourceEntity = Optional.of(builder.build());
            return this;
        }

        public Builder isDirect(boolean bl) {
            this.isDirect = Optional.of(bl);
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate((List<TagPredicate<DamageType>>)this.tags.build(), this.directEntity, this.sourceEntity, this.isDirect);
        }
    }
}

