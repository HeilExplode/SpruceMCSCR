/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetNameFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetNameFunction.commonFields(instance).and(instance.group((App)ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(setNameFunction -> setNameFunction.name), (App)LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(setNameFunction -> setNameFunction.resolutionContext), (App)Target.CODEC.optionalFieldOf("target", (Object)Target.CUSTOM_NAME).forGetter(setNameFunction -> setNameFunction.target))).apply((Applicative)instance, SetNameFunction::new));
    private final Optional<Component> name;
    private final Optional<LootContext.EntityTarget> resolutionContext;
    private final Target target;

    private SetNameFunction(List<LootItemCondition> list, Optional<Component> optional, Optional<LootContext.EntityTarget> optional2, Target target) {
        super(list);
        this.name = optional;
        this.resolutionContext = optional2;
        this.target = target;
    }

    public LootItemFunctionType<SetNameFunction> getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.map(entityTarget -> Set.of(entityTarget.getParam())).orElse(Set.of());
    }

    public static UnaryOperator<Component> createResolver(LootContext lootContext, @Nullable LootContext.EntityTarget entityTarget) {
        Entity entity;
        if (entityTarget != null && (entity = lootContext.getParamOrNull(entityTarget.getParam())) != null) {
            CommandSourceStack commandSourceStack = entity.createCommandSourceStack().withPermission(2);
            return component -> {
                try {
                    return ComponentUtils.updateForEntity(commandSourceStack, component, entity, 0);
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to resolve text component", (Throwable)commandSyntaxException);
                    return component;
                }
            };
        }
        return component -> component;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        this.name.ifPresent(component -> itemStack.set(this.target.component(), (Component)SetNameFunction.createResolver(lootContext, this.resolutionContext.orElse(null)).apply((Component)component)));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component component, Target target) {
        return SetNameFunction.simpleBuilder(list -> new SetNameFunction((List<LootItemCondition>)list, Optional.of(component), Optional.empty(), target));
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component component, Target target, LootContext.EntityTarget entityTarget) {
        return SetNameFunction.simpleBuilder(list -> new SetNameFunction((List<LootItemCondition>)list, Optional.of(component), Optional.of(entityTarget), target));
    }

    public static enum Target implements StringRepresentable
    {
        CUSTOM_NAME("custom_name"),
        ITEM_NAME("item_name");

        public static final Codec<Target> CODEC;
        private final String name;

        private Target(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public DataComponentType<Component> component() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 1 -> DataComponents.ITEM_NAME;
                case 0 -> DataComponents.CUSTOM_NAME;
            };
        }

        static {
            CODEC = StringRepresentable.fromEnum(Target::values);
        }
    }
}

