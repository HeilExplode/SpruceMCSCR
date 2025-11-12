/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public record RunFunction(ResourceLocation function) implements EnchantmentEntityEffect
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<RunFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("function").forGetter(RunFunction::function)).apply((Applicative)instance, RunFunction::new));

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        MinecraftServer minecraftServer = serverLevel.getServer();
        ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
        Optional<CommandFunction<CommandSourceStack>> optional = serverFunctionManager.get(this.function);
        if (optional.isPresent()) {
            CommandSourceStack commandSourceStack = minecraftServer.createCommandSourceStack().withPermission(2).withSuppressedOutput().withEntity(entity).withLevel(serverLevel).withPosition(vec3).withRotation(entity.getRotationVector());
            serverFunctionManager.execute(optional.get(), commandSourceStack);
        } else {
            LOGGER.error("Enchantment run_function effect failed for non-existent function {}", (Object)this.function);
        }
    }

    public MapCodec<RunFunction> codec() {
        return CODEC;
    }
}

