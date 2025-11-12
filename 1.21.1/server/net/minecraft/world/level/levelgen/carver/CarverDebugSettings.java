/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
    public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(false, Blocks.ACACIA_BUTTON.defaultBlockState(), Blocks.CANDLE.defaultBlockState(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), Blocks.GLASS.defaultBlockState());
    public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("debug_mode", (Object)false).forGetter(CarverDebugSettings::isDebugMode), (App)BlockState.CODEC.optionalFieldOf("air_state", (Object)DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState), (App)BlockState.CODEC.optionalFieldOf("water_state", (Object)DEFAULT.getAirState()).forGetter(CarverDebugSettings::getWaterState), (App)BlockState.CODEC.optionalFieldOf("lava_state", (Object)DEFAULT.getAirState()).forGetter(CarverDebugSettings::getLavaState), (App)BlockState.CODEC.optionalFieldOf("barrier_state", (Object)DEFAULT.getAirState()).forGetter(CarverDebugSettings::getBarrierState)).apply((Applicative)instance, CarverDebugSettings::new));
    private final boolean debugMode;
    private final BlockState airState;
    private final BlockState waterState;
    private final BlockState lavaState;
    private final BlockState barrierState;

    public static CarverDebugSettings of(boolean bl, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
        return new CarverDebugSettings(bl, blockState, blockState2, blockState3, blockState4);
    }

    public static CarverDebugSettings of(BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
        return new CarverDebugSettings(false, blockState, blockState2, blockState3, blockState4);
    }

    public static CarverDebugSettings of(boolean bl, BlockState blockState) {
        return new CarverDebugSettings(bl, blockState, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
    }

    private CarverDebugSettings(boolean bl, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
        this.debugMode = bl;
        this.airState = blockState;
        this.waterState = blockState2;
        this.lavaState = blockState3;
        this.barrierState = blockState4;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public BlockState getAirState() {
        return this.airState;
    }

    public BlockState getWaterState() {
        return this.waterState;
    }

    public BlockState getLavaState() {
        return this.lavaState;
    }

    public BlockState getBarrierState() {
        return this.barrierState;
    }
}

