/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.SingleValuePalette;

public class PalettedContainer<T>
implements PaletteResize<T>,
PalettedContainerRO<T> {
    private static final int MIN_PALETTE_BITS = 0;
    private final PaletteResize<T> dummyPaletteResize = (n, object) -> 0;
    private final IdMap<T> registry;
    private volatile Data<T> data;
    private final Strategy strategy;
    private final ThreadingDetector threadingDetector = new ThreadingDetector("PalettedContainer");

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public void release() {
        this.threadingDetector.checkAndUnlock();
    }

    public static <T> Codec<PalettedContainer<T>> codecRW(IdMap<T> idMap, Codec<T> codec, Strategy strategy, T t) {
        PalettedContainerRO.Unpacker unpacker = PalettedContainer::unpack;
        return PalettedContainer.codec(idMap, codec, strategy, t, unpacker);
    }

    public static <T> Codec<PalettedContainerRO<T>> codecRO(IdMap<T> idMap2, Codec<T> codec, Strategy strategy2, T t) {
        PalettedContainerRO.Unpacker unpacker = (idMap, strategy, packedData) -> PalettedContainer.unpack(idMap, strategy, packedData).map(palettedContainer -> palettedContainer);
        return PalettedContainer.codec(idMap2, codec, strategy2, t, unpacker);
    }

    private static <T, C extends PalettedContainerRO<T>> Codec<C> codec(IdMap<T> idMap, Codec<T> codec, Strategy strategy, T t, PalettedContainerRO.Unpacker<T, C> unpacker) {
        return RecordCodecBuilder.create(instance -> instance.group((App)codec.mapResult(ExtraCodecs.orElsePartial(t)).listOf().fieldOf("palette").forGetter(PalettedContainerRO.PackedData::paletteEntries), (App)Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(PalettedContainerRO.PackedData::storage)).apply((Applicative)instance, PalettedContainerRO.PackedData::new)).comapFlatMap(packedData -> unpacker.read(idMap, strategy, (PalettedContainerRO.PackedData)packedData), palettedContainerRO -> palettedContainerRO.pack(idMap, strategy));
    }

    public PalettedContainer(IdMap<T> idMap, Strategy strategy, Configuration<T> configuration, BitStorage bitStorage, List<T> list) {
        this.registry = idMap;
        this.strategy = strategy;
        this.data = new Data<T>(configuration, bitStorage, configuration.factory().create(configuration.bits(), idMap, this, list));
    }

    private PalettedContainer(IdMap<T> idMap, Strategy strategy, Data<T> data) {
        this.registry = idMap;
        this.strategy = strategy;
        this.data = data;
    }

    public PalettedContainer(IdMap<T> idMap, T t, Strategy strategy) {
        this.strategy = strategy;
        this.registry = idMap;
        this.data = this.createOrReuseData(null, 0);
        this.data.palette.idFor(t);
    }

    private Data<T> createOrReuseData(@Nullable Data<T> data, int n) {
        Configuration<T> configuration = this.strategy.getConfiguration(this.registry, n);
        if (data != null && configuration.equals(data.configuration())) {
            return data;
        }
        return configuration.createData(this.registry, this, this.strategy.size());
    }

    @Override
    public int onResize(int n, T t) {
        Data<T> data = this.data;
        Data data2 = this.createOrReuseData(data, n);
        data2.copyFrom(data.palette, data.storage);
        this.data = data2;
        return data2.palette.idFor(t);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T getAndSet(int n, int n2, int n3, T t) {
        this.acquire();
        try {
            T t2 = this.getAndSet(this.strategy.getIndex(n, n2, n3), t);
            return t2;
        }
        finally {
            this.release();
        }
    }

    public T getAndSetUnchecked(int n, int n2, int n3, T t) {
        return this.getAndSet(this.strategy.getIndex(n, n2, n3), t);
    }

    private T getAndSet(int n, T t) {
        int n2 = this.data.palette.idFor(t);
        int n3 = this.data.storage.getAndSet(n, n2);
        return this.data.palette.valueFor(n3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(int n, int n2, int n3, T t) {
        this.acquire();
        try {
            this.set(this.strategy.getIndex(n, n2, n3), t);
        }
        finally {
            this.release();
        }
    }

    private void set(int n, T t) {
        int n2 = this.data.palette.idFor(t);
        this.data.storage.set(n, n2);
    }

    @Override
    public T get(int n, int n2, int n3) {
        return this.get(this.strategy.getIndex(n, n2, n3));
    }

    protected T get(int n) {
        Data<T> data = this.data;
        return data.palette.valueFor(data.storage.get(n));
    }

    @Override
    public void getAll(Consumer<T> consumer) {
        Palette palette = this.data.palette();
        IntArraySet intArraySet = new IntArraySet();
        this.data.storage.getAll(arg_0 -> ((IntSet)intArraySet).add(arg_0));
        intArraySet.forEach(n -> consumer.accept(palette.valueFor(n)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.acquire();
        try {
            byte by = friendlyByteBuf.readByte();
            Data<T> data = this.createOrReuseData(this.data, by);
            data.palette.read(friendlyByteBuf);
            friendlyByteBuf.readLongArray(data.storage.getRaw());
            this.data = data;
        }
        finally {
            this.release();
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        this.acquire();
        try {
            this.data.write(friendlyByteBuf);
        }
        finally {
            this.release();
        }
    }

    private static <T> DataResult<PalettedContainer<T>> unpack(IdMap<T> idMap, Strategy strategy, PalettedContainerRO.PackedData<T> packedData) {
        BitStorage bitStorage;
        List<T> list = packedData.paletteEntries();
        int n2 = strategy.size();
        int n3 = strategy.calculateBitsForSerialization(idMap, list.size());
        Configuration<T> configuration = strategy.getConfiguration(idMap, n3);
        if (n3 == 0) {
            bitStorage = new ZeroBitStorage(n2);
        } else {
            Optional<LongStream> optional = packedData.storage();
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }
            long[] lArray = optional.get().toArray();
            try {
                if (configuration.factory() == Strategy.GLOBAL_PALETTE_FACTORY) {
                    HashMapPalette<Object> hashMapPalette = new HashMapPalette<Object>(idMap, n3, (n, object) -> 0, list);
                    SimpleBitStorage simpleBitStorage = new SimpleBitStorage(n3, n2, lArray);
                    int[] nArray = new int[n2];
                    simpleBitStorage.unpack(nArray);
                    PalettedContainer.swapPalette(nArray, n -> idMap.getId(hashMapPalette.valueFor(n)));
                    bitStorage = new SimpleBitStorage(configuration.bits(), n2, nArray);
                } else {
                    bitStorage = new SimpleBitStorage(configuration.bits(), n2, lArray);
                }
            }
            catch (SimpleBitStorage.InitializationException initializationException) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + initializationException.getMessage());
            }
        }
        return DataResult.success(new PalettedContainer<T>(idMap, strategy, configuration, bitStorage, list));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PalettedContainerRO.PackedData<T> pack(IdMap<T> idMap, Strategy strategy) {
        this.acquire();
        try {
            Optional<LongStream> optional;
            Object object;
            HashMapPalette<T> hashMapPalette = new HashMapPalette<T>(idMap, this.data.storage.getBits(), this.dummyPaletteResize);
            int n2 = strategy.size();
            int[] nArray = new int[n2];
            this.data.storage.unpack(nArray);
            PalettedContainer.swapPalette(nArray, n -> hashMapPalette.idFor(this.data.palette.valueFor(n)));
            int n3 = strategy.calculateBitsForSerialization(idMap, hashMapPalette.getSize());
            if (n3 != 0) {
                object = new SimpleBitStorage(n3, n2, nArray);
                optional = Optional.of(Arrays.stream(((SimpleBitStorage)object).getRaw()));
            } else {
                optional = Optional.empty();
            }
            object = new PalettedContainerRO.PackedData<T>(hashMapPalette.getEntries(), optional);
            return object;
        }
        finally {
            this.release();
        }
    }

    private static <T> void swapPalette(int[] nArray, IntUnaryOperator intUnaryOperator) {
        int n = -1;
        int n2 = -1;
        for (int i = 0; i < nArray.length; ++i) {
            int n3 = nArray[i];
            if (n3 != n) {
                n = n3;
                n2 = intUnaryOperator.applyAsInt(n3);
            }
            nArray[i] = n2;
        }
    }

    @Override
    public int getSerializedSize() {
        return this.data.getSerializedSize();
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        return this.data.palette.maybeHas(predicate);
    }

    public PalettedContainer<T> copy() {
        return new PalettedContainer<T>(this.registry, this.strategy, this.data.copy());
    }

    @Override
    public PalettedContainer<T> recreate() {
        return new PalettedContainer<T>(this.registry, this.data.palette.valueFor(0), this.strategy);
    }

    @Override
    public void count(CountConsumer<T> countConsumer) {
        if (this.data.palette.getSize() == 1) {
            countConsumer.accept(this.data.palette.valueFor(0), this.data.storage.getSize());
            return;
        }
        Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap();
        this.data.storage.getAll((int n) -> int2IntOpenHashMap.addTo(n, 1));
        int2IntOpenHashMap.int2IntEntrySet().forEach(entry -> countConsumer.accept(this.data.palette.valueFor(entry.getIntKey()), entry.getIntValue()));
    }

    public static abstract class Strategy {
        public static final Palette.Factory SINGLE_VALUE_PALETTE_FACTORY = SingleValuePalette::create;
        public static final Palette.Factory LINEAR_PALETTE_FACTORY = LinearPalette::create;
        public static final Palette.Factory HASHMAP_PALETTE_FACTORY = HashMapPalette::create;
        static final Palette.Factory GLOBAL_PALETTE_FACTORY = GlobalPalette::create;
        public static final Strategy SECTION_STATES = new Strategy(4){

            @Override
            public <A> Configuration<A> getConfiguration(IdMap<A> idMap, int n) {
                return switch (n) {
                    case 0 -> new Configuration(SINGLE_VALUE_PALETTE_FACTORY, n);
                    case 1, 2, 3, 4 -> new Configuration(LINEAR_PALETTE_FACTORY, 4);
                    case 5, 6, 7, 8 -> new Configuration(HASHMAP_PALETTE_FACTORY, n);
                    default -> new Configuration(GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idMap.size()));
                };
            }
        };
        public static final Strategy SECTION_BIOMES = new Strategy(2){

            @Override
            public <A> Configuration<A> getConfiguration(IdMap<A> idMap, int n) {
                return switch (n) {
                    case 0 -> new Configuration(SINGLE_VALUE_PALETTE_FACTORY, n);
                    case 1, 2, 3 -> new Configuration(LINEAR_PALETTE_FACTORY, n);
                    default -> new Configuration(GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idMap.size()));
                };
            }
        };
        private final int sizeBits;

        Strategy(int n) {
            this.sizeBits = n;
        }

        public int size() {
            return 1 << this.sizeBits * 3;
        }

        public int getIndex(int n, int n2, int n3) {
            return (n2 << this.sizeBits | n3) << this.sizeBits | n;
        }

        public abstract <A> Configuration<A> getConfiguration(IdMap<A> var1, int var2);

        <A> int calculateBitsForSerialization(IdMap<A> idMap, int n) {
            int n2 = Mth.ceillog2(n);
            Configuration<A> configuration = this.getConfiguration(idMap, n2);
            return configuration.factory() == GLOBAL_PALETTE_FACTORY ? n2 : configuration.bits();
        }
    }

    static final class Data<T>
    extends Record {
        private final Configuration<T> configuration;
        final BitStorage storage;
        final Palette<T> palette;

        Data(Configuration<T> configuration, BitStorage bitStorage, Palette<T> palette) {
            this.configuration = configuration;
            this.storage = bitStorage;
            this.palette = palette;
        }

        public void copyFrom(Palette<T> palette, BitStorage bitStorage) {
            for (int i = 0; i < bitStorage.getSize(); ++i) {
                T t = palette.valueFor(bitStorage.get(i));
                this.storage.set(i, this.palette.idFor(t));
            }
        }

        public int getSerializedSize() {
            return 1 + this.palette.getSerializedSize() + VarInt.getByteSize(this.storage.getRaw().length) + this.storage.getRaw().length * 8;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.storage.getBits());
            this.palette.write(friendlyByteBuf);
            friendlyByteBuf.writeLongArray(this.storage.getRaw());
        }

        public Data<T> copy() {
            return new Data<T>(this.configuration, this.storage.copy(), this.palette.copy());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this, object);
        }

        public Configuration<T> configuration() {
            return this.configuration;
        }

        public BitStorage storage() {
            return this.storage;
        }

        public Palette<T> palette() {
            return this.palette;
        }
    }

    record Configuration<T>(Palette.Factory factory, int bits) {
        public Data<T> createData(IdMap<T> idMap, PaletteResize<T> paletteResize, int n) {
            BitStorage bitStorage = this.bits == 0 ? new ZeroBitStorage(n) : new SimpleBitStorage(this.bits, n);
            Palette<T> palette = this.factory.create(this.bits, idMap, paletteResize, List.of());
            return new Data<T>(this, bitStorage, palette);
        }
    }

    @FunctionalInterface
    public static interface CountConsumer<T> {
        public void accept(T var1, int var2);
    }
}

