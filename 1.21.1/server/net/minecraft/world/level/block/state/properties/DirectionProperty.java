/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class DirectionProperty
extends EnumProperty<Direction> {
    protected DirectionProperty(String string, Collection<Direction> collection) {
        super(string, Direction.class, collection);
    }

    public static DirectionProperty create(String string) {
        return DirectionProperty.create(string, (Direction direction) -> true);
    }

    public static DirectionProperty create(String string, Predicate<Direction> predicate) {
        return DirectionProperty.create(string, Arrays.stream(Direction.values()).filter(predicate).collect(Collectors.toList()));
    }

    public static DirectionProperty create(String string, Direction ... directionArray) {
        return DirectionProperty.create(string, Lists.newArrayList((Object[])directionArray));
    }

    public static DirectionProperty create(String string, Collection<Direction> collection) {
        return new DirectionProperty(string, collection);
    }
}

