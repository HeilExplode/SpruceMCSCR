/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class TropicalFish
extends AbstractSchoolingFish
implements VariantHolder<Pattern> {
    public static final String BUCKET_VARIANT_TAG = "BucketVariantTag";
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
    public static final List<Variant> COMMON_VARIANTS = List.of(new Variant(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new Variant(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), new Variant(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new Variant(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new Variant(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), new Variant(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new Variant(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), new Variant(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new Variant(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
    private boolean isSchool = true;

    public TropicalFish(EntityType<? extends TropicalFish> entityType, Level level) {
        super((EntityType<? extends AbstractSchoolingFish>)entityType, level);
    }

    public static String getPredefinedName(int n) {
        return "entity.minecraft.tropical_fish.predefined." + n;
    }

    static int packVariant(Pattern pattern, DyeColor dyeColor, DyeColor dyeColor2) {
        return pattern.getPackedId() & 0xFFFF | (dyeColor.getId() & 0xFF) << 16 | (dyeColor2.getId() & 0xFF) << 24;
    }

    public static DyeColor getBaseColor(int n) {
        return DyeColor.byId(n >> 16 & 0xFF);
    }

    public static DyeColor getPatternColor(int n) {
        return DyeColor.byId(n >> 24 & 0xFF);
    }

    public static Pattern getPattern(int n) {
        return Pattern.byId(n & 0xFFFF);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", this.getPackedVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setPackedVariant(compoundTag.getInt("Variant"));
    }

    private void setPackedVariant(int n) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, n);
    }

    @Override
    public boolean isMaxGroupSizeReached(int n) {
        return !this.isSchool;
    }

    private int getPackedVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    public DyeColor getBaseColor() {
        return TropicalFish.getBaseColor(this.getPackedVariant());
    }

    public DyeColor getPatternColor() {
        return TropicalFish.getPatternColor(this.getPackedVariant());
    }

    @Override
    public Pattern getVariant() {
        return TropicalFish.getPattern(this.getPackedVariant());
    }

    @Override
    public void setVariant(Pattern pattern) {
        int n = this.getPackedVariant();
        DyeColor dyeColor = TropicalFish.getBaseColor(n);
        DyeColor dyeColor2 = TropicalFish.getPatternColor(n);
        this.setPackedVariant(TropicalFish.packVariant(pattern, dyeColor, dyeColor2));
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        super.saveToBucketTag(itemStack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> compoundTag.putInt(BUCKET_VARIANT_TAG, this.getPackedVariant()));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.TROPICAL_FISH_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.TROPICAL_FISH_FLOP;
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        super.loadFromBucketTag(compoundTag);
        if (compoundTag.contains(BUCKET_VARIANT_TAG, 3)) {
            this.setPackedVariant(compoundTag.getInt(BUCKET_VARIANT_TAG));
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        Variant variant;
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (spawnGroupData instanceof TropicalFishGroupData) {
            TropicalFishGroupData tropicalFishGroupData = (TropicalFishGroupData)spawnGroupData;
            variant = tropicalFishGroupData.variant;
        } else if ((double)randomSource.nextFloat() < 0.9) {
            variant = Util.getRandom(COMMON_VARIANTS, randomSource);
            spawnGroupData = new TropicalFishGroupData(this, variant);
        } else {
            this.isSchool = false;
            Pattern[] patternArray = Pattern.values();
            DyeColor[] dyeColorArray = DyeColor.values();
            Pattern pattern = Util.getRandom(patternArray, randomSource);
            DyeColor dyeColor = Util.getRandom(dyeColorArray, randomSource);
            DyeColor dyeColor2 = Util.getRandom(dyeColorArray, randomSource);
            variant = new Variant(pattern, dyeColor, dyeColor2);
        }
        this.setPackedVariant(variant.getPackedId());
        return spawnGroupData;
    }

    public static boolean checkTropicalFishSpawnRules(EntityType<TropicalFish> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getFluidState(blockPos.below()).is(FluidTags.WATER) && levelAccessor.getBlockState(blockPos.above()).is(Blocks.WATER) && (levelAccessor.getBiome(blockPos).is(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterAnimal.checkSurfaceWaterAnimalSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource));
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static enum Pattern implements StringRepresentable
    {
        KOB("kob", Base.SMALL, 0),
        SUNSTREAK("sunstreak", Base.SMALL, 1),
        SNOOPER("snooper", Base.SMALL, 2),
        DASHER("dasher", Base.SMALL, 3),
        BRINELY("brinely", Base.SMALL, 4),
        SPOTTY("spotty", Base.SMALL, 5),
        FLOPPER("flopper", Base.LARGE, 0),
        STRIPEY("stripey", Base.LARGE, 1),
        GLITTER("glitter", Base.LARGE, 2),
        BLOCKFISH("blockfish", Base.LARGE, 3),
        BETTY("betty", Base.LARGE, 4),
        CLAYFISH("clayfish", Base.LARGE, 5);

        public static final Codec<Pattern> CODEC;
        private static final IntFunction<Pattern> BY_ID;
        private final String name;
        private final Component displayName;
        private final Base base;
        private final int packedId;

        private Pattern(String string2, Base base, int n2) {
            this.name = string2;
            this.base = base;
            this.packedId = base.id | n2 << 8;
            this.displayName = Component.translatable("entity.minecraft.tropical_fish.type." + this.name);
        }

        public static Pattern byId(int n) {
            return BY_ID.apply(n);
        }

        public Base base() {
            return this.base;
        }

        public int getPackedId() {
            return this.packedId;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Component displayName() {
            return this.displayName;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Pattern::values);
            BY_ID = ByIdMap.sparse(Pattern::getPackedId, Pattern.values(), KOB);
        }
    }

    static class TropicalFishGroupData
    extends AbstractSchoolingFish.SchoolSpawnGroupData {
        final Variant variant;

        TropicalFishGroupData(TropicalFish tropicalFish, Variant variant) {
            super(tropicalFish);
            this.variant = variant;
        }
    }

    public record Variant(Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
        public static final Codec<Variant> CODEC = Codec.INT.xmap(Variant::new, Variant::getPackedId);

        public Variant(int n) {
            this(TropicalFish.getPattern(n), TropicalFish.getBaseColor(n), TropicalFish.getPatternColor(n));
        }

        public int getPackedId() {
            return TropicalFish.packVariant(this.pattern, this.baseColor, this.patternColor);
        }
    }

    public static enum Base {
        SMALL(0),
        LARGE(1);

        final int id;

        private Base(int n2) {
            this.id = n2;
        }
    }
}

