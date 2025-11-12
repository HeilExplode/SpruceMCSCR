/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;

public class GeodeCrackSettings {
    public static final Codec<GeodeCrackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GeodeConfiguration.CHANCE_RANGE.fieldOf("generate_crack_chance").orElse((Object)1.0).forGetter(geodeCrackSettings -> geodeCrackSettings.generateCrackChance), (App)Codec.doubleRange((double)0.0, (double)5.0).fieldOf("base_crack_size").orElse((Object)2.0).forGetter(geodeCrackSettings -> geodeCrackSettings.baseCrackSize), (App)Codec.intRange((int)0, (int)10).fieldOf("crack_point_offset").orElse((Object)2).forGetter(geodeCrackSettings -> geodeCrackSettings.crackPointOffset)).apply((Applicative)instance, GeodeCrackSettings::new));
    public final double generateCrackChance;
    public final double baseCrackSize;
    public final int crackPointOffset;

    public GeodeCrackSettings(double d, double d2, int n) {
        this.generateCrackChance = d;
        this.baseCrackSize = d2;
        this.crackPointOffset = n;
    }
}

