/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Triple
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.mojang.math;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.math.MatrixUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Transformation {
    private final Matrix4f matrix;
    public static final Codec<Transformation> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.VECTOR3F.fieldOf("translation").forGetter(transformation -> transformation.translation), (App)ExtraCodecs.QUATERNIONF.fieldOf("left_rotation").forGetter(transformation -> transformation.leftRotation), (App)ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter(transformation -> transformation.scale), (App)ExtraCodecs.QUATERNIONF.fieldOf("right_rotation").forGetter(transformation -> transformation.rightRotation)).apply((Applicative)instance, Transformation::new));
    public static final Codec<Transformation> EXTENDED_CODEC = Codec.withAlternative(CODEC, (Codec)ExtraCodecs.MATRIX4F.xmap(Transformation::new, Transformation::getMatrix));
    private boolean decomposed;
    @Nullable
    private Vector3f translation;
    @Nullable
    private Quaternionf leftRotation;
    @Nullable
    private Vector3f scale;
    @Nullable
    private Quaternionf rightRotation;
    private static final Transformation IDENTITY = Util.make(() -> {
        Transformation transformation = new Transformation(new Matrix4f());
        transformation.translation = new Vector3f();
        transformation.leftRotation = new Quaternionf();
        transformation.scale = new Vector3f(1.0f, 1.0f, 1.0f);
        transformation.rightRotation = new Quaternionf();
        transformation.decomposed = true;
        return transformation;
    });

    public Transformation(@Nullable Matrix4f matrix4f) {
        this.matrix = matrix4f == null ? new Matrix4f() : matrix4f;
    }

    public Transformation(@Nullable Vector3f vector3f, @Nullable Quaternionf quaternionf, @Nullable Vector3f vector3f2, @Nullable Quaternionf quaternionf2) {
        this.matrix = Transformation.compose(vector3f, quaternionf, vector3f2, quaternionf2);
        this.translation = vector3f != null ? vector3f : new Vector3f();
        this.leftRotation = quaternionf != null ? quaternionf : new Quaternionf();
        this.scale = vector3f2 != null ? vector3f2 : new Vector3f(1.0f, 1.0f, 1.0f);
        this.rightRotation = quaternionf2 != null ? quaternionf2 : new Quaternionf();
        this.decomposed = true;
    }

    public static Transformation identity() {
        return IDENTITY;
    }

    public Transformation compose(Transformation transformation) {
        Matrix4f matrix4f = this.getMatrix();
        matrix4f.mul((Matrix4fc)transformation.getMatrix());
        return new Transformation(matrix4f);
    }

    @Nullable
    public Transformation inverse() {
        if (this == IDENTITY) {
            return this;
        }
        Matrix4f matrix4f = this.getMatrix().invert();
        if (matrix4f.isFinite()) {
            return new Transformation(matrix4f);
        }
        return null;
    }

    private void ensureDecomposed() {
        if (!this.decomposed) {
            float f = 1.0f / this.matrix.m33();
            Triple<Quaternionf, Vector3f, Quaternionf> triple = MatrixUtil.svdDecompose(new Matrix3f((Matrix4fc)this.matrix).scale(f));
            this.translation = this.matrix.getTranslation(new Vector3f()).mul(f);
            this.leftRotation = new Quaternionf((Quaternionfc)triple.getLeft());
            this.scale = new Vector3f((Vector3fc)triple.getMiddle());
            this.rightRotation = new Quaternionf((Quaternionfc)triple.getRight());
            this.decomposed = true;
        }
    }

    private static Matrix4f compose(@Nullable Vector3f vector3f, @Nullable Quaternionf quaternionf, @Nullable Vector3f vector3f2, @Nullable Quaternionf quaternionf2) {
        Matrix4f matrix4f = new Matrix4f();
        if (vector3f != null) {
            matrix4f.translation((Vector3fc)vector3f);
        }
        if (quaternionf != null) {
            matrix4f.rotate((Quaternionfc)quaternionf);
        }
        if (vector3f2 != null) {
            matrix4f.scale((Vector3fc)vector3f2);
        }
        if (quaternionf2 != null) {
            matrix4f.rotate((Quaternionfc)quaternionf2);
        }
        return matrix4f;
    }

    public Matrix4f getMatrix() {
        return new Matrix4f((Matrix4fc)this.matrix);
    }

    public Vector3f getTranslation() {
        this.ensureDecomposed();
        return new Vector3f((Vector3fc)this.translation);
    }

    public Quaternionf getLeftRotation() {
        this.ensureDecomposed();
        return new Quaternionf((Quaternionfc)this.leftRotation);
    }

    public Vector3f getScale() {
        this.ensureDecomposed();
        return new Vector3f((Vector3fc)this.scale);
    }

    public Quaternionf getRightRotation() {
        this.ensureDecomposed();
        return new Quaternionf((Quaternionfc)this.rightRotation);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Transformation transformation = (Transformation)object;
        return Objects.equals(this.matrix, transformation.matrix);
    }

    public int hashCode() {
        return Objects.hash(this.matrix);
    }

    public Transformation slerp(Transformation transformation, float f) {
        Vector3f vector3f = this.getTranslation();
        Quaternionf quaternionf = this.getLeftRotation();
        Vector3f vector3f2 = this.getScale();
        Quaternionf quaternionf2 = this.getRightRotation();
        vector3f.lerp((Vector3fc)transformation.getTranslation(), f);
        quaternionf.slerp((Quaternionfc)transformation.getLeftRotation(), f);
        vector3f2.lerp((Vector3fc)transformation.getScale(), f);
        quaternionf2.slerp((Quaternionfc)transformation.getRightRotation(), f);
        return new Transformation(vector3f, quaternionf, vector3f2, quaternionf2);
    }
}

