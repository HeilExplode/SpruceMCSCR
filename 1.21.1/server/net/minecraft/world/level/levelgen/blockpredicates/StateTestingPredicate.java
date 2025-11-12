/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public abstract class StateTestingPredicate
implements BlockPredicate {
    protected final Vec3i offset;

    protected static <P extends StateTestingPredicate> Products.P1<RecordCodecBuilder.Mu<P>, Vec3i> stateTestingCodec(RecordCodecBuilder.Instance<P> instance) {
        return instance.group((App)Vec3i.offsetCodec(16).optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(stateTestingPredicate -> stateTestingPredicate.offset));
    }

    protected StateTestingPredicate(Vec3i vec3i) {
        this.offset = vec3i;
    }

    @Override
    public final boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return this.test(worldGenLevel.getBlockState(blockPos.offset(this.offset)));
    }

    protected abstract boolean test(BlockState var1);

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}

