/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;

public class WorldOptions {
    public static final MapCodec<WorldOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.LONG.fieldOf("seed").stable().forGetter(WorldOptions::seed), (App)Codec.BOOL.fieldOf("generate_features").orElse((Object)true).stable().forGetter(WorldOptions::generateStructures), (App)Codec.BOOL.fieldOf("bonus_chest").orElse((Object)false).stable().forGetter(WorldOptions::generateBonusChest), (App)Codec.STRING.lenientOptionalFieldOf("legacy_custom_options").stable().forGetter(worldOptions -> worldOptions.legacyCustomOptions)).apply((Applicative)instance, instance.stable(WorldOptions::new)));
    public static final WorldOptions DEMO_OPTIONS = new WorldOptions("North Carolina".hashCode(), true, true);
    private final long seed;
    private final boolean generateStructures;
    private final boolean generateBonusChest;
    private final Optional<String> legacyCustomOptions;

    public WorldOptions(long l, boolean bl, boolean bl2) {
        this(l, bl, bl2, Optional.empty());
    }

    public static WorldOptions defaultWithRandomSeed() {
        return new WorldOptions(WorldOptions.randomSeed(), true, false);
    }

    private WorldOptions(long l, boolean bl, boolean bl2, Optional<String> optional) {
        this.seed = l;
        this.generateStructures = bl;
        this.generateBonusChest = bl2;
        this.legacyCustomOptions = optional;
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateStructures() {
        return this.generateStructures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldOptions withBonusChest(boolean bl) {
        return new WorldOptions(this.seed, this.generateStructures, bl, this.legacyCustomOptions);
    }

    public WorldOptions withStructures(boolean bl) {
        return new WorldOptions(this.seed, bl, this.generateBonusChest, this.legacyCustomOptions);
    }

    public WorldOptions withSeed(OptionalLong optionalLong) {
        return new WorldOptions(optionalLong.orElse(WorldOptions.randomSeed()), this.generateStructures, this.generateBonusChest, this.legacyCustomOptions);
    }

    public static OptionalLong parseSeed(String string) {
        if (StringUtils.isEmpty((CharSequence)(string = string.trim()))) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(string));
        }
        catch (NumberFormatException numberFormatException) {
            return OptionalLong.of(string.hashCode());
        }
    }

    public static long randomSeed() {
        return RandomSource.create().nextLong();
    }
}

