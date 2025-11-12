/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;

public class TreeDecoratorType<P extends TreeDecorator> {
    public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = TreeDecoratorType.register("trunk_vine", TrunkVineDecorator.CODEC);
    public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = TreeDecoratorType.register("leave_vine", LeaveVineDecorator.CODEC);
    public static final TreeDecoratorType<CocoaDecorator> COCOA = TreeDecoratorType.register("cocoa", CocoaDecorator.CODEC);
    public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = TreeDecoratorType.register("beehive", BeehiveDecorator.CODEC);
    public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = TreeDecoratorType.register("alter_ground", AlterGroundDecorator.CODEC);
    public static final TreeDecoratorType<AttachedToLeavesDecorator> ATTACHED_TO_LEAVES = TreeDecoratorType.register("attached_to_leaves", AttachedToLeavesDecorator.CODEC);
    private final MapCodec<P> codec;

    private static <P extends TreeDecorator> TreeDecoratorType<P> register(String string, MapCodec<P> mapCodec) {
        return Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, string, new TreeDecoratorType<P>(mapCodec));
    }

    private TreeDecoratorType(MapCodec<P> mapCodec) {
        this.codec = mapCodec;
    }

    public MapCodec<P> codec() {
        return this.codec;
    }
}

