/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List<StructureSelectionEntry> structures, StructurePlacement placement) {
    public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)StructureSelectionEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures), (App)StructurePlacement.CODEC.fieldOf("placement").forGetter(StructureSet::placement)).apply((Applicative)instance, StructureSet::new));
    public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE_SET, DIRECT_CODEC);

    public StructureSet(Holder<Structure> holder, StructurePlacement structurePlacement) {
        this(List.of(new StructureSelectionEntry(holder, 1)), structurePlacement);
    }

    public static StructureSelectionEntry entry(Holder<Structure> holder, int n) {
        return new StructureSelectionEntry(holder, n);
    }

    public static StructureSelectionEntry entry(Holder<Structure> holder) {
        return new StructureSelectionEntry(holder, 1);
    }

    public record StructureSelectionEntry(Holder<Structure> structure, int weight) {
        public static final Codec<StructureSelectionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Structure.CODEC.fieldOf("structure").forGetter(StructureSelectionEntry::structure), (App)ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(StructureSelectionEntry::weight)).apply((Applicative)instance, StructureSelectionEntry::new));
    }
}

