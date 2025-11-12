/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix
extends DataFix {
    private final String name;

    public BlockRenameFix(Schema schema, String string) {
        super(schema, false);
        this.name = string;
    }

    public TypeRewriteRule makeRule() {
        Type type;
        Type type2 = this.getInputSchema().getType(References.BLOCK_NAME);
        if (!Objects.equals(type2, type = DSL.named((String)References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString()))) {
            throw new IllegalStateException("block type is not what was expected.");
        }
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhere(this.name + " for block", type, dynamicOps -> pair -> pair.mapSecond(this::renameBlock));
        TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::fixBlockState));
        TypeRewriteRule typeRewriteRule3 = this.fixTypeEverywhereTyped(this.name + " for flat_block_state", this.getInputSchema().getType(References.FLAT_BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asString().result().map(this::fixFlatBlockState).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)), (Object)dynamic)));
        return TypeRewriteRule.seq((TypeRewriteRule)typeRewriteRule, (TypeRewriteRule[])new TypeRewriteRule[]{typeRewriteRule2, typeRewriteRule3});
    }

    private Dynamic<?> fixBlockState(Dynamic<?> dynamic) {
        Optional optional = dynamic.get("Name").asString().result();
        if (optional.isPresent()) {
            return dynamic.set("Name", dynamic.createString(this.renameBlock((String)optional.get())));
        }
        return dynamic;
    }

    private String fixFlatBlockState(String string) {
        int n = string.indexOf(91);
        int n2 = string.indexOf(123);
        int n3 = string.length();
        if (n > 0) {
            n3 = n;
        }
        if (n2 > 0) {
            n3 = Math.min(n3, n2);
        }
        String string2 = string.substring(0, n3);
        String string3 = this.renameBlock(string2);
        return string3 + string.substring(n3);
    }

    protected abstract String renameBlock(String var1);

    public static DataFix create(Schema schema, String string, final Function<String, String> function) {
        return new BlockRenameFix(schema, string){

            @Override
            protected String renameBlock(String string) {
                return (String)function.apply(string);
            }
        };
    }
}

