/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LightningBoltPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.RaiderPredicate;
import net.minecraft.advancements.critereon.SlimePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class EntitySubPredicates {
    public static final MapCodec<LightningBoltPredicate> LIGHTNING = EntitySubPredicates.register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<FishingHookPredicate> FISHING_HOOK = EntitySubPredicates.register("fishing_hook", FishingHookPredicate.CODEC);
    public static final MapCodec<PlayerPredicate> PLAYER = EntitySubPredicates.register("player", PlayerPredicate.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = EntitySubPredicates.register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = EntitySubPredicates.register("raider", RaiderPredicate.CODEC);
    public static final EntityVariantPredicateType<Axolotl.Variant> AXOLOTL = EntitySubPredicates.register("axolotl", EntityVariantPredicateType.create(Axolotl.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl)entity;
            optional = Optional.of(axolotl.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Boat.Type> BOAT = EntitySubPredicates.register("boat", EntityVariantPredicateType.create(Boat.Type.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Boat) {
            Boat boat = (Boat)entity;
            optional = Optional.of(boat.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Fox.Type> FOX = EntitySubPredicates.register("fox", EntityVariantPredicateType.create(Fox.Type.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            optional = Optional.of(fox.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<MushroomCow.MushroomType> MOOSHROOM = EntitySubPredicates.register("mooshroom", EntityVariantPredicateType.create(MushroomCow.MushroomType.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof MushroomCow) {
            MushroomCow mushroomCow = (MushroomCow)entity;
            optional = Optional.of(mushroomCow.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Rabbit.Variant> RABBIT = EntitySubPredicates.register("rabbit", EntityVariantPredicateType.create(Rabbit.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)entity;
            optional = Optional.of(rabbit.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Variant> HORSE = EntitySubPredicates.register("horse", EntityVariantPredicateType.create(Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Horse) {
            Horse horse = (Horse)entity;
            optional = Optional.of(horse.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Llama.Variant> LLAMA = EntitySubPredicates.register("llama", EntityVariantPredicateType.create(Llama.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Llama) {
            Llama llama = (Llama)entity;
            optional = Optional.of(llama.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<VillagerType> VILLAGER = EntitySubPredicates.register("villager", EntityVariantPredicateType.create(BuiltInRegistries.VILLAGER_TYPE.byNameCodec(), entity -> {
        Optional<Object> optional;
        if (entity instanceof VillagerDataHolder) {
            VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
            optional = Optional.of(villagerDataHolder.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<Parrot.Variant> PARROT = EntitySubPredicates.register("parrot", EntityVariantPredicateType.create(Parrot.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot)entity;
            optional = Optional.of(parrot.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityVariantPredicateType<TropicalFish.Pattern> TROPICAL_FISH = EntitySubPredicates.register("tropical_fish", EntityVariantPredicateType.create(TropicalFish.Pattern.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof TropicalFish) {
            TropicalFish tropicalFish = (TropicalFish)entity;
            optional = Optional.of(tropicalFish.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityHolderVariantPredicateType<PaintingVariant> PAINTING = EntitySubPredicates.register("painting", EntityHolderVariantPredicateType.create(Registries.PAINTING_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof Painting) {
            Painting painting = (Painting)entity;
            optional = Optional.of(painting.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityHolderVariantPredicateType<CatVariant> CAT = EntitySubPredicates.register("cat", EntityHolderVariantPredicateType.create(Registries.CAT_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof Cat) {
            Cat cat = (Cat)entity;
            optional = Optional.of(cat.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityHolderVariantPredicateType<FrogVariant> FROG = EntitySubPredicates.register("frog", EntityHolderVariantPredicateType.create(Registries.FROG_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof Frog) {
            Frog frog = (Frog)entity;
            optional = Optional.of(frog.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final EntityHolderVariantPredicateType<WolfVariant> WOLF = EntitySubPredicates.register("wolf", EntityHolderVariantPredicateType.create(Registries.WOLF_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            optional = Optional.of(wolf.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));

    private static <T extends EntitySubPredicate> MapCodec<T> register(String string, MapCodec<T> mapCodec) {
        return Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, string, mapCodec);
    }

    private static <V> EntityVariantPredicateType<V> register(String string, EntityVariantPredicateType<V> entityVariantPredicateType) {
        Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, string, entityVariantPredicateType.codec);
        return entityVariantPredicateType;
    }

    private static <V> EntityHolderVariantPredicateType<V> register(String string, EntityHolderVariantPredicateType<V> entityHolderVariantPredicateType) {
        Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, string, entityHolderVariantPredicateType.codec);
        return entityHolderVariantPredicateType;
    }

    public static MapCodec<? extends EntitySubPredicate> bootstrap(Registry<MapCodec<? extends EntitySubPredicate>> registry) {
        return LIGHTNING;
    }

    public static EntitySubPredicate catVariant(Holder<CatVariant> holder) {
        return CAT.createPredicate(HolderSet.direct(holder));
    }

    public static EntitySubPredicate frogVariant(Holder<FrogVariant> holder) {
        return FROG.createPredicate(HolderSet.direct(holder));
    }

    public static EntitySubPredicate wolfVariant(HolderSet<WolfVariant> holderSet) {
        return WOLF.createPredicate(holderSet);
    }

    public static class EntityVariantPredicateType<V> {
        final MapCodec<Instance> codec;
        final Function<Entity, Optional<V>> getter;

        public static <V> EntityVariantPredicateType<V> create(Registry<V> registry, Function<Entity, Optional<V>> function) {
            return new EntityVariantPredicateType<V>(registry.byNameCodec(), function);
        }

        public static <V> EntityVariantPredicateType<V> create(Codec<V> codec, Function<Entity, Optional<V>> function) {
            return new EntityVariantPredicateType<V>(codec, function);
        }

        public EntityVariantPredicateType(Codec<V> codec, Function<Entity, Optional<V>> function) {
            this.getter = function;
            this.codec = RecordCodecBuilder.mapCodec(instance2 -> instance2.group((App)codec.fieldOf("variant").forGetter(instance -> instance.variant)).apply((Applicative)instance2, object -> new Instance(object)));
        }

        public EntitySubPredicate createPredicate(V v) {
            return new Instance(v);
        }

        class Instance
        implements EntitySubPredicate {
            final V variant;

            Instance(V v) {
                this.variant = v;
            }

            public MapCodec<Instance> codec() {
                return EntityVariantPredicateType.this.codec;
            }

            @Override
            public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
                return EntityVariantPredicateType.this.getter.apply(entity).filter(this.variant::equals).isPresent();
            }
        }
    }

    public static class EntityHolderVariantPredicateType<V> {
        final MapCodec<Instance> codec;
        final Function<Entity, Optional<Holder<V>>> getter;

        public static <V> EntityHolderVariantPredicateType<V> create(ResourceKey<? extends Registry<V>> resourceKey, Function<Entity, Optional<Holder<V>>> function) {
            return new EntityHolderVariantPredicateType<V>(resourceKey, function);
        }

        public EntityHolderVariantPredicateType(ResourceKey<? extends Registry<V>> resourceKey, Function<Entity, Optional<Holder<V>>> function) {
            this.getter = function;
            this.codec = RecordCodecBuilder.mapCodec(instance2 -> instance2.group((App)RegistryCodecs.homogeneousList(resourceKey).fieldOf("variant").forGetter(instance -> instance.variants)).apply((Applicative)instance2, holderSet -> new Instance(holderSet)));
        }

        public EntitySubPredicate createPredicate(HolderSet<V> holderSet) {
            return new Instance(holderSet);
        }

        class Instance
        implements EntitySubPredicate {
            final HolderSet<V> variants;

            Instance(HolderSet<V> holderSet) {
                this.variants = holderSet;
            }

            public MapCodec<Instance> codec() {
                return EntityHolderVariantPredicateType.this.codec;
            }

            @Override
            public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
                return EntityHolderVariantPredicateType.this.getter.apply(entity).filter(this.variants::contains).isPresent();
            }
        }
    }
}

