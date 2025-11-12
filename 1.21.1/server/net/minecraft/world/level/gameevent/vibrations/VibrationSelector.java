/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
    public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getLeft)), (App)Codec.LONG.fieldOf("tick").forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getRight).orElse(-1L))).apply((Applicative)instance, VibrationSelector::new));
    private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

    public VibrationSelector(Optional<VibrationInfo> optional, long l) {
        this.currentVibrationData = optional.map(vibrationInfo -> Pair.of((Object)vibrationInfo, (Object)l));
    }

    public VibrationSelector() {
        this.currentVibrationData = Optional.empty();
    }

    public void addCandidate(VibrationInfo vibrationInfo, long l) {
        if (this.shouldReplaceVibration(vibrationInfo, l)) {
            this.currentVibrationData = Optional.of(Pair.of((Object)vibrationInfo, (Object)l));
        }
    }

    private boolean shouldReplaceVibration(VibrationInfo vibrationInfo, long l) {
        if (this.currentVibrationData.isEmpty()) {
            return true;
        }
        Pair<VibrationInfo, Long> pair = this.currentVibrationData.get();
        long l2 = (Long)pair.getRight();
        if (l != l2) {
            return false;
        }
        VibrationInfo vibrationInfo2 = (VibrationInfo)pair.getLeft();
        if (vibrationInfo.distance() < vibrationInfo2.distance()) {
            return true;
        }
        if (vibrationInfo.distance() > vibrationInfo2.distance()) {
            return false;
        }
        return VibrationSystem.getGameEventFrequency(vibrationInfo.gameEvent()) > VibrationSystem.getGameEventFrequency(vibrationInfo2.gameEvent());
    }

    public Optional<VibrationInfo> chosenCandidate(long l) {
        if (this.currentVibrationData.isEmpty()) {
            return Optional.empty();
        }
        if ((Long)this.currentVibrationData.get().getRight() < l) {
            return Optional.of((VibrationInfo)this.currentVibrationData.get().getLeft());
        }
        return Optional.empty();
    }

    public void startOver() {
        this.currentVibrationData = Optional.empty();
    }
}

