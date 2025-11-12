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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(Optional<PositionPredicate> position, Optional<HolderSet<Biome>> biomes, Optional<HolderSet<Structure>> structures, Optional<ResourceKey<Level>> dimension, Optional<Boolean> smokey, Optional<LightPredicate> light, Optional<BlockPredicate> block, Optional<FluidPredicate> fluid, Optional<Boolean> canSeeSky) {
    public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position), (App)RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes), (App)RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures), (App)ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension), (App)Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey), (App)LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light), (App)BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block), (App)FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid), (App)Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)).apply((Applicative)instance, LocationPredicate::new));

    public boolean matches(ServerLevel serverLevel, double d, double d2, double d3) {
        if (this.position.isPresent() && !this.position.get().matches(d, d2, d3)) {
            return false;
        }
        if (this.dimension.isPresent() && this.dimension.get() != serverLevel.dimension()) {
            return false;
        }
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        boolean bl = serverLevel.isLoaded(blockPos);
        if (!(!this.biomes.isPresent() || bl && this.biomes.get().contains(serverLevel.getBiome(blockPos)))) {
            return false;
        }
        if (!(!this.structures.isPresent() || bl && serverLevel.structureManager().getStructureWithPieceAt(blockPos, this.structures.get()).isValid())) {
            return false;
        }
        if (this.smokey.isPresent() && (!bl || this.smokey.get() != CampfireBlock.isSmokeyPos(serverLevel, blockPos))) {
            return false;
        }
        if (this.light.isPresent() && !this.light.get().matches(serverLevel, blockPos)) {
            return false;
        }
        if (this.block.isPresent() && !this.block.get().matches(serverLevel, blockPos)) {
            return false;
        }
        if (this.fluid.isPresent() && !this.fluid.get().matches(serverLevel, blockPos)) {
            return false;
        }
        return !this.canSeeSky.isPresent() || this.canSeeSky.get().booleanValue() == serverLevel.canSeeSky(blockPos);
    }

    record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
        public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::z)).apply((Applicative)instance, PositionPredicate::new));

        static Optional<PositionPredicate> of(MinMaxBounds.Doubles doubles, MinMaxBounds.Doubles doubles2, MinMaxBounds.Doubles doubles3) {
            if (doubles.isAny() && doubles2.isAny() && doubles3.isAny()) {
                return Optional.empty();
            }
            return Optional.of(new PositionPredicate(doubles, doubles2, doubles3));
        }

        public boolean matches(double d, double d2, double d3) {
            return this.x.matches(d) && this.y.matches(d2) && this.z.matches(d3);
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        private Optional<HolderSet<Biome>> biomes = Optional.empty();
        private Optional<HolderSet<Structure>> structures = Optional.empty();
        private Optional<ResourceKey<Level>> dimension = Optional.empty();
        private Optional<Boolean> smokey = Optional.empty();
        private Optional<LightPredicate> light = Optional.empty();
        private Optional<BlockPredicate> block = Optional.empty();
        private Optional<FluidPredicate> fluid = Optional.empty();
        private Optional<Boolean> canSeeSky = Optional.empty();

        public static Builder location() {
            return new Builder();
        }

        public static Builder inBiome(Holder<Biome> holder) {
            return Builder.location().setBiomes(HolderSet.direct(holder));
        }

        public static Builder inDimension(ResourceKey<Level> resourceKey) {
            return Builder.location().setDimension(resourceKey);
        }

        public static Builder inStructure(Holder<Structure> holder) {
            return Builder.location().setStructures(HolderSet.direct(holder));
        }

        public static Builder atYLocation(MinMaxBounds.Doubles doubles) {
            return Builder.location().setY(doubles);
        }

        public Builder setX(MinMaxBounds.Doubles doubles) {
            this.x = doubles;
            return this;
        }

        public Builder setY(MinMaxBounds.Doubles doubles) {
            this.y = doubles;
            return this;
        }

        public Builder setZ(MinMaxBounds.Doubles doubles) {
            this.z = doubles;
            return this;
        }

        public Builder setBiomes(HolderSet<Biome> holderSet) {
            this.biomes = Optional.of(holderSet);
            return this;
        }

        public Builder setStructures(HolderSet<Structure> holderSet) {
            this.structures = Optional.of(holderSet);
            return this;
        }

        public Builder setDimension(ResourceKey<Level> resourceKey) {
            this.dimension = Optional.of(resourceKey);
            return this;
        }

        public Builder setLight(LightPredicate.Builder builder) {
            this.light = Optional.of(builder.build());
            return this;
        }

        public Builder setBlock(BlockPredicate.Builder builder) {
            this.block = Optional.of(builder.build());
            return this;
        }

        public Builder setFluid(FluidPredicate.Builder builder) {
            this.fluid = Optional.of(builder.build());
            return this;
        }

        public Builder setSmokey(boolean bl) {
            this.smokey = Optional.of(bl);
            return this;
        }

        public Builder setCanSeeSky(boolean bl) {
            this.canSeeSky = Optional.of(bl);
            return this;
        }

        public LocationPredicate build() {
            Optional<PositionPredicate> optional = PositionPredicate.of(this.x, this.y, this.z);
            return new LocationPredicate(optional, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
        }
    }
}

