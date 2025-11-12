/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final boolean fire;
    private final BlockInteraction blockInteraction;
    private final RandomSource random = RandomSource.create();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final Holder<SoundEvent> explosionSound;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

    public static DamageSource getDefaultDamageSource(Level level, @Nullable Entity entity) {
        return level.damageSources().explosion(entity, Explosion.getIndirectSourceEntityInternal(entity));
    }

    public Explosion(Level level, @Nullable Entity entity, double d, double d2, double d3, float f, List<BlockPos> list, BlockInteraction blockInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        this(level, entity, Explosion.getDefaultDamageSource(level, entity), null, d, d2, d3, f, false, blockInteraction, particleOptions, particleOptions2, holder);
        this.toBlow.addAll(list);
    }

    public Explosion(Level level, @Nullable Entity entity, double d, double d2, double d3, float f, boolean bl, BlockInteraction blockInteraction, List<BlockPos> list) {
        this(level, entity, d, d2, d3, f, bl, blockInteraction);
        this.toBlow.addAll(list);
    }

    public Explosion(Level level, @Nullable Entity entity, double d, double d2, double d3, float f, boolean bl, BlockInteraction blockInteraction) {
        this(level, entity, Explosion.getDefaultDamageSource(level, entity), null, d, d2, d3, f, bl, blockInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    public Explosion(Level level, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, BlockInteraction blockInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        this.level = level;
        this.source = entity;
        this.radius = f;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.fire = bl;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? level.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
        this.smallExplosionParticles = particleOptions;
        this.largeExplosionParticles = particleOptions2;
        this.explosionSound = holder;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
        double d2 = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
        double d3 = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
        double d4 = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double d5 = (1.0 - Math.floor(1.0 / d3) * d3) / 2.0;
        if (d < 0.0 || d2 < 0.0 || d3 < 0.0) {
            return 0.0f;
        }
        int n = 0;
        int n2 = 0;
        for (double d6 = 0.0; d6 <= 1.0; d6 += d) {
            for (double d7 = 0.0; d7 <= 1.0; d7 += d2) {
                for (double d8 = 0.0; d8 <= 1.0; d8 += d3) {
                    double d9 = Mth.lerp(d6, aABB.minX, aABB.maxX);
                    double d10 = Mth.lerp(d7, aABB.minY, aABB.maxY);
                    double d11 = Mth.lerp(d8, aABB.minZ, aABB.maxZ);
                    Vec3 vec32 = new Vec3(d9 + d4, d10, d11 + d5);
                    if (entity.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++n;
                    }
                    ++n2;
                }
            }
        }
        return (float)n / (float)n2;
    }

    public float radius() {
        return this.radius;
    }

    public Vec3 center() {
        return new Vec3(this.x, this.y, this.z);
    }

    public void explode() {
        int n;
        int n2;
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        HashSet hashSet = Sets.newHashSet();
        int n3 = 16;
        for (int i = 0; i < 16; ++i) {
            for (n2 = 0; n2 < 16; ++n2) {
                block2: for (n = 0; n < 16; ++n) {
                    if (i != 0 && i != 15 && n2 != 0 && n2 != 15 && n != 0 && n != 15) continue;
                    double d = (float)i / 15.0f * 2.0f - 1.0f;
                    double d2 = (float)n2 / 15.0f * 2.0f - 1.0f;
                    double d3 = (float)n / 15.0f * 2.0f - 1.0f;
                    double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
                    d /= d4;
                    d2 /= d4;
                    d3 /= d4;
                    double d5 = this.x;
                    double d6 = this.y;
                    double d7 = this.z;
                    float f = 0.3f;
                    for (float f2 = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); f2 > 0.0f; f2 -= 0.22500001f) {
                        BlockPos blockPos = BlockPos.containing(d5, d6, d7);
                        BlockState blockState = this.level.getBlockState(blockPos);
                        FluidState fluidState = this.level.getFluidState(blockPos);
                        if (!this.level.isInWorldBounds(blockPos)) continue block2;
                        Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
                        if (optional.isPresent()) {
                            f2 -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (f2 > 0.0f && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, f2)) {
                            hashSet.add(blockPos);
                        }
                        d5 += d * (double)0.3f;
                        d6 += d2 * (double)0.3f;
                        d7 += d3 * (double)0.3f;
                    }
                }
            }
        }
        this.toBlow.addAll((Collection)hashSet);
        float f = this.radius * 2.0f;
        n2 = Mth.floor(this.x - (double)f - 1.0);
        n = Mth.floor(this.x + (double)f + 1.0);
        int n4 = Mth.floor(this.y - (double)f - 1.0);
        int n5 = Mth.floor(this.y + (double)f + 1.0);
        int n6 = Mth.floor(this.z - (double)f - 1.0);
        int n7 = Mth.floor(this.z + (double)f + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB(n2, n4, n6, n, n5, n7));
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);
        for (Entity entity : list) {
            Player player;
            double d;
            Object object;
            double d8;
            double d9;
            double d10;
            double d11;
            double d12;
            if (entity.ignoreExplosion(this) || !((d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f) <= 1.0) || (d11 = Math.sqrt((d10 = entity.getX() - this.x) * d10 + (d9 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y) * d9 + (d8 = entity.getZ() - this.z) * d8)) == 0.0) continue;
            d10 /= d11;
            d9 /= d11;
            d8 /= d11;
            if (this.damageCalculator.shouldDamageEntity(this, entity)) {
                entity.hurt(this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity));
            }
            double d13 = (1.0 - d12) * (double)Explosion.getSeenPercent(vec3, entity) * (double)this.damageCalculator.getKnockbackMultiplier(entity);
            if (entity instanceof LivingEntity) {
                object = (LivingEntity)entity;
                d = d13 * (1.0 - ((LivingEntity)object).getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                d = d13;
            }
            object = new Vec3(d10 *= d, d9 *= d, d8 *= d);
            entity.setDeltaMovement(entity.getDeltaMovement().add((Vec3)object));
            if (!(!(entity instanceof Player) || (player = (Player)entity).isSpectator() || player.isCreative() && player.getAbilities().flying)) {
                this.hitPlayers.put(player, (Vec3)object);
            }
            entity.onExplosionHit(this.source);
        }
    }

    public void finalizeExplosion(boolean bl) {
        Object object;
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, this.explosionSound.value(), SoundSource.BLOCKS, 4.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
        }
        boolean bl2 = this.interactsWithBlocks();
        if (bl) {
            object = this.radius < 2.0f || !bl2 ? this.smallExplosionParticles : this.largeExplosionParticles;
            this.level.addParticle((ParticleOptions)object, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }
        if (bl2) {
            this.level.getProfiler().push("explosion_blocks");
            object = new ArrayList();
            Util.shuffle(this.toBlow, this.level.random);
            for (BlockPos blockPos : this.toBlow) {
                this.level.getBlockState(blockPos).onExplosionHit(this.level, blockPos, this, (arg_0, arg_1) -> Explosion.lambda$finalizeExplosion$0((List)object, arg_0, arg_1));
            }
            Object object2 = object.iterator();
            while (object2.hasNext()) {
                BlockPos blockPos;
                blockPos = (Pair)object2.next();
                Block.popResource(this.level, (BlockPos)blockPos.getSecond(), (ItemStack)blockPos.getFirst());
            }
            this.level.getProfiler().pop();
        }
        if (this.fire) {
            for (Object object2 : this.toBlow) {
                if (this.random.nextInt(3) != 0 || !this.level.getBlockState((BlockPos)object2).isAir() || !this.level.getBlockState(((BlockPos)object2).below()).isSolidRender(this.level, ((BlockPos)object2).below())) continue;
                this.level.setBlockAndUpdate((BlockPos)object2, BaseFireBlock.getState(this.level, (BlockPos)object2));
            }
        }
    }

    private static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> list, ItemStack itemStack, BlockPos blockPos) {
        for (int i = 0; i < list.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = list.get(i);
            ItemStack itemStack2 = (ItemStack)pair.getFirst();
            if (!ItemEntity.areMergable(itemStack2, itemStack)) continue;
            list.set(i, (Pair<ItemStack, BlockPos>)Pair.of((Object)ItemEntity.merge(itemStack2, itemStack, 16), (Object)((BlockPos)pair.getSecond())));
            if (!itemStack.isEmpty()) continue;
            return;
        }
        list.add((Pair<ItemStack, BlockPos>)Pair.of((Object)itemStack, (Object)blockPos));
    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != BlockInteraction.KEEP;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    private static LivingEntity getIndirectSourceEntityInternal(@Nullable Entity entity) {
        Projectile projectile;
        Entity entity2;
        if (entity == null) {
            return null;
        }
        if (entity instanceof PrimedTnt) {
            PrimedTnt primedTnt = (PrimedTnt)entity;
            return primedTnt.getOwner();
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity;
        }
        if (entity instanceof Projectile && (entity2 = (projectile = (Projectile)entity).getOwner()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            return livingEntity;
        }
        return null;
    }

    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntityInternal(this.source);
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public Holder<SoundEvent> getExplosionSound() {
        return this.explosionSound;
    }

    public boolean canTriggerBlocks() {
        if (this.blockInteraction != BlockInteraction.TRIGGER_BLOCK || this.level.isClientSide()) {
            return false;
        }
        if (this.source != null && this.source.getType() == EntityType.BREEZE_WIND_CHARGE) {
            return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        return true;
    }

    private static /* synthetic */ void lambda$finalizeExplosion$0(List list, ItemStack itemStack, BlockPos blockPos) {
        Explosion.addOrAppendStack(list, itemStack, blockPos);
    }

    public static enum BlockInteraction {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY,
        TRIGGER_BLOCK;

    }
}

