/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E>
implements Codec<HolderSet<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Holder<E>> elementCodec;
    private final Codec<List<Holder<E>>> homogenousListCodec;
    private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> codec, boolean bl) {
        Codec codec2 = codec.listOf().validate(ExtraCodecs.ensureHomogenous(Holder::kind));
        if (bl) {
            return codec2;
        }
        return Codec.either((Codec)codec2, codec).xmap(either -> (List)either.map(list -> list, List::of), list -> list.size() == 1 ? Either.right((Object)((Holder)list.get(0))) : Either.left((Object)list));
    }

    public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        return new HolderSetCodec<E>(resourceKey, codec, bl);
    }

    private HolderSetCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
        this.homogenousListCodec = HolderSetCodec.homogenousList(codec, bl);
        this.registryAwareCodec = Codec.either(TagKey.hashedCodec(resourceKey), this.homogenousListCodec);
    }

    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).getter(this.registryKey)).isPresent()) {
            HolderGetter holderGetter = optional.get();
            return this.registryAwareCodec.decode(dynamicOps, t).flatMap(pair -> {
                DataResult dataResult = (DataResult)((Either)pair.getFirst()).map(tagKey -> HolderSetCodec.lookupTag(holderGetter, tagKey), list -> DataResult.success(HolderSet.direct(list)));
                return dataResult.map(holderSet -> Pair.of((Object)holderSet, (Object)pair.getSecond()));
            });
        }
        return this.decodeWithoutRegistry(dynamicOps, t);
    }

    private static <E> DataResult<HolderSet<E>> lookupTag(HolderGetter<E> holderGetter, TagKey<E> tagKey) {
        return holderGetter.get(tagKey).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Missing tag: '" + String.valueOf(tagKey.location()) + "' in '" + String.valueOf(tagKey.registry().location()) + "'"));
    }

    public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).owner(this.registryKey)).isPresent()) {
            if (!holderSet.canSerializeIn(optional.get())) {
                return DataResult.error(() -> "HolderSet " + String.valueOf(holderSet) + " is not valid in current registry set");
            }
            return this.registryAwareCodec.encode((Object)holderSet.unwrap().mapRight(List::copyOf), dynamicOps, t);
        }
        return this.encodeWithoutRegistry(holderSet, dynamicOps, t);
    }

    private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> dynamicOps, T t) {
        return this.elementCodec.listOf().decode(dynamicOps, t).flatMap(pair -> {
            ArrayList<Holder.Direct> arrayList = new ArrayList<Holder.Direct>();
            for (Holder holder : (List)pair.getFirst()) {
                if (holder instanceof Holder.Direct) {
                    Holder.Direct direct = (Holder.Direct)holder;
                    arrayList.add(direct);
                    continue;
                }
                return DataResult.error(() -> "Can't decode element " + String.valueOf(holder) + " without registry");
            }
            return DataResult.success((Object)new Pair(HolderSet.direct(arrayList), pair.getSecond()));
        });
    }

    private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T t) {
        return this.homogenousListCodec.encode(holderSet.stream().toList(), dynamicOps, t);
    }

    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((HolderSet)object, dynamicOps, object2);
    }
}

