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
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record Tool(List<Rule> rules, float defaultMiningSpeed, int damagePerBlock) {
    public static final Codec<Tool> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Rule.CODEC.listOf().fieldOf("rules").forGetter(Tool::rules), (App)Codec.FLOAT.optionalFieldOf("default_mining_speed", (Object)Float.valueOf(1.0f)).forGetter(Tool::defaultMiningSpeed), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("damage_per_block", (Object)1).forGetter(Tool::damagePerBlock)).apply((Applicative)instance, Tool::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Tool> STREAM_CODEC = StreamCodec.composite(Rule.STREAM_CODEC.apply(ByteBufCodecs.list()), Tool::rules, ByteBufCodecs.FLOAT, Tool::defaultMiningSpeed, ByteBufCodecs.VAR_INT, Tool::damagePerBlock, Tool::new);

    public float getMiningSpeed(BlockState blockState) {
        for (Rule rule : this.rules) {
            if (!rule.speed.isPresent() || !blockState.is(rule.blocks)) continue;
            return rule.speed.get().floatValue();
        }
        return this.defaultMiningSpeed;
    }

    public boolean isCorrectForDrops(BlockState blockState) {
        for (Rule rule : this.rules) {
            if (!rule.correctForDrops.isPresent() || !blockState.is(rule.blocks)) continue;
            return rule.correctForDrops.get();
        }
        return false;
    }

    public static final class Rule
    extends Record {
        final HolderSet<Block> blocks;
        final Optional<Float> speed;
        final Optional<Boolean> correctForDrops;
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Rule::blocks), (App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("speed").forGetter(Rule::speed), (App)Codec.BOOL.optionalFieldOf("correct_for_drops").forGetter(Rule::correctForDrops)).apply((Applicative)instance, Rule::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Rule> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.BLOCK), Rule::blocks, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), Rule::speed, ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional), Rule::correctForDrops, Rule::new);

        public Rule(HolderSet<Block> holderSet, Optional<Float> optional, Optional<Boolean> optional2) {
            this.blocks = holderSet;
            this.speed = optional;
            this.correctForDrops = optional2;
        }

        public static Rule minesAndDrops(List<Block> list, float f) {
            return Rule.forBlocks(list, Optional.of(Float.valueOf(f)), Optional.of(true));
        }

        public static Rule minesAndDrops(TagKey<Block> tagKey, float f) {
            return Rule.forTag(tagKey, Optional.of(Float.valueOf(f)), Optional.of(true));
        }

        public static Rule deniesDrops(TagKey<Block> tagKey) {
            return Rule.forTag(tagKey, Optional.empty(), Optional.of(false));
        }

        public static Rule overrideSpeed(TagKey<Block> tagKey, float f) {
            return Rule.forTag(tagKey, Optional.of(Float.valueOf(f)), Optional.empty());
        }

        public static Rule overrideSpeed(List<Block> list, float f) {
            return Rule.forBlocks(list, Optional.of(Float.valueOf(f)), Optional.empty());
        }

        private static Rule forTag(TagKey<Block> tagKey, Optional<Float> optional, Optional<Boolean> optional2) {
            return new Rule(BuiltInRegistries.BLOCK.getOrCreateTag(tagKey), optional, optional2);
        }

        private static Rule forBlocks(List<Block> list, Optional<Float> optional, Optional<Boolean> optional2) {
            return new Rule(HolderSet.direct(list.stream().map(Block::builtInRegistryHolder).collect(Collectors.toList())), optional, optional2);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this, object);
        }

        public HolderSet<Block> blocks() {
            return this.blocks;
        }

        public Optional<Float> speed() {
            return this.speed;
        }

        public Optional<Boolean> correctForDrops() {
            return this.correctForDrops;
        }
    }
}

