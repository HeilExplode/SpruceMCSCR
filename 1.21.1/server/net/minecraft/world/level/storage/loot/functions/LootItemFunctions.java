/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionReference;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.ModifyContainerContents;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.functions.SetBannerPatternFunction;
import net.minecraft.world.level.storage.loot.functions.SetBookCoverFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetContainerLootTable;
import net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.SetCustomModelDataFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetFireworkExplosionFunction;
import net.minecraft.world.level.storage.loot.functions.SetFireworksFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetOminousBottleAmplifierFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.functions.SetWritableBookPagesFunction;
import net.minecraft.world.level.storage.loot.functions.SetWrittenBookPagesFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;

public class LootItemFunctions {
    public static final BiFunction<ItemStack, LootContext, ItemStack> IDENTITY = (itemStack, lootContext) -> itemStack;
    public static final Codec<LootItemFunction> TYPED_CODEC = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec().dispatch("function", LootItemFunction::getType, LootItemFunctionType::codec);
    public static final Codec<LootItemFunction> ROOT_CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, SequenceFunction.INLINE_CODEC));
    public static final Codec<Holder<LootItemFunction>> CODEC = RegistryFileCodec.create(Registries.ITEM_MODIFIER, ROOT_CODEC);
    public static final LootItemFunctionType<SetItemCountFunction> SET_COUNT = LootItemFunctions.register("set_count", SetItemCountFunction.CODEC);
    public static final LootItemFunctionType<SetItemFunction> SET_ITEM = LootItemFunctions.register("set_item", SetItemFunction.CODEC);
    public static final LootItemFunctionType<EnchantWithLevelsFunction> ENCHANT_WITH_LEVELS = LootItemFunctions.register("enchant_with_levels", EnchantWithLevelsFunction.CODEC);
    public static final LootItemFunctionType<EnchantRandomlyFunction> ENCHANT_RANDOMLY = LootItemFunctions.register("enchant_randomly", EnchantRandomlyFunction.CODEC);
    public static final LootItemFunctionType<SetEnchantmentsFunction> SET_ENCHANTMENTS = LootItemFunctions.register("set_enchantments", SetEnchantmentsFunction.CODEC);
    public static final LootItemFunctionType<SetCustomDataFunction> SET_CUSTOM_DATA = LootItemFunctions.register("set_custom_data", SetCustomDataFunction.CODEC);
    public static final LootItemFunctionType<SetComponentsFunction> SET_COMPONENTS = LootItemFunctions.register("set_components", SetComponentsFunction.CODEC);
    public static final LootItemFunctionType<SmeltItemFunction> FURNACE_SMELT = LootItemFunctions.register("furnace_smelt", SmeltItemFunction.CODEC);
    public static final LootItemFunctionType<EnchantedCountIncreaseFunction> ENCHANTED_COUNT_INCREASE = LootItemFunctions.register("enchanted_count_increase", EnchantedCountIncreaseFunction.CODEC);
    public static final LootItemFunctionType<SetItemDamageFunction> SET_DAMAGE = LootItemFunctions.register("set_damage", SetItemDamageFunction.CODEC);
    public static final LootItemFunctionType<SetAttributesFunction> SET_ATTRIBUTES = LootItemFunctions.register("set_attributes", SetAttributesFunction.CODEC);
    public static final LootItemFunctionType<SetNameFunction> SET_NAME = LootItemFunctions.register("set_name", SetNameFunction.CODEC);
    public static final LootItemFunctionType<ExplorationMapFunction> EXPLORATION_MAP = LootItemFunctions.register("exploration_map", ExplorationMapFunction.CODEC);
    public static final LootItemFunctionType<SetStewEffectFunction> SET_STEW_EFFECT = LootItemFunctions.register("set_stew_effect", SetStewEffectFunction.CODEC);
    public static final LootItemFunctionType<CopyNameFunction> COPY_NAME = LootItemFunctions.register("copy_name", CopyNameFunction.CODEC);
    public static final LootItemFunctionType<SetContainerContents> SET_CONTENTS = LootItemFunctions.register("set_contents", SetContainerContents.CODEC);
    public static final LootItemFunctionType<ModifyContainerContents> MODIFY_CONTENTS = LootItemFunctions.register("modify_contents", ModifyContainerContents.CODEC);
    public static final LootItemFunctionType<FilteredFunction> FILTERED = LootItemFunctions.register("filtered", FilteredFunction.CODEC);
    public static final LootItemFunctionType<LimitCount> LIMIT_COUNT = LootItemFunctions.register("limit_count", LimitCount.CODEC);
    public static final LootItemFunctionType<ApplyBonusCount> APPLY_BONUS = LootItemFunctions.register("apply_bonus", ApplyBonusCount.CODEC);
    public static final LootItemFunctionType<SetContainerLootTable> SET_LOOT_TABLE = LootItemFunctions.register("set_loot_table", SetContainerLootTable.CODEC);
    public static final LootItemFunctionType<ApplyExplosionDecay> EXPLOSION_DECAY = LootItemFunctions.register("explosion_decay", ApplyExplosionDecay.CODEC);
    public static final LootItemFunctionType<SetLoreFunction> SET_LORE = LootItemFunctions.register("set_lore", SetLoreFunction.CODEC);
    public static final LootItemFunctionType<FillPlayerHead> FILL_PLAYER_HEAD = LootItemFunctions.register("fill_player_head", FillPlayerHead.CODEC);
    public static final LootItemFunctionType<CopyCustomDataFunction> COPY_CUSTOM_DATA = LootItemFunctions.register("copy_custom_data", CopyCustomDataFunction.CODEC);
    public static final LootItemFunctionType<CopyBlockState> COPY_STATE = LootItemFunctions.register("copy_state", CopyBlockState.CODEC);
    public static final LootItemFunctionType<SetBannerPatternFunction> SET_BANNER_PATTERN = LootItemFunctions.register("set_banner_pattern", SetBannerPatternFunction.CODEC);
    public static final LootItemFunctionType<SetPotionFunction> SET_POTION = LootItemFunctions.register("set_potion", SetPotionFunction.CODEC);
    public static final LootItemFunctionType<SetInstrumentFunction> SET_INSTRUMENT = LootItemFunctions.register("set_instrument", SetInstrumentFunction.CODEC);
    public static final LootItemFunctionType<FunctionReference> REFERENCE = LootItemFunctions.register("reference", FunctionReference.CODEC);
    public static final LootItemFunctionType<SequenceFunction> SEQUENCE = LootItemFunctions.register("sequence", SequenceFunction.CODEC);
    public static final LootItemFunctionType<CopyComponentsFunction> COPY_COMPONENTS = LootItemFunctions.register("copy_components", CopyComponentsFunction.CODEC);
    public static final LootItemFunctionType<SetFireworksFunction> SET_FIREWORKS = LootItemFunctions.register("set_fireworks", SetFireworksFunction.CODEC);
    public static final LootItemFunctionType<SetFireworkExplosionFunction> SET_FIREWORK_EXPLOSION = LootItemFunctions.register("set_firework_explosion", SetFireworkExplosionFunction.CODEC);
    public static final LootItemFunctionType<SetBookCoverFunction> SET_BOOK_COVER = LootItemFunctions.register("set_book_cover", SetBookCoverFunction.CODEC);
    public static final LootItemFunctionType<SetWrittenBookPagesFunction> SET_WRITTEN_BOOK_PAGES = LootItemFunctions.register("set_written_book_pages", SetWrittenBookPagesFunction.CODEC);
    public static final LootItemFunctionType<SetWritableBookPagesFunction> SET_WRITABLE_BOOK_PAGES = LootItemFunctions.register("set_writable_book_pages", SetWritableBookPagesFunction.CODEC);
    public static final LootItemFunctionType<ToggleTooltips> TOGGLE_TOOLTIPS = LootItemFunctions.register("toggle_tooltips", ToggleTooltips.CODEC);
    public static final LootItemFunctionType<SetOminousBottleAmplifierFunction> SET_OMINOUS_BOTTLE_AMPLIFIER = LootItemFunctions.register("set_ominous_bottle_amplifier", SetOminousBottleAmplifierFunction.CODEC);
    public static final LootItemFunctionType<SetCustomModelDataFunction> SET_CUSTOM_MODEL_DATA = LootItemFunctions.register("set_custom_model_data", SetCustomModelDataFunction.CODEC);

    private static <T extends LootItemFunction> LootItemFunctionType<T> register(String string, MapCodec<T> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceLocation.withDefaultNamespace(string), new LootItemFunctionType<T>(mapCodec));
    }

    public static BiFunction<ItemStack, LootContext, ItemStack> compose(List<? extends BiFunction<ItemStack, LootContext, ItemStack>> list) {
        List<? extends BiFunction<ItemStack, LootContext, ItemStack>> list2 = List.copyOf(list);
        return switch (list2.size()) {
            case 0 -> IDENTITY;
            case 1 -> list2.get(0);
            case 2 -> {
                BiFunction<ItemStack, LootContext, ItemStack> var2_2 = list2.get(0);
                BiFunction<ItemStack, LootContext, ItemStack> var3_3 = list2.get(1);
                yield (itemStack, lootContext) -> (ItemStack)var3_3.apply((ItemStack)var2_2.apply((ItemStack)itemStack, (LootContext)lootContext), (LootContext)lootContext);
            }
            default -> (itemStack, lootContext) -> {
                for (BiFunction biFunction : list2) {
                    itemStack = (ItemStack)biFunction.apply(itemStack, lootContext);
                }
                return itemStack;
            };
        };
    }
}

