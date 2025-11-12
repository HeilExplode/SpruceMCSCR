/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper
extends ChangeOverTimeBlock<WeatherState> {
    public static final Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.EXPOSED_COPPER).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WEATHERED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.EXPOSED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WEATHERED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.OXIDIZED_CUT_COPPER).put((Object)Blocks.CHISELED_COPPER, (Object)Blocks.EXPOSED_CHISELED_COPPER).put((Object)Blocks.EXPOSED_CHISELED_COPPER, (Object)Blocks.WEATHERED_CHISELED_COPPER).put((Object)Blocks.WEATHERED_CHISELED_COPPER, (Object)Blocks.OXIDIZED_CHISELED_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS).put((Object)Blocks.COPPER_DOOR, (Object)Blocks.EXPOSED_COPPER_DOOR).put((Object)Blocks.EXPOSED_COPPER_DOOR, (Object)Blocks.WEATHERED_COPPER_DOOR).put((Object)Blocks.WEATHERED_COPPER_DOOR, (Object)Blocks.OXIDIZED_COPPER_DOOR).put((Object)Blocks.COPPER_TRAPDOOR, (Object)Blocks.EXPOSED_COPPER_TRAPDOOR).put((Object)Blocks.EXPOSED_COPPER_TRAPDOOR, (Object)Blocks.WEATHERED_COPPER_TRAPDOOR).put((Object)Blocks.WEATHERED_COPPER_TRAPDOOR, (Object)Blocks.OXIDIZED_COPPER_TRAPDOOR).put((Object)Blocks.COPPER_GRATE, (Object)Blocks.EXPOSED_COPPER_GRATE).put((Object)Blocks.EXPOSED_COPPER_GRATE, (Object)Blocks.WEATHERED_COPPER_GRATE).put((Object)Blocks.WEATHERED_COPPER_GRATE, (Object)Blocks.OXIDIZED_COPPER_GRATE).put((Object)Blocks.COPPER_BULB, (Object)Blocks.EXPOSED_COPPER_BULB).put((Object)Blocks.EXPOSED_COPPER_BULB, (Object)Blocks.WEATHERED_COPPER_BULB).put((Object)Blocks.WEATHERED_COPPER_BULB, (Object)Blocks.OXIDIZED_COPPER_BULB).build());
    public static final Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

    public static Optional<Block> getPrevious(Block block) {
        return Optional.ofNullable((Block)PREVIOUS_BY_BLOCK.get().get((Object)block));
    }

    public static Block getFirst(Block block) {
        Block block2 = block;
        Block block3 = (Block)PREVIOUS_BY_BLOCK.get().get((Object)block2);
        while (block3 != null) {
            block2 = block3;
            block3 = (Block)PREVIOUS_BY_BLOCK.get().get((Object)block2);
        }
        return block2;
    }

    public static Optional<BlockState> getPrevious(BlockState blockState) {
        return WeatheringCopper.getPrevious(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
    }

    public static Optional<Block> getNext(Block block) {
        return Optional.ofNullable((Block)NEXT_BY_BLOCK.get().get((Object)block));
    }

    public static BlockState getFirst(BlockState blockState) {
        return WeatheringCopper.getFirst(blockState.getBlock()).withPropertiesOf(blockState);
    }

    @Override
    default public Optional<BlockState> getNext(BlockState blockState) {
        return WeatheringCopper.getNext(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
    }

    @Override
    default public float getChanceModifier() {
        if (this.getAge() == WeatherState.UNAFFECTED) {
            return 0.75f;
        }
        return 1.0f;
    }

    public static enum WeatherState implements StringRepresentable
    {
        UNAFFECTED("unaffected"),
        EXPOSED("exposed"),
        WEATHERED("weathered"),
        OXIDIZED("oxidized");

        public static final Codec<WeatherState> CODEC;
        private final String name;

        private WeatherState(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(WeatherState::values);
        }
    }
}

