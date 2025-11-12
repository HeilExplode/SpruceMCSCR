/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;

public class ContextNbtProvider
implements NbtProvider {
    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final Getter BLOCK_ENTITY_PROVIDER = new Getter(){

        @Override
        public Tag get(LootContext lootContext) {
            BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            return blockEntity != null ? blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess()) : null;
        }

        @Override
        public String getId() {
            return ContextNbtProvider.BLOCK_ENTITY_ID;
        }

        @Override
        public Set<LootContextParam<?>> getReferencedContextParams() {
            return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
        }
    };
    public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
    private static final Codec<Getter> GETTER_CODEC = Codec.STRING.xmap(string -> {
        if (string.equals(BLOCK_ENTITY_ID)) {
            return BLOCK_ENTITY_PROVIDER;
        }
        LootContext.EntityTarget entityTarget = LootContext.EntityTarget.getByName(string);
        return ContextNbtProvider.forEntity(entityTarget);
    }, Getter::getId);
    public static final MapCodec<ContextNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)GETTER_CODEC.fieldOf("target").forGetter(contextNbtProvider -> contextNbtProvider.getter)).apply((Applicative)instance, ContextNbtProvider::new));
    public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, contextNbtProvider -> contextNbtProvider.getter);
    private final Getter getter;

    private static Getter forEntity(final LootContext.EntityTarget entityTarget) {
        return new Getter(){

            @Override
            @Nullable
            public Tag get(LootContext lootContext) {
                Entity entity = lootContext.getParamOrNull(entityTarget.getParam());
                return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
            }

            @Override
            public String getId() {
                return entityTarget.name();
            }

            @Override
            public Set<LootContextParam<?>> getReferencedContextParams() {
                return ImmutableSet.of(entityTarget.getParam());
            }
        };
    }

    private ContextNbtProvider(Getter getter) {
        this.getter = getter;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Override
    @Nullable
    public Tag get(LootContext lootContext) {
        return this.getter.get(lootContext);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.getter.getReferencedContextParams();
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget entityTarget) {
        return new ContextNbtProvider(ContextNbtProvider.forEntity(entityTarget));
    }

    static interface Getter {
        @Nullable
        public Tag get(LootContext var1);

        public String getId();

        public Set<LootContextParam<?>> getReferencedContextParams();
    }
}

