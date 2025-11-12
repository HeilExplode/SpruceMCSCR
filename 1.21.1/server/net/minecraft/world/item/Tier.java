/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public interface Tier {
    public int getUses();

    public float getSpeed();

    public float getAttackDamageBonus();

    public TagKey<Block> getIncorrectBlocksForDrops();

    public int getEnchantmentValue();

    public Ingredient getRepairIngredient();

    default public Tool createToolProperties(TagKey<Block> tagKey) {
        return new Tool(List.of(Tool.Rule.deniesDrops(this.getIncorrectBlocksForDrops()), Tool.Rule.minesAndDrops(tagKey, this.getSpeed())), 1.0f, 1);
    }
}

