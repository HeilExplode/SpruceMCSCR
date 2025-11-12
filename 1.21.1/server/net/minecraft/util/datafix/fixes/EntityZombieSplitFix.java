/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityZombieSplitFix
extends EntityRenameFix {
    private final Supplier<Type<?>> zombieVillagerType = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(References.ENTITY, "ZombieVillager"));

    public EntityZombieSplitFix(Schema schema) {
        super("EntityZombieSplitFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
        String string2;
        if (!string.equals("Zombie")) {
            return Pair.of((Object)string, typed);
        }
        Dynamic dynamic2 = (Dynamic)typed.getOptional(DSL.remainderFinder()).orElseThrow();
        int n = dynamic2.get("ZombieType").asInt(0);
        return Pair.of((Object)string2, (Object)(switch (n) {
            default -> {
                string2 = "Zombie";
                yield typed;
            }
            case 1, 2, 3, 4, 5 -> {
                string2 = "ZombieVillager";
                yield this.changeSchemaToZombieVillager(typed, n - 1);
            }
            case 6 -> {
                string2 = "Husk";
                yield typed;
            }
        }).update(DSL.remainderFinder(), dynamic -> dynamic.remove("ZombieType")));
    }

    private Typed<?> changeSchemaToZombieVillager(Typed<?> typed, int n) {
        return Util.writeAndReadTypedOrThrow(typed, this.zombieVillagerType.get(), dynamic -> dynamic.set("Profession", dynamic.createInt(n)));
    }
}

