/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemPotionFix
extends DataFix {
    private static final int SPLASH = 16384;
    private static final String[] POTIONS = (String[])DataFixUtils.make((Object)new String[128], stringArray -> {
        stringArray[0] = DEFAULT;
        stringArray[1] = "minecraft:regeneration";
        stringArray[2] = "minecraft:swiftness";
        stringArray[3] = "minecraft:fire_resistance";
        stringArray[4] = "minecraft:poison";
        stringArray[5] = "minecraft:healing";
        stringArray[6] = "minecraft:night_vision";
        stringArray[7] = null;
        stringArray[8] = "minecraft:weakness";
        stringArray[9] = "minecraft:strength";
        stringArray[10] = "minecraft:slowness";
        stringArray[11] = "minecraft:leaping";
        stringArray[12] = "minecraft:harming";
        stringArray[13] = "minecraft:water_breathing";
        stringArray[14] = "minecraft:invisibility";
        stringArray[15] = null;
        stringArray[16] = "minecraft:awkward";
        stringArray[17] = "minecraft:regeneration";
        stringArray[18] = "minecraft:swiftness";
        stringArray[19] = "minecraft:fire_resistance";
        stringArray[20] = "minecraft:poison";
        stringArray[21] = "minecraft:healing";
        stringArray[22] = "minecraft:night_vision";
        stringArray[23] = null;
        stringArray[24] = "minecraft:weakness";
        stringArray[25] = "minecraft:strength";
        stringArray[26] = "minecraft:slowness";
        stringArray[27] = "minecraft:leaping";
        stringArray[28] = "minecraft:harming";
        stringArray[29] = "minecraft:water_breathing";
        stringArray[30] = "minecraft:invisibility";
        stringArray[31] = null;
        stringArray[32] = "minecraft:thick";
        stringArray[33] = "minecraft:strong_regeneration";
        stringArray[34] = "minecraft:strong_swiftness";
        stringArray[35] = "minecraft:fire_resistance";
        stringArray[36] = "minecraft:strong_poison";
        stringArray[37] = "minecraft:strong_healing";
        stringArray[38] = "minecraft:night_vision";
        stringArray[39] = null;
        stringArray[40] = "minecraft:weakness";
        stringArray[41] = "minecraft:strong_strength";
        stringArray[42] = "minecraft:slowness";
        stringArray[43] = "minecraft:strong_leaping";
        stringArray[44] = "minecraft:strong_harming";
        stringArray[45] = "minecraft:water_breathing";
        stringArray[46] = "minecraft:invisibility";
        stringArray[47] = null;
        stringArray[48] = null;
        stringArray[49] = "minecraft:strong_regeneration";
        stringArray[50] = "minecraft:strong_swiftness";
        stringArray[51] = "minecraft:fire_resistance";
        stringArray[52] = "minecraft:strong_poison";
        stringArray[53] = "minecraft:strong_healing";
        stringArray[54] = "minecraft:night_vision";
        stringArray[55] = null;
        stringArray[56] = "minecraft:weakness";
        stringArray[57] = "minecraft:strong_strength";
        stringArray[58] = "minecraft:slowness";
        stringArray[59] = "minecraft:strong_leaping";
        stringArray[60] = "minecraft:strong_harming";
        stringArray[61] = "minecraft:water_breathing";
        stringArray[62] = "minecraft:invisibility";
        stringArray[63] = null;
        stringArray[64] = "minecraft:mundane";
        stringArray[65] = "minecraft:long_regeneration";
        stringArray[66] = "minecraft:long_swiftness";
        stringArray[67] = "minecraft:long_fire_resistance";
        stringArray[68] = "minecraft:long_poison";
        stringArray[69] = "minecraft:healing";
        stringArray[70] = "minecraft:long_night_vision";
        stringArray[71] = null;
        stringArray[72] = "minecraft:long_weakness";
        stringArray[73] = "minecraft:long_strength";
        stringArray[74] = "minecraft:long_slowness";
        stringArray[75] = "minecraft:long_leaping";
        stringArray[76] = "minecraft:harming";
        stringArray[77] = "minecraft:long_water_breathing";
        stringArray[78] = "minecraft:long_invisibility";
        stringArray[79] = null;
        stringArray[80] = "minecraft:awkward";
        stringArray[81] = "minecraft:long_regeneration";
        stringArray[82] = "minecraft:long_swiftness";
        stringArray[83] = "minecraft:long_fire_resistance";
        stringArray[84] = "minecraft:long_poison";
        stringArray[85] = "minecraft:healing";
        stringArray[86] = "minecraft:long_night_vision";
        stringArray[87] = null;
        stringArray[88] = "minecraft:long_weakness";
        stringArray[89] = "minecraft:long_strength";
        stringArray[90] = "minecraft:long_slowness";
        stringArray[91] = "minecraft:long_leaping";
        stringArray[92] = "minecraft:harming";
        stringArray[93] = "minecraft:long_water_breathing";
        stringArray[94] = "minecraft:long_invisibility";
        stringArray[95] = null;
        stringArray[96] = "minecraft:thick";
        stringArray[97] = "minecraft:regeneration";
        stringArray[98] = "minecraft:swiftness";
        stringArray[99] = "minecraft:long_fire_resistance";
        stringArray[100] = "minecraft:poison";
        stringArray[101] = "minecraft:strong_healing";
        stringArray[102] = "minecraft:long_night_vision";
        stringArray[103] = null;
        stringArray[104] = "minecraft:long_weakness";
        stringArray[105] = "minecraft:strength";
        stringArray[106] = "minecraft:long_slowness";
        stringArray[107] = "minecraft:leaping";
        stringArray[108] = "minecraft:strong_harming";
        stringArray[109] = "minecraft:long_water_breathing";
        stringArray[110] = "minecraft:long_invisibility";
        stringArray[111] = null;
        stringArray[112] = null;
        stringArray[113] = "minecraft:regeneration";
        stringArray[114] = "minecraft:swiftness";
        stringArray[115] = "minecraft:long_fire_resistance";
        stringArray[116] = "minecraft:poison";
        stringArray[117] = "minecraft:strong_healing";
        stringArray[118] = "minecraft:long_night_vision";
        stringArray[119] = null;
        stringArray[120] = "minecraft:long_weakness";
        stringArray[121] = "minecraft:strength";
        stringArray[122] = "minecraft:long_slowness";
        stringArray[123] = "minecraft:leaping";
        stringArray[124] = "minecraft:strong_harming";
        stringArray[125] = "minecraft:long_water_breathing";
        stringArray[126] = "minecraft:long_invisibility";
        stringArray[127] = null;
    });
    public static final String DEFAULT = "minecraft:water";

    public ItemPotionFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemPotionFix", type, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:potion")) {
                Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
                Optional optional2 = typed.getOptionalTyped(opticFinder2);
                short s = dynamic.get("Damage").asShort((short)0);
                if (optional2.isPresent()) {
                    Typed typed2 = typed;
                    Dynamic dynamic2 = (Dynamic)((Typed)optional2.get()).get(DSL.remainderFinder());
                    Optional optional3 = dynamic2.get("Potion").asString().result();
                    if (optional3.isEmpty()) {
                        String string = POTIONS[s & 0x7F];
                        Typed typed3 = ((Typed)optional2.get()).set(DSL.remainderFinder(), (Object)dynamic2.set("Potion", dynamic2.createString(string == null ? DEFAULT : string)));
                        typed2 = typed2.set(opticFinder2, typed3);
                        if ((s & 0x4000) == 16384) {
                            typed2 = typed2.set(opticFinder, (Object)Pair.of((Object)References.ITEM_NAME.typeName(), (Object)"minecraft:splash_potion"));
                        }
                    }
                    if (s != 0) {
                        dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
                    }
                    return typed2.set(DSL.remainderFinder(), (Object)dynamic);
                }
            }
            return typed;
        });
    }
}

