/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntIterator
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class CappedProcessor
extends StructureProcessor {
    public static final MapCodec<CappedProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter(cappedProcessor -> cappedProcessor.delegate), (App)IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter(cappedProcessor -> cappedProcessor.limit)).apply((Applicative)instance, CappedProcessor::new));
    private final StructureProcessor delegate;
    private final IntProvider limit;

    public CappedProcessor(StructureProcessor structureProcessor, IntProvider intProvider) {
        this.delegate = structureProcessor;
        this.limit = intProvider;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.CAPPED;
    }

    @Override
    public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list2, StructurePlaceSettings structurePlaceSettings) {
        if (this.limit.getMaxValue() == 0 || list2.isEmpty()) {
            return list2;
        }
        if (list.size() != list2.size()) {
            Util.logAndPauseIfInIde("Original block info list not in sync with processed list, skipping processing. Original size: " + list.size() + ", Processed size: " + list2.size());
            return list2;
        }
        RandomSource randomSource = RandomSource.create(serverLevelAccessor.getLevel().getSeed()).forkPositional().at(blockPos);
        int n = Math.min(this.limit.sample(randomSource), list2.size());
        if (n < 1) {
            return list2;
        }
        IntArrayList intArrayList = Util.toShuffledList(IntStream.range(0, list2.size()), randomSource);
        IntIterator intIterator = intArrayList.intIterator();
        int n2 = 0;
        while (intIterator.hasNext() && n2 < n) {
            StructureTemplate.StructureBlockInfo structureBlockInfo;
            int n3 = intIterator.nextInt();
            StructureTemplate.StructureBlockInfo structureBlockInfo2 = list.get(n3);
            StructureTemplate.StructureBlockInfo structureBlockInfo3 = this.delegate.processBlock(serverLevelAccessor, blockPos, blockPos2, structureBlockInfo2, structureBlockInfo = list2.get(n3), structurePlaceSettings);
            if (structureBlockInfo3 == null || structureBlockInfo.equals(structureBlockInfo3)) continue;
            ++n2;
            list2.set(n3, structureBlockInfo3);
        }
        return list2;
    }
}

