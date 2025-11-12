/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem
extends Item
implements Equipable {
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior(){

        @Override
        protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
            return ArmorItem.dispenseArmor(blockSource, itemStack) ? itemStack : super.execute(blockSource, itemStack);
        }
    };
    protected final Type type;
    protected final Holder<ArmorMaterial> material;
    private final Supplier<ItemAttributeModifiers> defaultModifiers;

    public static boolean dispenseArmor(BlockSource blockSource, ItemStack itemStack) {
        BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
        List<Entity> list = blockSource.level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(itemStack)));
        if (list.isEmpty()) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity)list.get(0);
        EquipmentSlot equipmentSlot = livingEntity.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = itemStack.split(1);
        livingEntity.setItemSlot(equipmentSlot, itemStack2);
        if (livingEntity instanceof Mob) {
            ((Mob)livingEntity).setDropChance(equipmentSlot, 2.0f);
            ((Mob)livingEntity).setPersistenceRequired();
        }
        return true;
    }

    public ArmorItem(Holder<ArmorMaterial> holder, Type type, Item.Properties properties) {
        super(properties);
        this.material = holder;
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
        this.defaultModifiers = Suppliers.memoize(() -> {
            int n = ((ArmorMaterial)holder.value()).getDefense(type);
            float f = ((ArmorMaterial)holder.value()).toughness();
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
            ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor." + type.getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(resourceLocation, n, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourceLocation, f, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
            float f2 = ((ArmorMaterial)holder.value()).knockbackResistance();
            if (f2 > 0.0f) {
                builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(resourceLocation, f2, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
            }
            return builder.build();
        });
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int getEnchantmentValue() {
        return this.material.value().enchantmentValue();
    }

    public Holder<ArmorMaterial> getMaterial() {
        return this.material;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return this.material.value().repairIngredient().get().test(itemStack2) || super.isValidRepairItem(itemStack, itemStack2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return this.swapWithEquipmentSlot(this, level, player, interactionHand);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.defaultModifiers.get();
    }

    public int getDefense() {
        return this.material.value().getDefense(this.type);
    }

    public float getToughness() {
        return this.material.value().toughness();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return this.getMaterial().value().equipSound();
    }

    public static enum Type implements StringRepresentable
    {
        HELMET(EquipmentSlot.HEAD, 11, "helmet"),
        CHESTPLATE(EquipmentSlot.CHEST, 16, "chestplate"),
        LEGGINGS(EquipmentSlot.LEGS, 15, "leggings"),
        BOOTS(EquipmentSlot.FEET, 13, "boots"),
        BODY(EquipmentSlot.BODY, 16, "body");

        public static final Codec<Type> CODEC;
        private final EquipmentSlot slot;
        private final String name;
        private final int durability;

        private Type(EquipmentSlot equipmentSlot, int n2, String string2) {
            this.slot = equipmentSlot;
            this.name = string2;
            this.durability = n2;
        }

        public int getDurability(int n) {
            return this.durability * n;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        public String getName() {
            return this.name;
        }

        public boolean hasTrims() {
            return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromValues(Type::values);
        }
    }
}

