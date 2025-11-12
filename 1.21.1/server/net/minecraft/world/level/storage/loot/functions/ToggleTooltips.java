/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips
extends LootItemConditionalFunction {
    private static final Map<DataComponentType<?>, ComponentToggle<?>> TOGGLES = Stream.of(new ComponentToggle<ArmorTrim>(DataComponents.TRIM, ArmorTrim::withTooltip), new ComponentToggle<DyedItemColor>(DataComponents.DYED_COLOR, DyedItemColor::withTooltip), new ComponentToggle<ItemEnchantments>(DataComponents.ENCHANTMENTS, ItemEnchantments::withTooltip), new ComponentToggle<ItemEnchantments>(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments::withTooltip), new ComponentToggle<Unbreakable>(DataComponents.UNBREAKABLE, Unbreakable::withTooltip), new ComponentToggle<AdventureModePredicate>(DataComponents.CAN_BREAK, AdventureModePredicate::withTooltip), new ComponentToggle<AdventureModePredicate>(DataComponents.CAN_PLACE_ON, AdventureModePredicate::withTooltip), new ComponentToggle<ItemAttributeModifiers>(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers::withTooltip), new ComponentToggle<JukeboxPlayable>(DataComponents.JUKEBOX_PLAYABLE, JukeboxPlayable::withTooltip)).collect(Collectors.toMap(ComponentToggle::type, componentToggle -> componentToggle));
    private static final Codec<ComponentToggle<?>> TOGGLE_CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap(dataComponentType -> {
        ComponentToggle<?> componentToggle = TOGGLES.get(dataComponentType);
        return componentToggle != null ? DataResult.success(componentToggle) : DataResult.error(() -> "Can't toggle tooltip visiblity for " + String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey((DataComponentType<?>)dataComponentType)));
    }, ComponentToggle::type);
    public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec(instance -> ToggleTooltips.commonFields(instance).and((App)Codec.unboundedMap(TOGGLE_CODEC, (Codec)Codec.BOOL).fieldOf("toggles").forGetter(toggleTooltips -> toggleTooltips.values)).apply((Applicative)instance, ToggleTooltips::new));
    private final Map<ComponentToggle<?>, Boolean> values;

    private ToggleTooltips(List<LootItemCondition> list, Map<ComponentToggle<?>, Boolean> map) {
        super(list);
        this.values = map;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        this.values.forEach((componentToggle, bl) -> componentToggle.applyIfPresent(itemStack, (boolean)bl));
        return itemStack;
    }

    public LootItemFunctionType<ToggleTooltips> getType() {
        return LootItemFunctions.TOGGLE_TOOLTIPS;
    }

    record ComponentToggle<T>(DataComponentType<T> type, TooltipWither<T> setter) {
        public void applyIfPresent(ItemStack itemStack, boolean bl) {
            T t = itemStack.get(this.type);
            if (t != null) {
                itemStack.set(this.type, this.setter.withTooltip(t, bl));
            }
        }
    }

    @FunctionalInterface
    static interface TooltipWither<T> {
        public T withTooltip(T var1, boolean var2);
    }
}

