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
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public record FluidPredicate(Optional<HolderSet<Fluid>> fluids, Optional<StatePropertiesPredicate> properties) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids), (App)StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::properties)).apply((Applicative)instance, FluidPredicate::new));

    public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
        if (!serverLevel.isLoaded(blockPos)) {
            return false;
        }
        FluidState fluidState = serverLevel.getFluidState(blockPos);
        if (this.fluids.isPresent() && !fluidState.is(this.fluids.get())) {
            return false;
        }
        return !this.properties.isPresent() || this.properties.get().matches(fluidState);
    }

    public static class Builder {
        private Optional<HolderSet<Fluid>> fluids = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        private Builder() {
        }

        public static Builder fluid() {
            return new Builder();
        }

        public Builder of(Fluid fluid) {
            this.fluids = Optional.of(HolderSet.direct(fluid.builtInRegistryHolder()));
            return this;
        }

        public Builder of(HolderSet<Fluid> holderSet) {
            this.fluids = Optional.of(holderSet);
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate statePropertiesPredicate) {
            this.properties = Optional.of(statePropertiesPredicate);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.properties);
        }
    }
}

