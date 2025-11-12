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
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemSpawnEggFix
extends DataFix {
    private static final String[] ID_TO_ENTITY = (String[])DataFixUtils.make((Object)new String[256], stringArray -> {
        stringArray[1] = "Item";
        stringArray[2] = "XPOrb";
        stringArray[7] = "ThrownEgg";
        stringArray[8] = "LeashKnot";
        stringArray[9] = "Painting";
        stringArray[10] = "Arrow";
        stringArray[11] = "Snowball";
        stringArray[12] = "Fireball";
        stringArray[13] = "SmallFireball";
        stringArray[14] = "ThrownEnderpearl";
        stringArray[15] = "EyeOfEnderSignal";
        stringArray[16] = "ThrownPotion";
        stringArray[17] = "ThrownExpBottle";
        stringArray[18] = "ItemFrame";
        stringArray[19] = "WitherSkull";
        stringArray[20] = "PrimedTnt";
        stringArray[21] = "FallingSand";
        stringArray[22] = "FireworksRocketEntity";
        stringArray[23] = "TippedArrow";
        stringArray[24] = "SpectralArrow";
        stringArray[25] = "ShulkerBullet";
        stringArray[26] = "DragonFireball";
        stringArray[30] = "ArmorStand";
        stringArray[41] = "Boat";
        stringArray[42] = "MinecartRideable";
        stringArray[43] = "MinecartChest";
        stringArray[44] = "MinecartFurnace";
        stringArray[45] = "MinecartTNT";
        stringArray[46] = "MinecartHopper";
        stringArray[47] = "MinecartSpawner";
        stringArray[40] = "MinecartCommandBlock";
        stringArray[50] = "Creeper";
        stringArray[51] = "Skeleton";
        stringArray[52] = "Spider";
        stringArray[53] = "Giant";
        stringArray[54] = "Zombie";
        stringArray[55] = "Slime";
        stringArray[56] = "Ghast";
        stringArray[57] = "PigZombie";
        stringArray[58] = "Enderman";
        stringArray[59] = "CaveSpider";
        stringArray[60] = "Silverfish";
        stringArray[61] = "Blaze";
        stringArray[62] = "LavaSlime";
        stringArray[63] = "EnderDragon";
        stringArray[64] = "WitherBoss";
        stringArray[65] = "Bat";
        stringArray[66] = "Witch";
        stringArray[67] = "Endermite";
        stringArray[68] = "Guardian";
        stringArray[69] = "Shulker";
        stringArray[90] = "Pig";
        stringArray[91] = "Sheep";
        stringArray[92] = "Cow";
        stringArray[93] = "Chicken";
        stringArray[94] = "Squid";
        stringArray[95] = "Wolf";
        stringArray[96] = "MushroomCow";
        stringArray[97] = "SnowMan";
        stringArray[98] = "Ozelot";
        stringArray[99] = "VillagerGolem";
        stringArray[100] = "EntityHorse";
        stringArray[101] = "Rabbit";
        stringArray[120] = "Villager";
        stringArray[200] = "EnderCrystal";
    });

    public ItemSpawnEggFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = schema.getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"id", (Type)DSL.string());
        OpticFinder opticFinder3 = type.findField("tag");
        OpticFinder opticFinder4 = opticFinder3.type().findField("EntityTag");
        OpticFinder opticFinder5 = DSL.typeFinder((Type)schema.getTypeRaw(References.ENTITY));
        Type type2 = this.getOutputSchema().getTypeRaw(References.ENTITY);
        return this.fixTypeEverywhereTyped("ItemSpawnEggFix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:spawn_egg")) {
                Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
                short s = dynamic.get("Damage").asShort((short)0);
                Optional optional2 = typed2.getOptionalTyped(opticFinder3);
                Optional optional3 = optional2.flatMap(typed -> typed.getOptionalTyped(opticFinder4));
                Optional optional4 = optional3.flatMap(typed -> typed.getOptionalTyped(opticFinder5));
                Optional optional5 = optional4.flatMap(typed -> typed.getOptional(opticFinder2));
                Typed typed3 = typed2;
                String string = ID_TO_ENTITY[s & 0xFF];
                if (string != null && (optional5.isEmpty() || !Objects.equals(optional5.get(), string))) {
                    Typed typed4 = typed2.getOrCreateTyped(opticFinder3);
                    Typed typed5 = typed4.getOrCreateTyped(opticFinder4);
                    Typed typed6 = typed5.getOrCreateTyped(opticFinder5);
                    Dynamic dynamic3 = dynamic;
                    Typed typed7 = Util.writeAndReadTypedOrThrow(typed6, type2, dynamic2 -> dynamic2.set("id", dynamic3.createString(string)));
                    typed3 = typed3.set(opticFinder3, typed4.set(opticFinder4, typed5.set(opticFinder5, typed7)));
                }
                if (s != 0) {
                    dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
                    typed3 = typed3.set(DSL.remainderFinder(), (Object)dynamic);
                }
                return typed3;
            }
            return typed2;
        });
    }
}

