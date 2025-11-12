/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BlockListReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.output = packOutput;
        this.registries = completableFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("blocks.json");
        return this.registries.thenCompose(provider -> {
            JsonObject jsonObject = new JsonObject();
            RegistryOps registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            provider.lookupOrThrow(Registries.BLOCK).listElements().forEach(reference -> {
                JsonArray jsonArray;
                JsonObject jsonObject2;
                JsonObject jsonObject3 = new JsonObject();
                StateDefinition<Block, BlockState> stateDefinition = ((Block)reference.value()).getStateDefinition();
                if (!stateDefinition.getProperties().isEmpty()) {
                    jsonObject2 = new JsonObject();
                    for (Property property : stateDefinition.getProperties()) {
                        jsonArray = new JsonArray();
                        for (Object object : property.getPossibleValues()) {
                            jsonArray.add(Util.getPropertyName(property, object));
                        }
                        jsonObject2.add(property.getName(), (JsonElement)jsonArray);
                    }
                    jsonObject3.add("properties", (JsonElement)jsonObject2);
                }
                jsonObject2 = new JsonArray();
                for (BlockState blockState : stateDefinition.getPossibleStates()) {
                    jsonArray = new JsonObject();
                    JsonObject jsonObject4 = new JsonObject();
                    for (Property property : stateDefinition.getProperties()) {
                        jsonObject4.addProperty(property.getName(), Util.getPropertyName(property, blockState.getValue(property)));
                    }
                    if (jsonObject4.size() > 0) {
                        jsonArray.add("properties", (JsonElement)jsonObject4);
                    }
                    jsonArray.addProperty("id", (Number)Block.getId(blockState));
                    if (blockState == ((Block)reference.value()).defaultBlockState()) {
                        jsonArray.addProperty("default", Boolean.valueOf(true));
                    }
                    jsonObject2.add((JsonElement)jsonArray);
                }
                jsonObject3.add("states", (JsonElement)jsonObject2);
                Object object = reference.getRegisteredName();
                JsonElement jsonElement = (JsonElement)BlockTypes.CODEC.codec().encodeStart((DynamicOps)registryOps, (Object)((Block)reference.value())).getOrThrow(arg_0 -> BlockListReport.lambda$run$0((String)object, arg_0));
                jsonObject3.add("definition", jsonElement);
                jsonObject.add((String)object, (JsonElement)jsonObject3);
            });
            return DataProvider.saveStable(cachedOutput, (JsonElement)jsonObject, path);
        });
    }

    @Override
    public final String getName() {
        return "Block List";
    }

    private static /* synthetic */ AssertionError lambda$run$0(String string, String string2) {
        return new AssertionError((Object)("Failed to serialize block " + string + " (is type registered in BlockTypes?): " + string2));
    }
}

