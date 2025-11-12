/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences
extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldSeed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public static SavedData.Factory<RandomSequences> factory(long l) {
        return new SavedData.Factory<RandomSequences>(() -> new RandomSequences(l), (compoundTag, provider) -> RandomSequences.load(l, compoundTag), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public RandomSequences(long l) {
        this.worldSeed = l;
    }

    public RandomSource get(ResourceLocation resourceLocation) {
        RandomSource randomSource = this.sequences.computeIfAbsent(resourceLocation, this::createSequence).random();
        return new DirtyMarkingRandomSource(randomSource);
    }

    private RandomSequence createSequence(ResourceLocation resourceLocation) {
        return this.createSequence(resourceLocation, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(ResourceLocation resourceLocation, int n, boolean bl, boolean bl2) {
        long l = (bl ? this.worldSeed : 0L) ^ (long)n;
        return new RandomSequence(l, bl2 ? Optional.of(resourceLocation) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<ResourceLocation, RandomSequence> biConsumer) {
        this.sequences.forEach(biConsumer);
    }

    public void setSeedDefaults(int n, boolean bl, boolean bl2) {
        this.salt = n;
        this.includeWorldSeed = bl;
        this.includeSequenceId = bl2;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt("salt", this.salt);
        compoundTag.putBoolean("include_world_seed", this.includeWorldSeed);
        compoundTag.putBoolean("include_sequence_id", this.includeSequenceId);
        CompoundTag compoundTag2 = new CompoundTag();
        this.sequences.forEach((resourceLocation, randomSequence) -> compoundTag2.put(resourceLocation.toString(), (Tag)RandomSequence.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, randomSequence).result().orElseThrow()));
        compoundTag.put("sequences", compoundTag2);
        return compoundTag;
    }

    private static boolean getBooleanWithDefault(CompoundTag compoundTag, String string, boolean bl) {
        if (compoundTag.contains(string, 1)) {
            return compoundTag.getBoolean(string);
        }
        return bl;
    }

    public static RandomSequences load(long l, CompoundTag compoundTag) {
        RandomSequences randomSequences = new RandomSequences(l);
        randomSequences.setSeedDefaults(compoundTag.getInt("salt"), RandomSequences.getBooleanWithDefault(compoundTag, "include_world_seed", true), RandomSequences.getBooleanWithDefault(compoundTag, "include_sequence_id", true));
        CompoundTag compoundTag2 = compoundTag.getCompound("sequences");
        Set<String> set = compoundTag2.getAllKeys();
        for (String string : set) {
            try {
                RandomSequence randomSequence = (RandomSequence)((Pair)RandomSequence.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2.get(string)).result().get()).getFirst();
                randomSequences.sequences.put(ResourceLocation.parse(string), randomSequence);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to load random sequence {}", (Object)string, (Object)exception);
            }
        }
        return randomSequences;
    }

    public int clear() {
        int n = this.sequences.size();
        this.sequences.clear();
        return n;
    }

    public void reset(ResourceLocation resourceLocation) {
        this.sequences.put(resourceLocation, this.createSequence(resourceLocation));
    }

    public void reset(ResourceLocation resourceLocation, int n, boolean bl, boolean bl2) {
        this.sequences.put(resourceLocation, this.createSequence(resourceLocation, n, bl, bl2));
    }

    class DirtyMarkingRandomSource
    implements RandomSource {
        private final RandomSource random;

        DirtyMarkingRandomSource(RandomSource randomSource) {
            this.random = randomSource;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long l) {
            RandomSequences.this.setDirty();
            this.random.setSeed(l);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int n) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(n);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof DirtyMarkingRandomSource) {
                DirtyMarkingRandomSource dirtyMarkingRandomSource = (DirtyMarkingRandomSource)object;
                return this.random.equals(dirtyMarkingRandomSource.random);
            }
            return false;
        }
    }
}

