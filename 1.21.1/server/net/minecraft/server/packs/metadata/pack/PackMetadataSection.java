/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.packs.metadata.pack;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record PackMetadataSection(Component description, int packFormat, Optional<InclusiveRange<Integer>> supportedFormats) {
    public static final Codec<PackMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("description").forGetter(PackMetadataSection::description), (App)Codec.INT.fieldOf("pack_format").forGetter(PackMetadataSection::packFormat), (App)InclusiveRange.codec(Codec.INT).lenientOptionalFieldOf("supported_formats").forGetter(PackMetadataSection::supportedFormats)).apply((Applicative)instance, PackMetadataSection::new));
    public static final MetadataSectionType<PackMetadataSection> TYPE = MetadataSectionType.fromCodec("pack", CODEC);
}

