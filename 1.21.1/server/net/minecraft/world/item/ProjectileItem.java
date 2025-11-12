/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public interface ProjectileItem {
    public Projectile asProjectile(Level var1, Position var2, ItemStack var3, Direction var4);

    default public DispenseConfig createDispenseConfig() {
        return DispenseConfig.DEFAULT;
    }

    default public void shoot(Projectile projectile, double d, double d2, double d3, float f, float f2) {
        projectile.shoot(d, d2, d3, f, f2);
    }

    public record DispenseConfig(PositionFunction positionFunction, float uncertainty, float power, OptionalInt overrideDispenseEvent) {
        public static final DispenseConfig DEFAULT = DispenseConfig.builder().build();

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private PositionFunction positionFunction = (blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 0.7, new Vec3(0.0, 0.1, 0.0));
            private float uncertainty = 6.0f;
            private float power = 1.1f;
            private OptionalInt overrideDispenseEvent = OptionalInt.empty();

            public Builder positionFunction(PositionFunction positionFunction) {
                this.positionFunction = positionFunction;
                return this;
            }

            public Builder uncertainty(float f) {
                this.uncertainty = f;
                return this;
            }

            public Builder power(float f) {
                this.power = f;
                return this;
            }

            public Builder overrideDispenseEvent(int n) {
                this.overrideDispenseEvent = OptionalInt.of(n);
                return this;
            }

            public DispenseConfig build() {
                return new DispenseConfig(this.positionFunction, this.uncertainty, this.power, this.overrideDispenseEvent);
            }
        }
    }

    @FunctionalInterface
    public static interface PositionFunction {
        public Position getDispensePosition(BlockSource var1, Direction var2);
    }
}

