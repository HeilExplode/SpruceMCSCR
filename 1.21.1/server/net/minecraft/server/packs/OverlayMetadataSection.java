/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.packs;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayEntry> overlays) {
    private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
    private static final Codec<OverlayMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)OverlayEntry.CODEC.listOf().fieldOf("entries").forGetter(OverlayMetadataSection::overlays)).apply((Applicative)instance, OverlayMetadataSection::new));
    public static final MetadataSectionType<OverlayMetadataSection> TYPE = MetadataSectionType.fromCodec("overlays", CODEC);

    private static DataResult<String> validateOverlayDir(String string) {
        if (!DIR_VALIDATOR.matcher(string).matches()) {
            return DataResult.error(() -> string + " is not accepted directory name");
        }
        return DataResult.success((Object)string);
    }

    public List<String> overlaysForVersion(int n) {
        return this.overlays.stream().filter(overlayEntry -> overlayEntry.isApplicable(n)).map(OverlayEntry::overlay).toList();
    }

    public record OverlayEntry(InclusiveRange<Integer> format, String overlay) {
        static final Codec<OverlayEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)InclusiveRange.codec(Codec.INT).fieldOf("formats").forGetter(OverlayEntry::format), (App)Codec.STRING.validate(OverlayMetadataSection::validateOverlayDir).fieldOf("directory").forGetter(OverlayEntry::overlay)).apply((Applicative)instance, OverlayEntry::new));

        public boolean isApplicable(int n) {
            return this.format.isValueInRange(n);
        }
    }
}

