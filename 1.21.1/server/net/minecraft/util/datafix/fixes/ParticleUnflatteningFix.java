/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class ParticleUnflatteningFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ParticleUnflatteningFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.PARTICLE);
        Type type2 = this.getOutputSchema().getType(References.PARTICLE);
        return this.writeFixAndRead("ParticleUnflatteningFix", type, type2, this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        String string = (String)optional.get();
        String[] stringArray = string.split(" ", 2);
        String string2 = NamespacedSchema.ensureNamespaced(stringArray[0]);
        Dynamic<T> dynamic2 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString(string2)));
        return switch (string2) {
            case "minecraft:item" -> {
                if (stringArray.length > 1) {
                    yield this.updateItem(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust", "minecraft:dust_pillar" -> {
                if (stringArray.length > 1) {
                    yield this.updateBlock(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:dust" -> {
                if (stringArray.length > 1) {
                    yield this.updateDust(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:dust_color_transition" -> {
                if (stringArray.length > 1) {
                    yield this.updateDustTransition(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:sculk_charge" -> {
                if (stringArray.length > 1) {
                    yield this.updateSculkCharge(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:vibration" -> {
                if (stringArray.length > 1) {
                    yield this.updateVibration(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            case "minecraft:shriek" -> {
                if (stringArray.length > 1) {
                    yield this.updateShriek(dynamic2, stringArray[1]);
                }
                yield dynamic2;
            }
            default -> dynamic2;
        };
    }

    private <T> Dynamic<T> updateItem(Dynamic<T> dynamic, String string) {
        int n = string.indexOf("{");
        Dynamic dynamic2 = dynamic.createMap(Map.of(dynamic.createString("Count"), dynamic.createInt(1)));
        if (n == -1) {
            dynamic2 = dynamic2.set("id", dynamic.createString(string));
        } else {
            dynamic2 = dynamic2.set("id", dynamic.createString(string.substring(0, n)));
            CompoundTag compoundTag = ParticleUnflatteningFix.parseTag(string.substring(n));
            if (compoundTag != null) {
                dynamic2 = dynamic2.set("tag", new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag).convert(dynamic.getOps()));
            }
        }
        return dynamic.set("item", dynamic2);
    }

    @Nullable
    private static CompoundTag parseTag(String string) {
        try {
            return TagParser.parseTag(string);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse tag: {}", (Object)string, (Object)exception);
            return null;
        }
    }

    private <T> Dynamic<T> updateBlock(Dynamic<T> dynamic, String string) {
        int n = string.indexOf("[");
        Dynamic dynamic2 = dynamic.emptyMap();
        if (n == -1) {
            dynamic2 = dynamic2.set("Name", dynamic.createString(NamespacedSchema.ensureNamespaced(string)));
        } else {
            dynamic2 = dynamic2.set("Name", dynamic.createString(NamespacedSchema.ensureNamespaced(string.substring(0, n))));
            Map<Dynamic<T>, Dynamic<T>> map = ParticleUnflatteningFix.parseBlockProperties(dynamic, string.substring(n));
            if (!map.isEmpty()) {
                dynamic2 = dynamic2.set("Properties", dynamic.createMap(map));
            }
        }
        return dynamic.set("block_state", dynamic2);
    }

    private static <T> Map<Dynamic<T>, Dynamic<T>> parseBlockProperties(Dynamic<T> dynamic, String string) {
        try {
            HashMap<Dynamic<T>, Dynamic<T>> hashMap = new HashMap<Dynamic<T>, Dynamic<T>>();
            StringReader stringReader = new StringReader(string);
            stringReader.expect('[');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != ']') {
                stringReader.skipWhitespace();
                String string2 = stringReader.readString();
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                String string3 = stringReader.readString();
                stringReader.skipWhitespace();
                hashMap.put(dynamic.createString(string2), dynamic.createString(string3));
                if (!stringReader.canRead()) continue;
                if (stringReader.peek() != ',') break;
                stringReader.skip();
            }
            stringReader.expect(']');
            return hashMap;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse block properties: {}", (Object)string, (Object)exception);
            return Map.of();
        }
    }

    private static <T> Dynamic<T> readVector(Dynamic<T> dynamic, StringReader stringReader) throws CommandSyntaxException {
        float f = stringReader.readFloat();
        stringReader.expect(' ');
        float f2 = stringReader.readFloat();
        stringReader.expect(' ');
        float f3 = stringReader.readFloat();
        return dynamic.createList(Stream.of(Float.valueOf(f), Float.valueOf(f2), Float.valueOf(f3)).map(arg_0 -> dynamic.createFloat(arg_0)));
    }

    private <T> Dynamic<T> updateDust(Dynamic<T> dynamic, String string) {
        try {
            StringReader stringReader = new StringReader(string);
            Dynamic<T> dynamic2 = ParticleUnflatteningFix.readVector(dynamic, stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return dynamic.set("color", dynamic2).set("scale", dynamic.createFloat(f));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)string, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateDustTransition(Dynamic<T> dynamic, String string) {
        try {
            StringReader stringReader = new StringReader(string);
            Dynamic<T> dynamic2 = ParticleUnflatteningFix.readVector(dynamic, stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            stringReader.expect(' ');
            Dynamic<T> dynamic3 = ParticleUnflatteningFix.readVector(dynamic, stringReader);
            return dynamic.set("from_color", dynamic2).set("to_color", dynamic3).set("scale", dynamic.createFloat(f));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)string, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateSculkCharge(Dynamic<T> dynamic, String string) {
        try {
            StringReader stringReader = new StringReader(string);
            float f = stringReader.readFloat();
            return dynamic.set("roll", dynamic.createFloat(f));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)string, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateVibration(Dynamic<T> dynamic, String string) {
        try {
            StringReader stringReader = new StringReader(string);
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f2 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f3 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int n = stringReader.readInt();
            Dynamic dynamic2 = dynamic.createIntList(IntStream.of(Mth.floor(f), Mth.floor(f2), Mth.floor(f3)));
            Dynamic dynamic3 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString("minecraft:block"), dynamic.createString("pos"), dynamic2));
            return dynamic.set("destination", dynamic3).set("arrival_in_ticks", dynamic.createInt(n));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)string, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateShriek(Dynamic<T> dynamic, String string) {
        try {
            StringReader stringReader = new StringReader(string);
            int n = stringReader.readInt();
            return dynamic.set("delay", dynamic.createInt(n));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)string, (Object)exception);
            return dynamic;
        }
    }
}

