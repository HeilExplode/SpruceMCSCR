/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalStructure
extends Structure {
    private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
    private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
    private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05f;
    private static final int MIN_Y_INDEX = 15;
    private final List<Setup> setups;
    public static final MapCodec<RuinedPortalStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RuinedPortalStructure.settingsCodec(instance), (App)ExtraCodecs.nonEmptyList(Setup.CODEC.listOf()).fieldOf("setups").forGetter(ruinedPortalStructure -> ruinedPortalStructure.setups)).apply((Applicative)instance, RuinedPortalStructure::new));

    public RuinedPortalStructure(Structure.StructureSettings structureSettings, List<Setup> list) {
        super(structureSettings);
        this.setups = list;
    }

    public RuinedPortalStructure(Structure.StructureSettings structureSettings, Setup setup) {
        this(structureSettings, List.of(setup));
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        RuinedPortalPiece.Properties properties = new RuinedPortalPiece.Properties();
        WorldgenRandom worldgenRandom = generationContext.random();
        Setup setup = null;
        if (this.setups.size() > 1) {
            float f = 0.0f;
            for (Setup object22 : this.setups) {
                f += object22.weight();
            }
            float f2 = worldgenRandom.nextFloat();
            for (Setup setup2 : this.setups) {
                if (!((f2 -= setup2.weight() / f) < 0.0f)) continue;
                setup = setup2;
                break;
            }
        } else {
            setup = this.setups.get(0);
        }
        if (setup == null) {
            throw new IllegalStateException();
        }
        Setup setup2 = setup;
        properties.airPocket = RuinedPortalStructure.sample(worldgenRandom, setup2.airPocketProbability());
        properties.mossiness = setup2.mossiness();
        properties.overgrown = setup2.overgrown();
        properties.vines = setup2.vines();
        properties.replaceWithBlackstone = setup2.replaceWithBlackstone();
        ResourceLocation resourceLocation = worldgenRandom.nextFloat() < 0.05f ? ResourceLocation.withDefaultNamespace(STRUCTURE_LOCATION_GIANT_PORTALS[worldgenRandom.nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]) : ResourceLocation.withDefaultNamespace(STRUCTURE_LOCATION_PORTALS[worldgenRandom.nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
        StructureTemplate structureTemplate = generationContext.structureTemplateManager().getOrCreate(resourceLocation);
        Rotation rotation = Util.getRandom(Rotation.values(), (RandomSource)worldgenRandom);
        Mirror mirror = worldgenRandom.nextFloat() < 0.5f ? Mirror.NONE : Mirror.FRONT_BACK;
        BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        RandomState randomState = generationContext.randomState();
        BlockPos blockPos2 = generationContext.chunkPos().getWorldPosition();
        BoundingBox boundingBox = structureTemplate.getBoundingBox(blockPos2, rotation, blockPos, mirror);
        BlockPos blockPos3 = boundingBox.getCenter();
        int n = chunkGenerator.getBaseHeight(blockPos3.getX(), blockPos3.getZ(), RuinedPortalPiece.getHeightMapType(setup2.placement()), levelHeightAccessor, randomState) - 1;
        int n2 = RuinedPortalStructure.findSuitableY(worldgenRandom, chunkGenerator, setup2.placement(), properties.airPocket, n, boundingBox.getYSpan(), boundingBox, levelHeightAccessor, randomState);
        BlockPos blockPos4 = new BlockPos(blockPos2.getX(), n2, blockPos2.getZ());
        return Optional.of(new Structure.GenerationStub(blockPos4, structurePiecesBuilder -> {
            if (setup2.canBeCold()) {
                properties.cold = RuinedPortalStructure.isCold(blockPos4, generationContext.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockPos4.getX()), QuartPos.fromBlock(blockPos4.getY()), QuartPos.fromBlock(blockPos4.getZ()), randomState.sampler()));
            }
            structurePiecesBuilder.addPiece(new RuinedPortalPiece(generationContext.structureTemplateManager(), blockPos4, setup2.placement(), properties, resourceLocation, structureTemplate, rotation, mirror, blockPos));
        }));
    }

    private static boolean sample(WorldgenRandom worldgenRandom, float f) {
        if (f == 0.0f) {
            return false;
        }
        if (f == 1.0f) {
            return true;
        }
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean isCold(BlockPos blockPos, Holder<Biome> holder) {
        return holder.value().coldEnoughToSnow(blockPos);
    }

    private static int findSuitableY(RandomSource randomSource, ChunkGenerator chunkGenerator, RuinedPortalPiece.VerticalPlacement verticalPlacement, boolean bl, int n, int n2, BoundingBox boundingBox, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int n3;
        int n4 = levelHeightAccessor.getMinBuildHeight() + 15;
        if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            var9_10 = bl ? Mth.randomBetweenInclusive(randomSource, 32, 100) : (randomSource.nextFloat() < 0.5f ? Mth.randomBetweenInclusive(randomSource, 27, 29) : Mth.randomBetweenInclusive(randomSource, 29, 100));
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            var11_11 = n - n2;
            var9_10 = RuinedPortalStructure.getRandomWithinInterval(randomSource, 70, var11_11);
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            var11_11 = n - n2;
            var9_10 = RuinedPortalStructure.getRandomWithinInterval(randomSource, n4, var11_11);
        } else {
            var9_10 = verticalPlacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED ? n - n2 + Mth.randomBetweenInclusive(randomSource, 2, 8) : n;
        }
        ImmutableList immutableList = ImmutableList.of((Object)new BlockPos(boundingBox.minX(), 0, boundingBox.minZ()), (Object)new BlockPos(boundingBox.maxX(), 0, boundingBox.minZ()), (Object)new BlockPos(boundingBox.minX(), 0, boundingBox.maxZ()), (Object)new BlockPos(boundingBox.maxX(), 0, boundingBox.maxZ()));
        List list = immutableList.stream().map(blockPos -> chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), levelHeightAccessor, randomState)).collect(Collectors.toList());
        Heightmap.Types types = verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        block0: for (n3 = var9_10; n3 > n4; --n3) {
            int n5 = 0;
            for (NoiseColumn noiseColumn : list) {
                BlockState blockState = noiseColumn.getBlock(n3);
                if (!types.isOpaque().test(blockState) || ++n5 != 3) continue;
                break block0;
            }
        }
        return n3;
    }

    private static int getRandomWithinInterval(RandomSource randomSource, int n, int n2) {
        if (n < n2) {
            return Mth.randomBetweenInclusive(randomSource, n, n2);
        }
        return n2;
    }

    @Override
    public StructureType<?> type() {
        return StructureType.RUINED_PORTAL;
    }

    public record Setup(RuinedPortalPiece.VerticalPlacement placement, float airPocketProbability, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean replaceWithBlackstone, float weight) {
        public static final Codec<Setup> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RuinedPortalPiece.VerticalPlacement.CODEC.fieldOf("placement").forGetter(Setup::placement), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("air_pocket_probability").forGetter(Setup::airPocketProbability), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("mossiness").forGetter(Setup::mossiness), (App)Codec.BOOL.fieldOf("overgrown").forGetter(Setup::overgrown), (App)Codec.BOOL.fieldOf("vines").forGetter(Setup::vines), (App)Codec.BOOL.fieldOf("can_be_cold").forGetter(Setup::canBeCold), (App)Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(Setup::replaceWithBlackstone), (App)ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(Setup::weight)).apply((Applicative)instance, Setup::new));
    }
}

