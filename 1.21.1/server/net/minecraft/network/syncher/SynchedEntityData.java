/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ObjectUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ID_VALUE = 254;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SyncedDataHolder entity;
    private final DataItem<?>[] itemsById;
    private boolean isDirty;

    SynchedEntityData(SyncedDataHolder syncedDataHolder, DataItem<?>[] dataItemArray) {
        this.entity = syncedDataHolder;
        this.itemsById = dataItemArray;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> clazz, EntityDataSerializer<T> entityDataSerializer) {
        int n;
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> clazz2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!clazz2.equals(clazz)) {
                    LOGGER.debug("defineId called for: {} from {}", new Object[]{clazz, clazz2, new RuntimeException()});
                }
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
        }
        if ((n = ID_REGISTRY.define(clazz)) > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + n + "! (Max is 254)");
        }
        return entityDataSerializer.createAccessor(n);
    }

    private <T> DataItem<T> getItem(EntityDataAccessor<T> entityDataAccessor) {
        return this.itemsById[entityDataAccessor.id()];
    }

    public <T> T get(EntityDataAccessor<T> entityDataAccessor) {
        return this.getItem(entityDataAccessor).getValue();
    }

    public <T> void set(EntityDataAccessor<T> entityDataAccessor, T t) {
        this.set(entityDataAccessor, t, false);
    }

    public <T> void set(EntityDataAccessor<T> entityDataAccessor, T t, boolean bl) {
        DataItem<T> dataItem = this.getItem(entityDataAccessor);
        if (bl || ObjectUtils.notEqual(t, dataItem.getValue())) {
            dataItem.setValue(t);
            this.entity.onSyncedDataUpdated(entityDataAccessor);
            dataItem.setDirty(true);
            this.isDirty = true;
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<DataValue<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        }
        this.isDirty = false;
        ArrayList arrayList = new ArrayList();
        for (DataItem<?> dataItem : this.itemsById) {
            if (!dataItem.isDirty()) continue;
            dataItem.setDirty(false);
            arrayList.add(dataItem.value());
        }
        return arrayList;
    }

    @Nullable
    public List<DataValue<?>> getNonDefaultValues() {
        ArrayList arrayList = null;
        for (DataItem<?> dataItem : this.itemsById) {
            if (dataItem.isSetToDefault()) continue;
            if (arrayList == null) {
                arrayList = new ArrayList();
            }
            arrayList.add(dataItem.value());
        }
        return arrayList;
    }

    public void assignValues(List<DataValue<?>> list) {
        for (DataValue<?> dataValue : list) {
            DataItem<?> dataItem = this.itemsById[dataValue.id];
            this.assignValue(dataItem, dataValue);
            this.entity.onSyncedDataUpdated(dataItem.getAccessor());
        }
        this.entity.onSyncedDataUpdated(list);
    }

    private <T> void assignValue(DataItem<T> dataItem, DataValue<?> dataValue) {
        if (!Objects.equals(dataValue.serializer(), dataItem.accessor.serializer())) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", dataItem.accessor.id(), this.entity, dataItem.value, dataItem.value.getClass(), dataValue.value, dataValue.value.getClass()));
        }
        dataItem.setValue(dataValue.value);
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> entityDataAccessor, T t) {
            this.accessor = entityDataAccessor;
            this.initialValue = t;
            this.value = t;
        }

        public EntityDataAccessor<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T t) {
            this.value = t;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean bl) {
            this.dirty = bl;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public DataValue<T> value() {
            return DataValue.create(this.accessor, this.value);
        }
    }

    public static final class DataValue<T>
    extends Record {
        final int id;
        private final EntityDataSerializer<T> serializer;
        final T value;

        public DataValue(int n, EntityDataSerializer<T> entityDataSerializer, T t) {
            this.id = n;
            this.serializer = entityDataSerializer;
            this.value = t;
        }

        public static <T> DataValue<T> create(EntityDataAccessor<T> entityDataAccessor, T t) {
            EntityDataSerializer<T> entityDataSerializer = entityDataAccessor.serializer();
            return new DataValue<T>(entityDataAccessor.id(), entityDataSerializer, entityDataSerializer.copy(t));
        }

        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            int n = EntityDataSerializers.getSerializedId(this.serializer);
            if (n < 0) {
                throw new EncoderException("Unknown serializer type " + String.valueOf(this.serializer));
            }
            registryFriendlyByteBuf.writeByte(this.id);
            registryFriendlyByteBuf.writeVarInt(n);
            this.serializer.codec().encode(registryFriendlyByteBuf, this.value);
        }

        public static DataValue<?> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int n) {
            int n2 = registryFriendlyByteBuf.readVarInt();
            EntityDataSerializer<?> entityDataSerializer = EntityDataSerializers.getSerializer(n2);
            if (entityDataSerializer == null) {
                throw new DecoderException("Unknown serializer type " + n2);
            }
            return DataValue.read(registryFriendlyByteBuf, n, entityDataSerializer);
        }

        private static <T> DataValue<T> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int n, EntityDataSerializer<T> entityDataSerializer) {
            return new DataValue<T>(n, entityDataSerializer, entityDataSerializer.codec().decode(registryFriendlyByteBuf));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this, object);
        }

        public int id() {
            return this.id;
        }

        public EntityDataSerializer<T> serializer() {
            return this.serializer;
        }

        public T value() {
            return this.value;
        }
    }

    public static class Builder {
        private final SyncedDataHolder entity;
        private final DataItem<?>[] itemsById;

        public Builder(SyncedDataHolder syncedDataHolder) {
            this.entity = syncedDataHolder;
            this.itemsById = new DataItem[ID_REGISTRY.getCount(syncedDataHolder.getClass())];
        }

        public <T> Builder define(EntityDataAccessor<T> entityDataAccessor, T t) {
            int n = entityDataAccessor.id();
            if (n > this.itemsById.length) {
                throw new IllegalArgumentException("Data value id is too big with " + n + "! (Max is " + this.itemsById.length + ")");
            }
            if (this.itemsById[n] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + n + "!");
            }
            if (EntityDataSerializers.getSerializedId(entityDataAccessor.serializer()) < 0) {
                throw new IllegalArgumentException("Unregistered serializer " + String.valueOf(entityDataAccessor.serializer()) + " for " + n + "!");
            }
            this.itemsById[entityDataAccessor.id()] = new DataItem<T>(entityDataAccessor, t);
            return this;
        }

        public SynchedEntityData build() {
            for (int i = 0; i < this.itemsById.length; ++i) {
                if (this.itemsById[i] != null) continue;
                throw new IllegalStateException("Entity " + String.valueOf(this.entity.getClass()) + " has not defined synched data value " + i);
            }
            return new SynchedEntityData(this.entity, this.itemsById);
        }
    }
}

