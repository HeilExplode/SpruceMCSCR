/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public class Rotations {
    public static final StreamCodec<ByteBuf, Rotations> STREAM_CODEC = new StreamCodec<ByteBuf, Rotations>(){

        @Override
        public Rotations decode(ByteBuf byteBuf) {
            return new Rotations(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Rotations rotations) {
            byteBuf.writeFloat(rotations.x);
            byteBuf.writeFloat(rotations.y);
            byteBuf.writeFloat(rotations.z);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Rotations)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    protected final float x;
    protected final float y;
    protected final float z;

    public Rotations(float f, float f2, float f3) {
        this.x = Float.isInfinite(f) || Float.isNaN(f) ? 0.0f : f % 360.0f;
        this.y = Float.isInfinite(f2) || Float.isNaN(f2) ? 0.0f : f2 % 360.0f;
        this.z = Float.isInfinite(f3) || Float.isNaN(f3) ? 0.0f : f3 % 360.0f;
    }

    public Rotations(ListTag listTag) {
        this(listTag.getFloat(0), listTag.getFloat(1), listTag.getFloat(2));
    }

    public ListTag save() {
        ListTag listTag = new ListTag();
        listTag.add(FloatTag.valueOf(this.x));
        listTag.add(FloatTag.valueOf(this.y));
        listTag.add(FloatTag.valueOf(this.z));
        return listTag;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Rotations)) {
            return false;
        }
        Rotations rotations = (Rotations)object;
        return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getWrappedX() {
        return Mth.wrapDegrees(this.x);
    }

    public float getWrappedY() {
        return Mth.wrapDegrees(this.y);
    }

    public float getWrappedZ() {
        return Mth.wrapDegrees(this.z);
    }
}

