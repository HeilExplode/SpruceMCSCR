/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display
extends Entity {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int NO_BRIGHTNESS_OVERRIDE = -1;
    private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_POS_ROT_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Quaternionf> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Quaternionf> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final IntSet RENDER_STATE_IDS = IntSet.of((int[])new int[]{DATA_TRANSLATION_ID.id(), DATA_SCALE_ID.id(), DATA_LEFT_ROTATION_ID.id(), DATA_RIGHT_ROTATION_ID.id(), DATA_BILLBOARD_RENDER_CONSTRAINTS_ID.id(), DATA_BRIGHTNESS_OVERRIDE_ID.id(), DATA_SHADOW_RADIUS_ID.id(), DATA_SHADOW_STRENGTH_ID.id()});
    private static final float INITIAL_SHADOW_RADIUS = 0.0f;
    private static final float INITIAL_SHADOW_STRENGTH = 1.0f;
    private static final int NO_GLOW_COLOR_OVERRIDE = -1;
    public static final String TAG_POS_ROT_INTERPOLATION_DURATION = "teleport_duration";
    public static final String TAG_TRANSFORMATION_INTERPOLATION_DURATION = "interpolation_duration";
    public static final String TAG_TRANSFORMATION_START_INTERPOLATION = "start_interpolation";
    public static final String TAG_TRANSFORMATION = "transformation";
    public static final String TAG_BILLBOARD = "billboard";
    public static final String TAG_BRIGHTNESS = "brightness";
    public static final String TAG_VIEW_RANGE = "view_range";
    public static final String TAG_SHADOW_RADIUS = "shadow_radius";
    public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
    private long interpolationStartClientTick = Integer.MIN_VALUE;
    private int interpolationDuration;
    private float lastProgress;
    private AABB cullingBoundingBox;
    protected boolean updateRenderState;
    private boolean updateStartTick;
    private boolean updateInterpolationDuration;
    @Nullable
    private RenderState renderState;
    @Nullable
    private PosRotInterpolationTarget posRotInterpolationTarget;

    public Display(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.cullingBoundingBox = this.getBoundingBox();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
            this.updateCulling();
        }
        if (DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID.equals(entityDataAccessor)) {
            this.updateStartTick = true;
        }
        if (DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID.equals(entityDataAccessor)) {
            this.updateInterpolationDuration = true;
        }
        if (RENDER_STATE_IDS.contains(entityDataAccessor.id())) {
            this.updateRenderState = true;
        }
    }

    private static Transformation createTransformation(SynchedEntityData synchedEntityData) {
        Vector3f vector3f = synchedEntityData.get(DATA_TRANSLATION_ID);
        Quaternionf quaternionf = synchedEntityData.get(DATA_LEFT_ROTATION_ID);
        Vector3f vector3f2 = synchedEntityData.get(DATA_SCALE_ID);
        Quaternionf quaternionf2 = synchedEntityData.get(DATA_RIGHT_ROTATION_ID);
        return new Transformation(vector3f, quaternionf, vector3f2, quaternionf2);
    }

    @Override
    public void tick() {
        Entity entity = this.getVehicle();
        if (entity != null && entity.isRemoved()) {
            this.stopRiding();
        }
        if (this.level().isClientSide) {
            int n;
            if (this.updateStartTick) {
                this.updateStartTick = false;
                n = this.getTransformationInterpolationDelay();
                this.interpolationStartClientTick = this.tickCount + n;
            }
            if (this.updateInterpolationDuration) {
                this.updateInterpolationDuration = false;
                this.interpolationDuration = this.getTransformationInterpolationDuration();
            }
            if (this.updateRenderState) {
                this.updateRenderState = false;
                n = this.interpolationDuration != 0 ? 1 : 0;
                this.renderState = n != 0 && this.renderState != null ? this.createInterpolatedRenderState(this.renderState, this.lastProgress) : this.createFreshRenderState();
                this.updateRenderSubState(n != 0, this.lastProgress);
            }
            if (this.posRotInterpolationTarget != null) {
                if (this.posRotInterpolationTarget.steps == 0) {
                    this.posRotInterpolationTarget.applyTargetPosAndRot(this);
                    this.setOldPosAndRot();
                    this.posRotInterpolationTarget = null;
                } else {
                    this.posRotInterpolationTarget.applyLerpStep(this);
                    --this.posRotInterpolationTarget.steps;
                    if (this.posRotInterpolationTarget.steps == 0) {
                        this.posRotInterpolationTarget = null;
                    }
                }
            }
        }
    }

    protected abstract void updateRenderSubState(boolean var1, float var2);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0);
        builder.define(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, 0);
        builder.define(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, 0);
        builder.define(DATA_TRANSLATION_ID, new Vector3f());
        builder.define(DATA_SCALE_ID, new Vector3f(1.0f, 1.0f, 1.0f));
        builder.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
        builder.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
        builder.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, BillboardConstraints.FIXED.getId());
        builder.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
        builder.define(DATA_VIEW_RANGE_ID, Float.valueOf(1.0f));
        builder.define(DATA_SHADOW_RADIUS_ID, Float.valueOf(0.0f));
        builder.define(DATA_SHADOW_STRENGTH_ID, Float.valueOf(1.0f));
        builder.define(DATA_WIDTH_ID, Float.valueOf(0.0f));
        builder.define(DATA_HEIGHT_ID, Float.valueOf(0.0f));
        builder.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        int n;
        if (compoundTag.contains(TAG_TRANSFORMATION)) {
            Transformation.EXTENDED_CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_TRANSFORMATION)).resultOrPartial(Util.prefix("Display entity", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(pair -> this.setTransformation((Transformation)pair.getFirst()));
        }
        if (compoundTag.contains(TAG_TRANSFORMATION_INTERPOLATION_DURATION, 99)) {
            n = compoundTag.getInt(TAG_TRANSFORMATION_INTERPOLATION_DURATION);
            this.setTransformationInterpolationDuration(n);
        }
        if (compoundTag.contains(TAG_TRANSFORMATION_START_INTERPOLATION, 99)) {
            n = compoundTag.getInt(TAG_TRANSFORMATION_START_INTERPOLATION);
            this.setTransformationInterpolationDelay(n);
        }
        if (compoundTag.contains(TAG_POS_ROT_INTERPOLATION_DURATION, 99)) {
            n = compoundTag.getInt(TAG_POS_ROT_INTERPOLATION_DURATION);
            this.setPosRotInterpolationDuration(Mth.clamp(n, 0, 59));
        }
        if (compoundTag.contains(TAG_BILLBOARD, 8)) {
            BillboardConstraints.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_BILLBOARD)).resultOrPartial(Util.prefix("Display entity", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(pair -> this.setBillboardConstraints((BillboardConstraints)pair.getFirst()));
        }
        if (compoundTag.contains(TAG_VIEW_RANGE, 99)) {
            this.setViewRange(compoundTag.getFloat(TAG_VIEW_RANGE));
        }
        if (compoundTag.contains(TAG_SHADOW_RADIUS, 99)) {
            this.setShadowRadius(compoundTag.getFloat(TAG_SHADOW_RADIUS));
        }
        if (compoundTag.contains(TAG_SHADOW_STRENGTH, 99)) {
            this.setShadowStrength(compoundTag.getFloat(TAG_SHADOW_STRENGTH));
        }
        if (compoundTag.contains(TAG_WIDTH, 99)) {
            this.setWidth(compoundTag.getFloat(TAG_WIDTH));
        }
        if (compoundTag.contains(TAG_HEIGHT, 99)) {
            this.setHeight(compoundTag.getFloat(TAG_HEIGHT));
        }
        if (compoundTag.contains(TAG_GLOW_COLOR_OVERRIDE, 99)) {
            this.setGlowColorOverride(compoundTag.getInt(TAG_GLOW_COLOR_OVERRIDE));
        }
        if (compoundTag.contains(TAG_BRIGHTNESS, 10)) {
            Brightness.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_BRIGHTNESS)).resultOrPartial(Util.prefix("Display entity", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(pair -> this.setBrightnessOverride((Brightness)pair.getFirst()));
        } else {
            this.setBrightnessOverride(null);
        }
    }

    private void setTransformation(Transformation transformation) {
        this.entityData.set(DATA_TRANSLATION_ID, transformation.getTranslation());
        this.entityData.set(DATA_LEFT_ROTATION_ID, transformation.getLeftRotation());
        this.entityData.set(DATA_SCALE_ID, transformation.getScale());
        this.entityData.set(DATA_RIGHT_ROTATION_ID, transformation.getRightRotation());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        Transformation.EXTENDED_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)Display.createTransformation(this.entityData)).ifSuccess(tag -> compoundTag.put(TAG_TRANSFORMATION, (Tag)tag));
        BillboardConstraints.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.getBillboardConstraints()).ifSuccess(tag -> compoundTag.put(TAG_BILLBOARD, (Tag)tag));
        compoundTag.putInt(TAG_TRANSFORMATION_INTERPOLATION_DURATION, this.getTransformationInterpolationDuration());
        compoundTag.putInt(TAG_POS_ROT_INTERPOLATION_DURATION, this.getPosRotInterpolationDuration());
        compoundTag.putFloat(TAG_VIEW_RANGE, this.getViewRange());
        compoundTag.putFloat(TAG_SHADOW_RADIUS, this.getShadowRadius());
        compoundTag.putFloat(TAG_SHADOW_STRENGTH, this.getShadowStrength());
        compoundTag.putFloat(TAG_WIDTH, this.getWidth());
        compoundTag.putFloat(TAG_HEIGHT, this.getHeight());
        compoundTag.putInt(TAG_GLOW_COLOR_OVERRIDE, this.getGlowColorOverride());
        Brightness brightness = this.getBrightnessOverride();
        if (brightness != null) {
            Brightness.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)brightness).ifSuccess(tag -> compoundTag.put(TAG_BRIGHTNESS, (Tag)tag));
        }
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        int n2 = this.getPosRotInterpolationDuration();
        this.posRotInterpolationTarget = new PosRotInterpolationTarget(n2, d, d2, d3, f, f2);
    }

    @Override
    public double lerpTargetX() {
        return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.posRotInterpolationTarget != null ? (float)this.posRotInterpolationTarget.targetXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.posRotInterpolationTarget != null ? (float)this.posRotInterpolationTarget.targetYRot : this.getYRot();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.cullingBoundingBox;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Nullable
    public RenderState renderState() {
        return this.renderState;
    }

    private void setTransformationInterpolationDuration(int n) {
        this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, n);
    }

    private int getTransformationInterpolationDuration() {
        return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID);
    }

    private void setTransformationInterpolationDelay(int n) {
        this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, n, true);
    }

    private int getTransformationInterpolationDelay() {
        return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID);
    }

    private void setPosRotInterpolationDuration(int n) {
        this.entityData.set(DATA_POS_ROT_INTERPOLATION_DURATION_ID, n);
    }

    private int getPosRotInterpolationDuration() {
        return this.entityData.get(DATA_POS_ROT_INTERPOLATION_DURATION_ID);
    }

    private void setBillboardConstraints(BillboardConstraints billboardConstraints) {
        this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, billboardConstraints.getId());
    }

    private BillboardConstraints getBillboardConstraints() {
        return BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID).byteValue());
    }

    private void setBrightnessOverride(@Nullable Brightness brightness) {
        this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, brightness != null ? brightness.pack() : -1);
    }

    @Nullable
    private Brightness getBrightnessOverride() {
        int n = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
        return n != -1 ? Brightness.unpack(n) : null;
    }

    private int getPackedBrightnessOverride() {
        return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
    }

    private void setViewRange(float f) {
        this.entityData.set(DATA_VIEW_RANGE_ID, Float.valueOf(f));
    }

    private float getViewRange() {
        return this.entityData.get(DATA_VIEW_RANGE_ID).floatValue();
    }

    private void setShadowRadius(float f) {
        this.entityData.set(DATA_SHADOW_RADIUS_ID, Float.valueOf(f));
    }

    private float getShadowRadius() {
        return this.entityData.get(DATA_SHADOW_RADIUS_ID).floatValue();
    }

    private void setShadowStrength(float f) {
        this.entityData.set(DATA_SHADOW_STRENGTH_ID, Float.valueOf(f));
    }

    private float getShadowStrength() {
        return this.entityData.get(DATA_SHADOW_STRENGTH_ID).floatValue();
    }

    private void setWidth(float f) {
        this.entityData.set(DATA_WIDTH_ID, Float.valueOf(f));
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID).floatValue();
    }

    private void setHeight(float f) {
        this.entityData.set(DATA_HEIGHT_ID, Float.valueOf(f));
    }

    private int getGlowColorOverride() {
        return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
    }

    private void setGlowColorOverride(int n) {
        this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, n);
    }

    public float calculateInterpolationProgress(float f) {
        float f2;
        int n = this.interpolationDuration;
        if (n <= 0) {
            return 1.0f;
        }
        float f3 = (long)this.tickCount - this.interpolationStartClientTick;
        float f4 = f3 + f;
        this.lastProgress = f2 = Mth.clamp(Mth.inverseLerp(f4, 0.0f, n), 0.0f, 1.0f);
        return f2;
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID).floatValue();
    }

    @Override
    public void setPos(double d, double d2, double d3) {
        super.setPos(d, d2, d3);
        this.updateCulling();
    }

    private void updateCulling() {
        float f = this.getWidth();
        float f2 = this.getHeight();
        if (f == 0.0f || f2 == 0.0f) {
            this.noCulling = true;
        } else {
            this.noCulling = false;
            float f3 = f / 2.0f;
            double d = this.getX();
            double d2 = this.getY();
            double d3 = this.getZ();
            this.cullingBoundingBox = new AABB(d - (double)f3, d2, d3 - (double)f3, d + (double)f3, d2 + (double)f2, d3 + (double)f3);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < Mth.square((double)this.getViewRange() * 64.0 * Display.getViewScale());
    }

    @Override
    public int getTeamColor() {
        int n = this.getGlowColorOverride();
        return n != -1 ? n : super.getTeamColor();
    }

    private RenderState createFreshRenderState() {
        return new RenderState(GenericInterpolator.constant(Display.createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), FloatInterpolator.constant(this.getShadowRadius()), FloatInterpolator.constant(this.getShadowStrength()), this.getGlowColorOverride());
    }

    private RenderState createInterpolatedRenderState(RenderState renderState, float f) {
        Transformation transformation = renderState.transformation.get(f);
        float f2 = renderState.shadowRadius.get(f);
        float f3 = renderState.shadowStrength.get(f);
        return new RenderState(new TransformationInterpolator(transformation, Display.createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), new LinearFloatInterpolator(f2, this.getShadowRadius()), new LinearFloatInterpolator(f3, this.getShadowStrength()), this.getGlowColorOverride());
    }

    public static final class RenderState
    extends Record {
        final GenericInterpolator<Transformation> transformation;
        private final BillboardConstraints billboardConstraints;
        private final int brightnessOverride;
        final FloatInterpolator shadowRadius;
        final FloatInterpolator shadowStrength;
        private final int glowColorOverride;

        public RenderState(GenericInterpolator<Transformation> genericInterpolator, BillboardConstraints billboardConstraints, int n, FloatInterpolator floatInterpolator, FloatInterpolator floatInterpolator2, int n2) {
            this.transformation = genericInterpolator;
            this.billboardConstraints = billboardConstraints;
            this.brightnessOverride = n;
            this.shadowRadius = floatInterpolator;
            this.shadowStrength = floatInterpolator2;
            this.glowColorOverride = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this, object);
        }

        public GenericInterpolator<Transformation> transformation() {
            return this.transformation;
        }

        public BillboardConstraints billboardConstraints() {
            return this.billboardConstraints;
        }

        public int brightnessOverride() {
            return this.brightnessOverride;
        }

        public FloatInterpolator shadowRadius() {
            return this.shadowRadius;
        }

        public FloatInterpolator shadowStrength() {
            return this.shadowStrength;
        }

        public int glowColorOverride() {
            return this.glowColorOverride;
        }
    }

    static class PosRotInterpolationTarget {
        int steps;
        final double targetX;
        final double targetY;
        final double targetZ;
        final double targetYRot;
        final double targetXRot;

        PosRotInterpolationTarget(int n, double d, double d2, double d3, double d4, double d5) {
            this.steps = n;
            this.targetX = d;
            this.targetY = d2;
            this.targetZ = d3;
            this.targetYRot = d4;
            this.targetXRot = d5;
        }

        void applyTargetPosAndRot(Entity entity) {
            entity.setPos(this.targetX, this.targetY, this.targetZ);
            entity.setRot((float)this.targetYRot, (float)this.targetXRot);
        }

        void applyLerpStep(Entity entity) {
            entity.lerpPositionAndRotationStep(this.steps, this.targetX, this.targetY, this.targetZ, this.targetYRot, this.targetXRot);
        }
    }

    public static enum BillboardConstraints implements StringRepresentable
    {
        FIXED(0, "fixed"),
        VERTICAL(1, "vertical"),
        HORIZONTAL(2, "horizontal"),
        CENTER(3, "center");

        public static final Codec<BillboardConstraints> CODEC;
        public static final IntFunction<BillboardConstraints> BY_ID;
        private final byte id;
        private final String name;

        private BillboardConstraints(byte by, String string2) {
            this.name = string2;
            this.id = by;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        byte getId() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(BillboardConstraints::values);
            BY_ID = ByIdMap.continuous(BillboardConstraints::getId, BillboardConstraints.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }

    @FunctionalInterface
    public static interface GenericInterpolator<T> {
        public static <T> GenericInterpolator<T> constant(T t) {
            return f -> t;
        }

        public T get(float var1);
    }

    @FunctionalInterface
    public static interface FloatInterpolator {
        public static FloatInterpolator constant(float f) {
            return f2 -> f;
        }

        public float get(float var1);
    }

    record TransformationInterpolator(Transformation previous, Transformation current) implements GenericInterpolator<Transformation>
    {
        @Override
        public Transformation get(float f) {
            if ((double)f >= 1.0) {
                return this.current;
            }
            return this.previous.slerp(this.current, f);
        }

        @Override
        public /* synthetic */ Object get(float f) {
            return this.get(f);
        }
    }

    record LinearFloatInterpolator(float previous, float current) implements FloatInterpolator
    {
        @Override
        public float get(float f) {
            return Mth.lerp(f, this.previous, this.current);
        }
    }

    record ColorInterpolator(int previous, int current) implements IntInterpolator
    {
        @Override
        public int get(float f) {
            return FastColor.ARGB32.lerp(f, this.previous, this.current);
        }
    }

    record LinearIntInterpolator(int previous, int current) implements IntInterpolator
    {
        @Override
        public int get(float f) {
            return Mth.lerpInt(f, this.previous, this.current);
        }
    }

    @FunctionalInterface
    public static interface IntInterpolator {
        public static IntInterpolator constant(int n) {
            return f -> n;
        }

        public int get(float var1);
    }

    public static class TextDisplay
    extends Display {
        public static final String TAG_TEXT = "text";
        private static final String TAG_LINE_WIDTH = "line_width";
        private static final String TAG_TEXT_OPACITY = "text_opacity";
        private static final String TAG_BACKGROUND_COLOR = "background";
        private static final String TAG_SHADOW = "shadow";
        private static final String TAG_SEE_THROUGH = "see_through";
        private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
        private static final String TAG_ALIGNMENT = "alignment";
        public static final byte FLAG_SHADOW = 1;
        public static final byte FLAG_SEE_THROUGH = 2;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
        public static final byte FLAG_ALIGN_LEFT = 8;
        public static final byte FLAG_ALIGN_RIGHT = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 0x40000000;
        private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.COMPONENT);
        private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private static final IntSet TEXT_RENDER_STATE_IDS = IntSet.of((int[])new int[]{DATA_TEXT_ID.id(), DATA_LINE_WIDTH_ID.id(), DATA_BACKGROUND_COLOR_ID.id(), DATA_TEXT_OPACITY_ID.id(), DATA_STYLE_FLAGS_ID.id()});
        @Nullable
        private CachedInfo clientDisplayCache;
        @Nullable
        private TextRenderState textRenderState;

        public TextDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            super.defineSynchedData(builder);
            builder.define(DATA_TEXT_ID, Component.empty());
            builder.define(DATA_LINE_WIDTH_ID, 200);
            builder.define(DATA_BACKGROUND_COLOR_ID, 0x40000000);
            builder.define(DATA_TEXT_OPACITY_ID, (byte)-1);
            builder.define(DATA_STYLE_FLAGS_ID, (byte)0);
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
            super.onSyncedDataUpdated(entityDataAccessor);
            if (TEXT_RENDER_STATE_IDS.contains(entityDataAccessor.id())) {
                this.updateRenderState = true;
            }
        }

        private Component getText() {
            return this.entityData.get(DATA_TEXT_ID);
        }

        private void setText(Component component) {
            this.entityData.set(DATA_TEXT_ID, component);
        }

        private int getLineWidth() {
            return this.entityData.get(DATA_LINE_WIDTH_ID);
        }

        private void setLineWidth(int n) {
            this.entityData.set(DATA_LINE_WIDTH_ID, n);
        }

        private byte getTextOpacity() {
            return this.entityData.get(DATA_TEXT_OPACITY_ID);
        }

        private void setTextOpacity(byte by) {
            this.entityData.set(DATA_TEXT_OPACITY_ID, by);
        }

        private int getBackgroundColor() {
            return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
        }

        private void setBackgroundColor(int n) {
            this.entityData.set(DATA_BACKGROUND_COLOR_ID, n);
        }

        private byte getFlags() {
            return this.entityData.get(DATA_STYLE_FLAGS_ID);
        }

        private void setFlags(byte by) {
            this.entityData.set(DATA_STYLE_FLAGS_ID, by);
        }

        private static byte loadFlag(byte by, CompoundTag compoundTag, String string, byte by2) {
            if (compoundTag.getBoolean(string)) {
                return (byte)(by | by2);
            }
            return by;
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            if (compoundTag.contains(TAG_LINE_WIDTH, 99)) {
                this.setLineWidth(compoundTag.getInt(TAG_LINE_WIDTH));
            }
            if (compoundTag.contains(TAG_TEXT_OPACITY, 99)) {
                this.setTextOpacity(compoundTag.getByte(TAG_TEXT_OPACITY));
            }
            if (compoundTag.contains(TAG_BACKGROUND_COLOR, 99)) {
                this.setBackgroundColor(compoundTag.getInt(TAG_BACKGROUND_COLOR));
            }
            byte by = TextDisplay.loadFlag((byte)0, compoundTag, TAG_SHADOW, (byte)1);
            by = TextDisplay.loadFlag(by, compoundTag, TAG_SEE_THROUGH, (byte)2);
            by = TextDisplay.loadFlag(by, compoundTag, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            Optional<Align> optional = Align.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_ALIGNMENT)).resultOrPartial(Util.prefix("Display entity", arg_0 -> ((Logger)LOGGER).error(arg_0))).map(Pair::getFirst);
            if (optional.isPresent()) {
                by = switch (optional.get().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> by;
                    case 1 -> (byte)(by | 8);
                    case 2 -> (byte)(by | 0x10);
                };
            }
            this.setFlags(by);
            if (compoundTag.contains(TAG_TEXT, 8)) {
                String string = compoundTag.getString(TAG_TEXT);
                try {
                    MutableComponent mutableComponent = Component.Serializer.fromJson(string, (HolderLookup.Provider)this.registryAccess());
                    if (mutableComponent != null) {
                        CommandSourceStack commandSourceStack = this.createCommandSourceStack().withPermission(2);
                        MutableComponent mutableComponent2 = ComponentUtils.updateForEntity(commandSourceStack, mutableComponent, (Entity)this, 0);
                        this.setText(mutableComponent2);
                    } else {
                        this.setText(Component.empty());
                    }
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to parse display entity text {}", (Object)string, (Object)exception);
                }
            }
        }

        private static void storeFlag(byte by, CompoundTag compoundTag, String string, byte by2) {
            compoundTag.putBoolean(string, (by & by2) != 0);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString(TAG_TEXT, Component.Serializer.toJson(this.getText(), this.registryAccess()));
            compoundTag.putInt(TAG_LINE_WIDTH, this.getLineWidth());
            compoundTag.putInt(TAG_BACKGROUND_COLOR, this.getBackgroundColor());
            compoundTag.putByte(TAG_TEXT_OPACITY, this.getTextOpacity());
            byte by = this.getFlags();
            TextDisplay.storeFlag(by, compoundTag, TAG_SHADOW, (byte)1);
            TextDisplay.storeFlag(by, compoundTag, TAG_SEE_THROUGH, (byte)2);
            TextDisplay.storeFlag(by, compoundTag, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            Align.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)TextDisplay.getAlign(by)).ifSuccess(tag -> compoundTag.put(TAG_ALIGNMENT, (Tag)tag));
        }

        @Override
        protected void updateRenderSubState(boolean bl, float f) {
            this.textRenderState = bl && this.textRenderState != null ? this.createInterpolatedTextRenderState(this.textRenderState, f) : this.createFreshTextRenderState();
            this.clientDisplayCache = null;
        }

        @Nullable
        public TextRenderState textRenderState() {
            return this.textRenderState;
        }

        private TextRenderState createFreshTextRenderState() {
            return new TextRenderState(this.getText(), this.getLineWidth(), IntInterpolator.constant(this.getTextOpacity()), IntInterpolator.constant(this.getBackgroundColor()), this.getFlags());
        }

        private TextRenderState createInterpolatedTextRenderState(TextRenderState textRenderState, float f) {
            int n = textRenderState.backgroundColor.get(f);
            int n2 = textRenderState.textOpacity.get(f);
            return new TextRenderState(this.getText(), this.getLineWidth(), new LinearIntInterpolator(n2, this.getTextOpacity()), new ColorInterpolator(n, this.getBackgroundColor()), this.getFlags());
        }

        public CachedInfo cacheDisplay(LineSplitter lineSplitter) {
            if (this.clientDisplayCache == null) {
                this.clientDisplayCache = this.textRenderState != null ? lineSplitter.split(this.textRenderState.text(), this.textRenderState.lineWidth()) : new CachedInfo(List.of(), 0);
            }
            return this.clientDisplayCache;
        }

        public static Align getAlign(byte by) {
            if ((by & 8) != 0) {
                return Align.LEFT;
            }
            if ((by & 0x10) != 0) {
                return Align.RIGHT;
            }
            return Align.CENTER;
        }

        public static enum Align implements StringRepresentable
        {
            CENTER("center"),
            LEFT("left"),
            RIGHT("right");

            public static final Codec<Align> CODEC;
            private final String name;

            private Align(String string2) {
                this.name = string2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Align::values);
            }
        }

        public static final class TextRenderState
        extends Record {
            private final Component text;
            private final int lineWidth;
            final IntInterpolator textOpacity;
            final IntInterpolator backgroundColor;
            private final byte flags;

            public TextRenderState(Component component, int n, IntInterpolator intInterpolator, IntInterpolator intInterpolator2, byte by) {
                this.text = component;
                this.lineWidth = n;
                this.textOpacity = intInterpolator;
                this.backgroundColor = intInterpolator2;
                this.flags = by;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this, object);
            }

            public Component text() {
                return this.text;
            }

            public int lineWidth() {
                return this.lineWidth;
            }

            public IntInterpolator textOpacity() {
                return this.textOpacity;
            }

            public IntInterpolator backgroundColor() {
                return this.backgroundColor;
            }

            public byte flags() {
                return this.flags;
            }
        }

        public record CachedInfo(List<CachedLine> lines, int width) {
        }

        @FunctionalInterface
        public static interface LineSplitter {
            public CachedInfo split(Component var1, int var2);
        }

        public record CachedLine(FormattedCharSequence contents, int width) {
        }
    }

    public static class BlockDisplay
    extends Display {
        public static final String TAG_BLOCK_STATE = "block_state";
        private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(BlockDisplay.class, EntityDataSerializers.BLOCK_STATE);
        @Nullable
        private BlockRenderState blockRenderState;

        public BlockDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            super.defineSynchedData(builder);
            builder.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
            super.onSyncedDataUpdated(entityDataAccessor);
            if (entityDataAccessor.equals(DATA_BLOCK_STATE_ID)) {
                this.updateRenderState = true;
            }
        }

        private BlockState getBlockState() {
            return this.entityData.get(DATA_BLOCK_STATE_ID);
        }

        private void setBlockState(BlockState blockState) {
            this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound(TAG_BLOCK_STATE)));
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.put(TAG_BLOCK_STATE, NbtUtils.writeBlockState(this.getBlockState()));
        }

        @Nullable
        public BlockRenderState blockRenderState() {
            return this.blockRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean bl, float f) {
            this.blockRenderState = new BlockRenderState(this.getBlockState());
        }

        public record BlockRenderState(BlockState blockState) {
        }
    }

    public static class ItemDisplay
    extends Display {
        private static final String TAG_ITEM = "item";
        private static final String TAG_ITEM_DISPLAY = "item_display";
        private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.ITEM_STACK);
        private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.BYTE);
        private final SlotAccess slot = SlotAccess.of(this::getItemStack, this::setItemStack);
        @Nullable
        private ItemRenderState itemRenderState;

        public ItemDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            super.defineSynchedData(builder);
            builder.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
            builder.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
            super.onSyncedDataUpdated(entityDataAccessor);
            if (DATA_ITEM_STACK_ID.equals(entityDataAccessor) || DATA_ITEM_DISPLAY_ID.equals(entityDataAccessor)) {
                this.updateRenderState = true;
            }
        }

        private ItemStack getItemStack() {
            return this.entityData.get(DATA_ITEM_STACK_ID);
        }

        private void setItemStack(ItemStack itemStack) {
            this.entityData.set(DATA_ITEM_STACK_ID, itemStack);
        }

        private void setItemTransform(ItemDisplayContext itemDisplayContext) {
            this.entityData.set(DATA_ITEM_DISPLAY_ID, itemDisplayContext.getId());
        }

        private ItemDisplayContext getItemTransform() {
            return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID).byteValue());
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            if (compoundTag.contains(TAG_ITEM)) {
                this.setItemStack(ItemStack.parse(this.registryAccess(), compoundTag.getCompound(TAG_ITEM)).orElse(ItemStack.EMPTY));
            } else {
                this.setItemStack(ItemStack.EMPTY);
            }
            if (compoundTag.contains(TAG_ITEM_DISPLAY, 8)) {
                ItemDisplayContext.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(TAG_ITEM_DISPLAY)).resultOrPartial(Util.prefix("Display entity", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(pair -> this.setItemTransform((ItemDisplayContext)pair.getFirst()));
            }
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            if (!this.getItemStack().isEmpty()) {
                compoundTag.put(TAG_ITEM, this.getItemStack().save(this.registryAccess()));
            }
            ItemDisplayContext.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.getItemTransform()).ifSuccess(tag -> compoundTag.put(TAG_ITEM_DISPLAY, (Tag)tag));
        }

        @Override
        public SlotAccess getSlot(int n) {
            if (n == 0) {
                return this.slot;
            }
            return SlotAccess.NULL;
        }

        @Nullable
        public ItemRenderState itemRenderState() {
            return this.itemRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean bl, float f) {
            ItemStack itemStack = this.getItemStack();
            itemStack.setEntityRepresentation(this);
            this.itemRenderState = new ItemRenderState(itemStack, this.getItemTransform());
        }

        public record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
        }
    }
}

