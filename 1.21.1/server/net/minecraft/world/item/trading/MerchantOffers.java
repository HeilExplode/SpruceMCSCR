/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantOffers
extends ArrayList<MerchantOffer> {
    public static final Codec<MerchantOffers> CODEC = MerchantOffer.CODEC.listOf().fieldOf("Recipes").xmap(MerchantOffers::new, Function.identity()).codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffers> STREAM_CODEC = MerchantOffer.STREAM_CODEC.apply(ByteBufCodecs.collection(MerchantOffers::new));

    public MerchantOffers() {
    }

    private MerchantOffers(int n) {
        super(n);
    }

    private MerchantOffers(Collection<MerchantOffer> collection) {
        super(collection);
    }

    @Nullable
    public MerchantOffer getRecipeFor(ItemStack itemStack, ItemStack itemStack2, int n) {
        if (n > 0 && n < this.size()) {
            MerchantOffer merchantOffer = (MerchantOffer)this.get(n);
            if (merchantOffer.satisfiedBy(itemStack, itemStack2)) {
                return merchantOffer;
            }
            return null;
        }
        for (int i = 0; i < this.size(); ++i) {
            MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
            if (!merchantOffer.satisfiedBy(itemStack, itemStack2)) continue;
            return merchantOffer;
        }
        return null;
    }

    public MerchantOffers copy() {
        MerchantOffers merchantOffers = new MerchantOffers(this.size());
        for (MerchantOffer merchantOffer : this) {
            merchantOffers.add(merchantOffer.copy());
        }
        return merchantOffers;
    }
}

