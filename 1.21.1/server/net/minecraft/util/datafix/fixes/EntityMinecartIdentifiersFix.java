/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityMinecartIdentifiersFix
extends EntityRenameFix {
    public EntityMinecartIdentifiersFix(Schema schema) {
        super("EntityMinecartIdentifiersFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
        if (!string.equals("Minecart")) {
            return Pair.of((Object)string, typed);
        }
        int n = ((Dynamic)typed.getOrCreate(DSL.remainderFinder())).get("Type").asInt(0);
        String string2 = switch (n) {
            default -> "MinecartRideable";
            case 1 -> "MinecartChest";
            case 2 -> "MinecartFurnace";
        };
        Type type = (Type)this.getOutputSchema().findChoiceType(References.ENTITY).types().get(string2);
        return Pair.of((Object)string2, Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.remove("Type")));
    }
}

