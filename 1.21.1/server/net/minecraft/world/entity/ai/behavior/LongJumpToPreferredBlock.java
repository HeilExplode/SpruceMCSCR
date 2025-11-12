/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.level.block.Block;

public class LongJumpToPreferredBlock<E extends Mob>
extends LongJumpToRandomPos<E> {
    private final TagKey<Block> preferredBlockTag;
    private final float preferredBlocksChance;
    private final List<LongJumpToRandomPos.PossibleJump> notPrefferedJumpCandidates = new ArrayList<LongJumpToRandomPos.PossibleJump>();
    private boolean currentlyWantingPreferredOnes;

    public LongJumpToPreferredBlock(UniformInt uniformInt, int n, int n2, float f, Function<E, SoundEvent> function, TagKey<Block> tagKey, float f2, BiPredicate<E, BlockPos> biPredicate) {
        super(uniformInt, n, n2, f, function, biPredicate);
        this.preferredBlockTag = tagKey;
        this.preferredBlocksChance = f2;
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        super.start(serverLevel, e, l);
        this.notPrefferedJumpCandidates.clear();
        this.currentlyWantingPreferredOnes = ((Entity)e).getRandom().nextFloat() < this.preferredBlocksChance;
    }

    @Override
    protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel serverLevel) {
        if (!this.currentlyWantingPreferredOnes) {
            return super.getJumpCandidate(serverLevel);
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        while (!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = super.getJumpCandidate(serverLevel);
            if (!optional.isPresent()) continue;
            LongJumpToRandomPos.PossibleJump possibleJump = optional.get();
            if (serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)possibleJump.getJumpTarget(), Direction.DOWN)).is(this.preferredBlockTag)) {
                return optional;
            }
            this.notPrefferedJumpCandidates.add(possibleJump);
        }
        if (!this.notPrefferedJumpCandidates.isEmpty()) {
            return Optional.of(this.notPrefferedJumpCandidates.remove(0));
        }
        return Optional.empty();
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Mob)livingEntity), l);
    }
}

