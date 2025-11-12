/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class LootParams {
    private final ServerLevel level;
    private final Map<LootContextParam<?>, Object> params;
    private final Map<ResourceLocation, DynamicDrop> dynamicDrops;
    private final float luck;

    public LootParams(ServerLevel serverLevel, Map<LootContextParam<?>, Object> map, Map<ResourceLocation, DynamicDrop> map2, float f) {
        this.level = serverLevel;
        this.params = map;
        this.dynamicDrops = map2;
        this.luck = f;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean hasParam(LootContextParam<?> lootContextParam) {
        return this.params.containsKey(lootContextParam);
    }

    public <T> T getParameter(LootContextParam<T> lootContextParam) {
        Object object = this.params.get(lootContextParam);
        if (object == null) {
            throw new NoSuchElementException(lootContextParam.getName().toString());
        }
        return (T)object;
    }

    @Nullable
    public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
        return (T)this.params.get(lootContextParam);
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
        return (T)this.params.get(lootContextParam);
    }

    public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(resourceLocation);
        if (dynamicDrop != null) {
            dynamicDrop.add(consumer);
        }
    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public static interface DynamicDrop {
        public void add(Consumer<ItemStack> var1);
    }

    public static class Builder {
        private final ServerLevel level;
        private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> Builder withParameter(LootContextParam<T> lootContextParam, T t) {
            this.params.put(lootContextParam, t);
            return this;
        }

        public <T> Builder withOptionalParameter(LootContextParam<T> lootContextParam, @Nullable T t) {
            if (t == null) {
                this.params.remove(lootContextParam);
            } else {
                this.params.put(lootContextParam, t);
            }
            return this;
        }

        public <T> T getParameter(LootContextParam<T> lootContextParam) {
            Object object = this.params.get(lootContextParam);
            if (object == null) {
                throw new NoSuchElementException(lootContextParam.getName().toString());
            }
            return (T)object;
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
            return (T)this.params.get(lootContextParam);
        }

        public Builder withDynamicDrop(ResourceLocation resourceLocation, DynamicDrop dynamicDrop) {
            DynamicDrop dynamicDrop2 = this.dynamicDrops.put(resourceLocation, dynamicDrop);
            if (dynamicDrop2 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            }
            return this;
        }

        public Builder withLuck(float f) {
            this.luck = f;
            return this;
        }

        public LootParams create(LootContextParamSet lootContextParamSet) {
            Sets.SetView setView = Sets.difference(this.params.keySet(), lootContextParamSet.getAllowed());
            if (!setView.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(setView));
            }
            Sets.SetView setView2 = Sets.difference(lootContextParamSet.getRequired(), this.params.keySet());
            if (!setView2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(setView2));
            }
            return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
        }
    }
}

