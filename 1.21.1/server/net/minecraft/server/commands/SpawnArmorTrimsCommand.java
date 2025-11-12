/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SpawnArmorTrimsCommand {
    private static final Map<Pair<Holder<ArmorMaterial>, EquipmentSlot>, Item> MATERIAL_AND_SLOT_TO_ITEM = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(Pair.of(ArmorMaterials.CHAIN, (Object)EquipmentSlot.HEAD), Items.CHAINMAIL_HELMET);
        hashMap.put(Pair.of(ArmorMaterials.CHAIN, (Object)EquipmentSlot.CHEST), Items.CHAINMAIL_CHESTPLATE);
        hashMap.put(Pair.of(ArmorMaterials.CHAIN, (Object)EquipmentSlot.LEGS), Items.CHAINMAIL_LEGGINGS);
        hashMap.put(Pair.of(ArmorMaterials.CHAIN, (Object)EquipmentSlot.FEET), Items.CHAINMAIL_BOOTS);
        hashMap.put(Pair.of(ArmorMaterials.IRON, (Object)EquipmentSlot.HEAD), Items.IRON_HELMET);
        hashMap.put(Pair.of(ArmorMaterials.IRON, (Object)EquipmentSlot.CHEST), Items.IRON_CHESTPLATE);
        hashMap.put(Pair.of(ArmorMaterials.IRON, (Object)EquipmentSlot.LEGS), Items.IRON_LEGGINGS);
        hashMap.put(Pair.of(ArmorMaterials.IRON, (Object)EquipmentSlot.FEET), Items.IRON_BOOTS);
        hashMap.put(Pair.of(ArmorMaterials.GOLD, (Object)EquipmentSlot.HEAD), Items.GOLDEN_HELMET);
        hashMap.put(Pair.of(ArmorMaterials.GOLD, (Object)EquipmentSlot.CHEST), Items.GOLDEN_CHESTPLATE);
        hashMap.put(Pair.of(ArmorMaterials.GOLD, (Object)EquipmentSlot.LEGS), Items.GOLDEN_LEGGINGS);
        hashMap.put(Pair.of(ArmorMaterials.GOLD, (Object)EquipmentSlot.FEET), Items.GOLDEN_BOOTS);
        hashMap.put(Pair.of(ArmorMaterials.NETHERITE, (Object)EquipmentSlot.HEAD), Items.NETHERITE_HELMET);
        hashMap.put(Pair.of(ArmorMaterials.NETHERITE, (Object)EquipmentSlot.CHEST), Items.NETHERITE_CHESTPLATE);
        hashMap.put(Pair.of(ArmorMaterials.NETHERITE, (Object)EquipmentSlot.LEGS), Items.NETHERITE_LEGGINGS);
        hashMap.put(Pair.of(ArmorMaterials.NETHERITE, (Object)EquipmentSlot.FEET), Items.NETHERITE_BOOTS);
        hashMap.put(Pair.of(ArmorMaterials.DIAMOND, (Object)EquipmentSlot.HEAD), Items.DIAMOND_HELMET);
        hashMap.put(Pair.of(ArmorMaterials.DIAMOND, (Object)EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE);
        hashMap.put(Pair.of(ArmorMaterials.DIAMOND, (Object)EquipmentSlot.LEGS), Items.DIAMOND_LEGGINGS);
        hashMap.put(Pair.of(ArmorMaterials.DIAMOND, (Object)EquipmentSlot.FEET), Items.DIAMOND_BOOTS);
        hashMap.put(Pair.of(ArmorMaterials.TURTLE, (Object)EquipmentSlot.HEAD), Items.TURTLE_HELMET);
    });
    private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(TrimPatterns.SENTRY, TrimPatterns.DUNE, TrimPatterns.COAST, TrimPatterns.WILD, TrimPatterns.WARD, TrimPatterns.EYE, TrimPatterns.VEX, TrimPatterns.TIDE, TrimPatterns.SNOUT, TrimPatterns.RIB, TrimPatterns.SPIRE, TrimPatterns.WAYFINDER, TrimPatterns.SHAPER, TrimPatterns.SILENCE, TrimPatterns.RAISER, TrimPatterns.HOST, TrimPatterns.FLOW, TrimPatterns.BOLT);
    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.LAPIS, TrimMaterials.AMETHYST);
    private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
    private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawn_armor_trims").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> SpawnArmorTrimsCommand.spawnArmorTrims((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException())));
    }

    private static int spawnArmorTrims(CommandSourceStack commandSourceStack, Player player) {
        Level level = player.level();
        NonNullList<ArmorTrim> nonNullList = NonNullList.create();
        Registry<TrimPattern> registry = level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN);
        Registry<TrimMaterial> registry2 = level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL);
        registry.stream().sorted(Comparator.comparing(trimPattern -> TRIM_PATTERN_ORDER.applyAsInt(registry.getResourceKey((TrimPattern)trimPattern).orElse(null)))).forEachOrdered(trimPattern -> registry2.stream().sorted(Comparator.comparing(trimMaterial -> TRIM_MATERIAL_ORDER.applyAsInt(registry2.getResourceKey((TrimMaterial)trimMaterial).orElse(null)))).forEachOrdered(trimMaterial -> nonNullList.add(new ArmorTrim(registry2.wrapAsHolder((TrimMaterial)trimMaterial), registry.wrapAsHolder((TrimPattern)trimPattern)))));
        BlockPos blockPos = player.blockPosition().relative(player.getDirection(), 5);
        Registry<ArmorMaterial> registry3 = commandSourceStack.registryAccess().registryOrThrow(Registries.ARMOR_MATERIAL);
        int n = registry3.size() - 1;
        double d = 3.0;
        int n2 = 0;
        int n3 = 0;
        for (ArmorTrim armorTrim : nonNullList) {
            for (ArmorMaterial armorMaterial : registry3) {
                if (armorMaterial == ArmorMaterials.LEATHER.value()) continue;
                double d2 = (double)blockPos.getX() + 0.5 - (double)(n2 % registry2.size()) * 3.0;
                double d3 = (double)blockPos.getY() + 0.5 + (double)(n3 % n) * 3.0;
                double d4 = (double)blockPos.getZ() + 0.5 + (double)(n2 / registry2.size() * 10);
                ArmorStand armorStand = new ArmorStand(level, d2, d3, d4);
                armorStand.setYRot(180.0f);
                armorStand.setNoGravity(true);
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    ArmorItem armorItem;
                    Item item = MATERIAL_AND_SLOT_TO_ITEM.get(Pair.of((Object)armorMaterial, (Object)equipmentSlot));
                    if (item == null) continue;
                    ItemStack itemStack = new ItemStack(item);
                    itemStack.set(DataComponents.TRIM, armorTrim);
                    armorStand.setItemSlot(equipmentSlot, itemStack);
                    if (item instanceof ArmorItem && (armorItem = (ArmorItem)item).getMaterial().is(ArmorMaterials.TURTLE)) {
                        armorStand.setCustomName(armorTrim.pattern().value().copyWithStyle(armorTrim.material()).copy().append(" ").append(armorTrim.material().value().description()));
                        armorStand.setCustomNameVisible(true);
                        continue;
                    }
                    armorStand.setInvisible(true);
                }
                level.addFreshEntity(armorStand);
                ++n3;
            }
            ++n2;
        }
        commandSourceStack.sendSuccess(() -> Component.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }
}

