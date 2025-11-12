/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public enum EquipmentSlotGroup implements StringRepresentable
{
    ANY(0, "any", equipmentSlot -> true),
    MAINHAND(1, "mainhand", EquipmentSlot.MAINHAND),
    OFFHAND(2, "offhand", EquipmentSlot.OFFHAND),
    HAND(3, "hand", equipmentSlot -> equipmentSlot.getType() == EquipmentSlot.Type.HAND),
    FEET(4, "feet", EquipmentSlot.FEET),
    LEGS(5, "legs", EquipmentSlot.LEGS),
    CHEST(6, "chest", EquipmentSlot.CHEST),
    HEAD(7, "head", EquipmentSlot.HEAD),
    ARMOR(8, "armor", EquipmentSlot::isArmor),
    BODY(9, "body", EquipmentSlot.BODY);

    public static final IntFunction<EquipmentSlotGroup> BY_ID;
    public static final Codec<EquipmentSlotGroup> CODEC;
    public static final StreamCodec<ByteBuf, EquipmentSlotGroup> STREAM_CODEC;
    private final int id;
    private final String key;
    private final Predicate<EquipmentSlot> predicate;

    private EquipmentSlotGroup(int n2, String string2, Predicate<EquipmentSlot> predicate) {
        this.id = n2;
        this.key = string2;
        this.predicate = predicate;
    }

    private EquipmentSlotGroup(int n2, String string2, EquipmentSlot equipmentSlot) {
        this(n2, string2, (EquipmentSlot equipmentSlot2) -> equipmentSlot2 == equipmentSlot);
    }

    public static EquipmentSlotGroup bySlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> MAINHAND;
            case EquipmentSlot.OFFHAND -> OFFHAND;
            case EquipmentSlot.FEET -> FEET;
            case EquipmentSlot.LEGS -> LEGS;
            case EquipmentSlot.CHEST -> CHEST;
            case EquipmentSlot.HEAD -> HEAD;
            case EquipmentSlot.BODY -> BODY;
        };
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    public boolean test(EquipmentSlot equipmentSlot) {
        return this.predicate.test(equipmentSlot);
    }

    static {
        BY_ID = ByIdMap.continuous(equipmentSlotGroup -> equipmentSlotGroup.id, EquipmentSlotGroup.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        CODEC = StringRepresentable.fromEnum(EquipmentSlotGroup::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, equipmentSlotGroup -> equipmentSlotGroup.id);
    }
}

