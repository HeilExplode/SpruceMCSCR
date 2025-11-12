/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.syncher;

import net.minecraft.network.syncher.EntityDataSerializer;

public record EntityDataAccessor<T>(int id, EntityDataSerializer<T> serializer) {
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EntityDataAccessor entityDataAccessor = (EntityDataAccessor)object;
        return this.id == entityDataAccessor.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}

