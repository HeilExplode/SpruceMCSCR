/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnimalArmorItem
extends ArmorItem {
    private final ResourceLocation textureLocation;
    @Nullable
    private final ResourceLocation overlayTextureLocation;
    private final BodyType bodyType;

    public AnimalArmorItem(Holder<ArmorMaterial> holder, BodyType bodyType, boolean bl, Item.Properties properties) {
        super(holder, ArmorItem.Type.BODY, properties);
        this.bodyType = bodyType;
        ResourceLocation resourceLocation = bodyType.textureLocator.apply(holder.unwrapKey().orElseThrow().location());
        this.textureLocation = resourceLocation.withSuffix(".png");
        this.overlayTextureLocation = bl ? resourceLocation.withSuffix("_overlay.png") : null;
    }

    public ResourceLocation getTexture() {
        return this.textureLocation;
    }

    @Nullable
    public ResourceLocation getOverlayTexture() {
        return this.overlayTextureLocation;
    }

    public BodyType getBodyType() {
        return this.bodyType;
    }

    @Override
    public SoundEvent getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    public static enum BodyType {
        EQUESTRIAN(resourceLocation -> resourceLocation.withPath(string -> "textures/entity/horse/armor/horse_armor_" + string), SoundEvents.ITEM_BREAK),
        CANINE(resourceLocation -> resourceLocation.withPath("textures/entity/wolf/wolf_armor"), SoundEvents.WOLF_ARMOR_BREAK);

        final Function<ResourceLocation, ResourceLocation> textureLocator;
        final SoundEvent breakingSound;

        private BodyType(Function<ResourceLocation, ResourceLocation> function, SoundEvent soundEvent) {
            this.textureLocator = function;
            this.breakingSound = soundEvent;
        }
    }
}

