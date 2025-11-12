/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityZombieVillagerTypeFix
extends NamedEntityFix {
    private static final int PROFESSION_MAX = 6;

    public EntityZombieVillagerTypeFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityZombieVillagerTypeFix", References.ENTITY, "Zombie");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        if (dynamic.get("IsVillager").asBoolean(false)) {
            if (dynamic.get("ZombieType").result().isEmpty()) {
                int n = this.getVillagerProfession(dynamic.get("VillagerProfession").asInt(-1));
                if (n == -1) {
                    n = this.getVillagerProfession(RandomSource.create().nextInt(6));
                }
                dynamic = dynamic.set("ZombieType", dynamic.createInt(n));
            }
            dynamic = dynamic.remove("IsVillager");
        }
        return dynamic;
    }

    private int getVillagerProfession(int n) {
        if (n < 0 || n >= 6) {
            return -1;
        }
        return n;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}

