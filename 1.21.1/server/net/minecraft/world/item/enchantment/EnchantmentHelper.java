/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

public class EnchantmentHelper {
    public static int getItemEnchantmentLevel(Holder<Enchantment> holder, ItemStack itemStack) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return itemEnchantments.getLevel(holder);
    }

    public static ItemEnchantments updateEnchantments(ItemStack itemStack, Consumer<ItemEnchantments.Mutable> consumer) {
        DataComponentType<ItemEnchantments> dataComponentType = EnchantmentHelper.getComponentType(itemStack);
        ItemEnchantments itemEnchantments = itemStack.get(dataComponentType);
        if (itemEnchantments == null) {
            return ItemEnchantments.EMPTY;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
        consumer.accept(mutable);
        ItemEnchantments itemEnchantments2 = mutable.toImmutable();
        itemStack.set(dataComponentType, itemEnchantments2);
        return itemEnchantments2;
    }

    public static boolean canStoreEnchantments(ItemStack itemStack) {
        return itemStack.has(EnchantmentHelper.getComponentType(itemStack));
    }

    public static void setEnchantments(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        itemStack.set(EnchantmentHelper.getComponentType(itemStack), itemEnchantments);
    }

    public static ItemEnchantments getEnchantmentsForCrafting(ItemStack itemStack) {
        return itemStack.getOrDefault(EnchantmentHelper.getComponentType(itemStack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> getComponentType(ItemStack itemStack) {
        return itemStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    public static boolean hasAnyEnchantments(ItemStack itemStack) {
        return !itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() || !itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static int processDurabilityChange(ServerLevel serverLevel, ItemStack itemStack, int n2) {
        MutableFloat mutableFloat = new MutableFloat((float)n2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyDurabilityChange(serverLevel, n, itemStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processAmmoUse(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2, int n2) {
        MutableFloat mutableFloat = new MutableFloat((float)n2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyAmmoCount(serverLevel, n, itemStack2, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processBlockExperience(ServerLevel serverLevel, ItemStack itemStack, int n2) {
        MutableFloat mutableFloat = new MutableFloat((float)n2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyBlockExperience(serverLevel, n, itemStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processMobExperience(ServerLevel serverLevel, @Nullable Entity entity, Entity entity2, int n2) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            MutableFloat mutableFloat = new MutableFloat((float)n2);
            EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).modifyMobExperience(serverLevel, n, enchantedItemInUse.itemStack(), entity2, mutableFloat));
            return mutableFloat.intValue();
        }
        return n2;
    }

    private static void runIterationOnItem(ItemStack itemStack, EnchantmentVisitor enchantmentVisitor) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            enchantmentVisitor.accept((Holder)entry.getKey(), entry.getIntValue());
        }
    }

    private static void runIterationOnItem(ItemStack itemStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, EnchantmentInSlotVisitor enchantmentInSlotVisitor) {
        if (itemStack.isEmpty()) {
            return;
        }
        ItemEnchantments itemEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
        if (itemEnchantments == null || itemEnchantments.isEmpty()) {
            return;
        }
        EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            Holder holder = (Holder)entry.getKey();
            if (!((Enchantment)holder.value()).matchingSlot(equipmentSlot)) continue;
            enchantmentInSlotVisitor.accept(holder, entry.getIntValue(), enchantedItemInUse);
        }
    }

    private static void runIterationOnEquipment(LivingEntity livingEntity, EnchantmentInSlotVisitor enchantmentInSlotVisitor) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EnchantmentHelper.runIterationOnItem(livingEntity.getItemBySlot(equipmentSlot), equipmentSlot, livingEntity, enchantmentInSlotVisitor);
        }
    }

    public static boolean isImmuneToDamage(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> mutableBoolean.setValue(mutableBoolean.isTrue() || ((Enchantment)holder.value()).isImmuneToDamage(serverLevel, n, livingEntity, damageSource)));
        return mutableBoolean.isTrue();
    }

    public static float getDamageProtection(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).modifyDamageProtection(serverLevel, n, enchantedItemInUse.itemStack(), livingEntity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyDamage(serverLevel, n, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyFallBasedDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyFallBasedDamage(serverLevel, n, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyArmorEffectiveness(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyArmorEffectivness(serverLevel, n, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyKnockback(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyKnockback(serverLevel, n, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static void doPostAttackEffects(ServerLevel serverLevel, Entity entity, DamageSource damageSource) {
        Entity entity2 = damageSource.getEntity();
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, livingEntity.getWeaponItem());
        } else {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, null);
        }
    }

    public static void doPostAttackEffectsWithItemSource(ServerLevel serverLevel, Entity entity, DamageSource damageSource, @Nullable ItemStack itemStack) {
        Entity entity2;
        LivingEntity livingEntity;
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).doPostAttack(serverLevel, n, enchantedItemInUse, EnchantmentTarget.VICTIM, entity, damageSource));
        }
        if (itemStack != null && (entity2 = damageSource.getEntity()) instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity2;
            EnchantmentHelper.runIterationOnItem(itemStack, EquipmentSlot.MAINHAND, livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).doPostAttack(serverLevel, n, enchantedItemInUse, EnchantmentTarget.ATTACKER, entity, damageSource));
        }
    }

    public static void runLocationChangedEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).runLocationChangedEffects(serverLevel, n, enchantedItemInUse, livingEntity));
    }

    public static void runLocationChangedEffects(ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        EnchantmentHelper.runIterationOnItem(itemStack, equipmentSlot, livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).runLocationChangedEffects(serverLevel, n, enchantedItemInUse, livingEntity));
    }

    public static void stopLocationBasedEffects(LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).stopLocationBasedEffects(n, enchantedItemInUse, livingEntity));
    }

    public static void stopLocationBasedEffects(ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        EnchantmentHelper.runIterationOnItem(itemStack, equipmentSlot, livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).stopLocationBasedEffects(n, enchantedItemInUse, livingEntity));
    }

    public static void tickEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> ((Enchantment)holder.value()).tick(serverLevel, n, enchantedItemInUse, livingEntity));
    }

    public static int getEnchantmentLevel(Holder<Enchantment> holder, LivingEntity livingEntity) {
        Collection<ItemStack> collection = holder.value().getSlotItems(livingEntity).values();
        int n = 0;
        for (ItemStack itemStack : collection) {
            int n2 = EnchantmentHelper.getItemEnchantmentLevel(holder, itemStack);
            if (n2 <= n) continue;
            n = n2;
        }
        return n;
    }

    public static int processProjectileCount(ServerLevel serverLevel, ItemStack itemStack, Entity entity, int n2) {
        MutableFloat mutableFloat = new MutableFloat((float)n2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyProjectileCount(serverLevel, n, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float processProjectileSpread(ServerLevel serverLevel, ItemStack itemStack, Entity entity, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyProjectileSpread(serverLevel, n, itemStack, entity, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getPiercingCount(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyPiercingCount(serverLevel, n, itemStack2, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static void onProjectileSpawned(ServerLevel serverLevel, ItemStack itemStack, AbstractArrow abstractArrow, Consumer<Item> consumer) {
        LivingEntity livingEntity;
        Object object;
        Entity entity = abstractArrow.getOwner();
        if (entity instanceof LivingEntity) {
            object = (LivingEntity)entity;
            livingEntity = object;
        } else {
            livingEntity = null;
        }
        LivingEntity livingEntity2 = livingEntity;
        object = new EnchantedItemInUse(itemStack, null, livingEntity2, consumer);
        EnchantmentHelper.runIterationOnItem(itemStack, (arg_0, arg_1) -> EnchantmentHelper.lambda$onProjectileSpawned$20(serverLevel, (EnchantedItemInUse)object, abstractArrow, arg_0, arg_1));
    }

    public static void onHitBlock(ServerLevel serverLevel, ItemStack itemStack, @Nullable LivingEntity livingEntity, Entity entity, @Nullable EquipmentSlot equipmentSlot, Vec3 vec3, BlockState blockState, Consumer<Item> consumer) {
        EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity, consumer);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).onHitBlock(serverLevel, n, enchantedItemInUse, entity, vec3, blockState));
    }

    public static int modifyDurabilityToRepairFromXp(ServerLevel serverLevel, ItemStack itemStack, int n2) {
        MutableFloat mutableFloat = new MutableFloat((float)n2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyDurabilityToRepairFromXp(serverLevel, n, itemStack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float processEquipmentDropChance(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        RandomSource randomSource = livingEntity.getRandom();
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, n, enchantedItemInUse) -> {
            LootContext lootContext = Enchantment.damageContext(serverLevel, n, livingEntity, damageSource);
            ((Enchantment)holder.value()).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach(targetedConditionalEffect -> {
                if (targetedConditionalEffect.enchanted() == EnchantmentTarget.VICTIM && targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM && targetedConditionalEffect.matches(lootContext)) {
                    mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(n, randomSource, mutableFloat.floatValue()));
                }
            });
        });
        Entity entity = damageSource.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            EnchantmentHelper.runIterationOnEquipment(livingEntity2, (holder, n, enchantedItemInUse) -> {
                LootContext lootContext = Enchantment.damageContext(serverLevel, n, livingEntity, damageSource);
                ((Enchantment)holder.value()).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach(targetedConditionalEffect -> {
                    if (targetedConditionalEffect.enchanted() == EnchantmentTarget.ATTACKER && targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM && targetedConditionalEffect.matches(lootContext)) {
                        mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(n, randomSource, mutableFloat.floatValue()));
                    }
                });
            });
        }
        return mutableFloat.floatValue();
    }

    public static void forEachModifier(ItemStack itemStack, EquipmentSlotGroup equipmentSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(enchantmentAttributeEffect -> {
            if (((Enchantment)holder.value()).definition().slots().contains(equipmentSlotGroup)) {
                biConsumer.accept(enchantmentAttributeEffect.attribute(), enchantmentAttributeEffect.getModifier(n, equipmentSlotGroup));
            }
        }));
    }

    public static void forEachModifier(ItemStack itemStack, EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(enchantmentAttributeEffect -> {
            if (((Enchantment)holder.value()).matchingSlot(equipmentSlot)) {
                biConsumer.accept(enchantmentAttributeEffect.attribute(), enchantmentAttributeEffect.getModifier(n, equipmentSlot));
            }
        }));
    }

    public static int getFishingLuckBonus(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyFishingLuckBonus(serverLevel, n, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getFishingTimeReduction(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyFishingTimeReduction(serverLevel, n, itemStack, entity, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getTridentReturnToOwnerAcceleration(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyTridentReturnToOwnerAcceleration(serverLevel, n, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float modifyCrossbowChargingTime(ItemStack itemStack, LivingEntity livingEntity, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyCrossbowChargeTime(livingEntity.getRandom(), n, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack itemStack, LivingEntity livingEntity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> ((Enchantment)holder.value()).modifyTridentSpinAttackStrength(livingEntity.getRandom(), n, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static boolean hasTag(ItemStack itemStack, TagKey<Enchantment> tagKey) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            Holder holder = (Holder)entry.getKey();
            if (!holder.is(tagKey)) continue;
            return true;
        }
        return false;
    }

    public static boolean has(ItemStack itemStack, DataComponentType<?> dataComponentType) {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> {
            if (((Enchantment)holder.value()).effects().has(dataComponentType)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.booleanValue();
    }

    public static <T> Optional<T> pickHighestLevel(ItemStack itemStack, DataComponentType<List<T>> dataComponentType) {
        Pair<List<T>, Integer> pair = EnchantmentHelper.getHighestLevel(itemStack, dataComponentType);
        if (pair != null) {
            List list = (List)pair.getFirst();
            int n = (Integer)pair.getSecond();
            return Optional.of(list.get(Math.min(n, list.size()) - 1));
        }
        return Optional.empty();
    }

    @Nullable
    public static <T> Pair<T, Integer> getHighestLevel(ItemStack itemStack, DataComponentType<T> dataComponentType) {
        MutableObject mutableObject = new MutableObject();
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, n) -> {
            Object t;
            if ((mutableObject.getValue() == null || (Integer)((Pair)mutableObject.getValue()).getSecond() < n) && (t = ((Enchantment)holder.value()).effects().get(dataComponentType)) != null) {
                mutableObject.setValue((Object)Pair.of(t, (Object)n));
            }
        });
        return (Pair)mutableObject.getValue();
    }

    public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> dataComponentType, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        ArrayList<EnchantedItemInUse> arrayList = new ArrayList<EnchantedItemInUse>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
            if (!predicate.test(itemStack)) continue;
            ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Holder holder = (Holder)entry.getKey();
                if (!((Enchantment)holder.value()).effects().has(dataComponentType) || !((Enchantment)holder.value()).matchingSlot(equipmentSlot)) continue;
                arrayList.add(new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity));
            }
        }
        return Util.getRandomSafe(arrayList, livingEntity.getRandom());
    }

    public static int getEnchantmentCost(RandomSource randomSource, int n, int n2, ItemStack itemStack) {
        Item item = itemStack.getItem();
        int n3 = item.getEnchantmentValue();
        if (n3 <= 0) {
            return 0;
        }
        if (n2 > 15) {
            n2 = 15;
        }
        int n4 = randomSource.nextInt(8) + 1 + (n2 >> 1) + randomSource.nextInt(n2 + 1);
        if (n == 0) {
            return Math.max(n4 / 3, 1);
        }
        if (n == 1) {
            return n4 * 2 / 3 + 1;
        }
        return Math.max(n4, n2 * 2);
    }

    public static ItemStack enchantItem(RandomSource randomSource, ItemStack itemStack, int n, RegistryAccess registryAccess, Optional<? extends HolderSet<Enchantment>> optional) {
        return EnchantmentHelper.enchantItem(randomSource, itemStack, n, optional.map(HolderSet::stream).orElseGet(() -> registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().map(reference -> reference)));
    }

    public static ItemStack enchantItem(RandomSource randomSource, ItemStack itemStack, int n, Stream<Holder<Enchantment>> stream) {
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(randomSource, itemStack, n, stream);
        if (itemStack.is(Items.BOOK)) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentInstance enchantmentInstance : list) {
            itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
        }
        return itemStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource randomSource, ItemStack itemStack, int n, Stream<Holder<Enchantment>> stream) {
        ArrayList arrayList = Lists.newArrayList();
        Item item = itemStack.getItem();
        int n2 = item.getEnchantmentValue();
        if (n2 <= 0) {
            return arrayList;
        }
        n += 1 + randomSource.nextInt(n2 / 4 + 1) + randomSource.nextInt(n2 / 4 + 1);
        float f = (randomSource.nextFloat() + randomSource.nextFloat() - 1.0f) * 0.15f;
        List<EnchantmentInstance> list = EnchantmentHelper.getAvailableEnchantmentResults(n = Mth.clamp(Math.round((float)n + (float)n * f), 1, Integer.MAX_VALUE), itemStack, stream);
        if (!list.isEmpty()) {
            WeightedRandom.getRandomItem(randomSource, list).ifPresent(arrayList::add);
            while (randomSource.nextInt(50) <= n) {
                if (!arrayList.isEmpty()) {
                    EnchantmentHelper.filterCompatibleEnchantments(list, (EnchantmentInstance)Util.lastOf(arrayList));
                }
                if (list.isEmpty()) break;
                WeightedRandom.getRandomItem(randomSource, list).ifPresent(arrayList::add);
                n /= 2;
            }
        }
        return arrayList;
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentInstance) {
        list.removeIf(enchantmentInstance2 -> !Enchantment.areCompatible(enchantmentInstance.enchantment, enchantmentInstance2.enchantment));
    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Holder<Enchantment> holder) {
        for (Holder<Enchantment> holder2 : collection) {
            if (Enchantment.areCompatible(holder2, holder)) continue;
            return false;
        }
        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int n, ItemStack itemStack, Stream<Holder<Enchantment>> stream) {
        ArrayList arrayList = Lists.newArrayList();
        boolean bl = itemStack.is(Items.BOOK);
        stream.filter(holder -> ((Enchantment)holder.value()).isPrimaryItem(itemStack) || bl).forEach(holder -> {
            Enchantment enchantment = (Enchantment)holder.value();
            for (int i = enchantment.getMaxLevel(); i >= enchantment.getMinLevel(); --i) {
                if (n < enchantment.getMinCost(i) || n > enchantment.getMaxCost(i)) continue;
                arrayList.add(new EnchantmentInstance((Holder<Enchantment>)holder, i));
                break;
            }
        });
        return arrayList;
    }

    public static void enchantItemFromProvider(ItemStack itemStack, RegistryAccess registryAccess, ResourceKey<EnchantmentProvider> resourceKey, DifficultyInstance difficultyInstance, RandomSource randomSource) {
        EnchantmentProvider enchantmentProvider = registryAccess.registryOrThrow(Registries.ENCHANTMENT_PROVIDER).get(resourceKey);
        if (enchantmentProvider != null) {
            EnchantmentHelper.updateEnchantments(itemStack, mutable -> enchantmentProvider.enchant(itemStack, (ItemEnchantments.Mutable)mutable, randomSource, difficultyInstance));
        }
    }

    private static /* synthetic */ void lambda$onProjectileSpawned$20(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, AbstractArrow abstractArrow, Holder holder, int n) {
        ((Enchantment)holder.value()).onProjectileSpawned(serverLevel, n, enchantedItemInUse, abstractArrow);
    }

    @FunctionalInterface
    static interface EnchantmentVisitor {
        public void accept(Holder<Enchantment> var1, int var2);
    }

    @FunctionalInterface
    static interface EnchantmentInSlotVisitor {
        public void accept(Holder<Enchantment> var1, int var2, EnchantedItemInUse var3);
    }
}

