/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 */
package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

public class BiomeManager {
    public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;
    private final NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;

    public BiomeManager(NoiseBiomeSource noiseBiomeSource, long l) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = l;
    }

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public BiomeManager withDifferentSource(NoiseBiomeSource noiseBiomeSource) {
        return new BiomeManager(noiseBiomeSource, this.biomeZoomSeed);
    }

    public Holder<Biome> getBiome(BlockPos blockPos) {
        int n;
        int n2;
        int n3;
        int n4 = blockPos.getX() - 2;
        int n5 = blockPos.getY() - 2;
        int n6 = blockPos.getZ() - 2;
        int n7 = n4 >> 2;
        int n8 = n5 >> 2;
        int n9 = n6 >> 2;
        double d = (double)(n4 & 3) / 4.0;
        double d2 = (double)(n5 & 3) / 4.0;
        double d3 = (double)(n6 & 3) / 4.0;
        int n10 = 0;
        double d4 = Double.POSITIVE_INFINITY;
        for (n3 = 0; n3 < 8; ++n3) {
            double d5;
            double d6;
            double d7;
            boolean bl;
            int n11;
            int n12;
            n2 = (n3 & 4) == 0 ? 1 : 0;
            int n13 = n2 != 0 ? n7 : n7 + 1;
            double d8 = BiomeManager.getFiddledDistance(this.biomeZoomSeed, n13, n12 = (n = (n3 & 2) == 0 ? 1 : 0) != 0 ? n8 : n8 + 1, n11 = (bl = (n3 & 1) == 0) ? n9 : n9 + 1, d7 = n2 != 0 ? d : d - 1.0, d6 = n != 0 ? d2 : d2 - 1.0, d5 = bl ? d3 : d3 - 1.0);
            if (!(d4 > d8)) continue;
            n10 = n3;
            d4 = d8;
        }
        n3 = (n10 & 4) == 0 ? n7 : n7 + 1;
        n2 = (n10 & 2) == 0 ? n8 : n8 + 1;
        n = (n10 & 1) == 0 ? n9 : n9 + 1;
        return this.noiseBiomeSource.getNoiseBiome(n3, n2, n);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(double d, double d2, double d3) {
        int n = QuartPos.fromBlock(Mth.floor(d));
        int n2 = QuartPos.fromBlock(Mth.floor(d2));
        int n3 = QuartPos.fromBlock(Mth.floor(d3));
        return this.getNoiseBiomeAtQuart(n, n2, n3);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos blockPos) {
        int n = QuartPos.fromBlock(blockPos.getX());
        int n2 = QuartPos.fromBlock(blockPos.getY());
        int n3 = QuartPos.fromBlock(blockPos.getZ());
        return this.getNoiseBiomeAtQuart(n, n2, n3);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(int n, int n2, int n3) {
        return this.noiseBiomeSource.getNoiseBiome(n, n2, n3);
    }

    private static double getFiddledDistance(long l, int n, int n2, int n3, double d, double d2, double d3) {
        long l2 = l;
        l2 = LinearCongruentialGenerator.next(l2, n);
        l2 = LinearCongruentialGenerator.next(l2, n2);
        l2 = LinearCongruentialGenerator.next(l2, n3);
        l2 = LinearCongruentialGenerator.next(l2, n);
        l2 = LinearCongruentialGenerator.next(l2, n2);
        l2 = LinearCongruentialGenerator.next(l2, n3);
        double d4 = BiomeManager.getFiddle(l2);
        l2 = LinearCongruentialGenerator.next(l2, l);
        double d5 = BiomeManager.getFiddle(l2);
        l2 = LinearCongruentialGenerator.next(l2, l);
        double d6 = BiomeManager.getFiddle(l2);
        return Mth.square(d3 + d6) + Mth.square(d2 + d5) + Mth.square(d + d4);
    }

    private static double getFiddle(long l) {
        double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    public static interface NoiseBiomeSource {
        public Holder<Biome> getNoiseBiome(int var1, int var2, int var3);
    }
}

