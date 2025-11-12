/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.trading;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

public class MerchantOffer {
    public static final Codec<MerchantOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemCost.CODEC.fieldOf("buy").forGetter(merchantOffer -> merchantOffer.baseCostA), (App)ItemCost.CODEC.lenientOptionalFieldOf("buyB").forGetter(merchantOffer -> merchantOffer.costB), (App)ItemStack.CODEC.fieldOf("sell").forGetter(merchantOffer -> merchantOffer.result), (App)Codec.INT.lenientOptionalFieldOf("uses", (Object)0).forGetter(merchantOffer -> merchantOffer.uses), (App)Codec.INT.lenientOptionalFieldOf("maxUses", (Object)4).forGetter(merchantOffer -> merchantOffer.maxUses), (App)Codec.BOOL.lenientOptionalFieldOf("rewardExp", (Object)true).forGetter(merchantOffer -> merchantOffer.rewardExp), (App)Codec.INT.lenientOptionalFieldOf("specialPrice", (Object)0).forGetter(merchantOffer -> merchantOffer.specialPriceDiff), (App)Codec.INT.lenientOptionalFieldOf("demand", (Object)0).forGetter(merchantOffer -> merchantOffer.demand), (App)Codec.FLOAT.lenientOptionalFieldOf("priceMultiplier", (Object)Float.valueOf(0.0f)).forGetter(merchantOffer -> Float.valueOf(merchantOffer.priceMultiplier)), (App)Codec.INT.lenientOptionalFieldOf("xp", (Object)1).forGetter(merchantOffer -> merchantOffer.xp)).apply((Applicative)instance, MerchantOffer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffer> STREAM_CODEC = StreamCodec.of(MerchantOffer::writeToStream, MerchantOffer::createFromStream);
    private final ItemCost baseCostA;
    private final Optional<ItemCost> costB;
    private final ItemStack result;
    private int uses;
    private final int maxUses;
    private final boolean rewardExp;
    private int specialPriceDiff;
    private int demand;
    private final float priceMultiplier;
    private final int xp;

    private MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int n, int n2, boolean bl, int n3, int n4, float f, int n5) {
        this.baseCostA = itemCost;
        this.costB = optional;
        this.result = itemStack;
        this.uses = n;
        this.maxUses = n2;
        this.rewardExp = bl;
        this.specialPriceDiff = n3;
        this.demand = n4;
        this.priceMultiplier = f;
        this.xp = n5;
    }

    public MerchantOffer(ItemCost itemCost, ItemStack itemStack, int n, int n2, float f) {
        this(itemCost, Optional.empty(), itemStack, n, n2, f);
    }

    public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int n, int n2, float f) {
        this(itemCost, optional, itemStack, 0, n, n2, f);
    }

    public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int n, int n2, int n3, float f) {
        this(itemCost, optional, itemStack, n, n2, n3, f, 0);
    }

    public MerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int n, int n2, int n3, float f, int n4) {
        this(itemCost, optional, itemStack, n, n2, true, 0, n4, f, n3);
    }

    private MerchantOffer(MerchantOffer merchantOffer) {
        this(merchantOffer.baseCostA, merchantOffer.costB, merchantOffer.result.copy(), merchantOffer.uses, merchantOffer.maxUses, merchantOffer.rewardExp, merchantOffer.specialPriceDiff, merchantOffer.demand, merchantOffer.priceMultiplier, merchantOffer.xp);
    }

    public ItemStack getBaseCostA() {
        return this.baseCostA.itemStack();
    }

    public ItemStack getCostA() {
        return this.baseCostA.itemStack().copyWithCount(this.getModifiedCostCount(this.baseCostA));
    }

    private int getModifiedCostCount(ItemCost itemCost) {
        int n = itemCost.count();
        int n2 = Math.max(0, Mth.floor((float)(n * this.demand) * this.priceMultiplier));
        return Mth.clamp(n + n2 + this.specialPriceDiff, 1, itemCost.itemStack().getMaxStackSize());
    }

    public ItemStack getCostB() {
        return this.costB.map(ItemCost::itemStack).orElse(ItemStack.EMPTY);
    }

    public ItemCost getItemCostA() {
        return this.baseCostA;
    }

    public Optional<ItemCost> getItemCostB() {
        return this.costB;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public void updateDemand() {
        this.demand = this.demand + this.uses - (this.maxUses - this.uses);
    }

    public ItemStack assemble() {
        return this.result.copy();
    }

    public int getUses() {
        return this.uses;
    }

    public void resetUses() {
        this.uses = 0;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void increaseUses() {
        ++this.uses;
    }

    public int getDemand() {
        return this.demand;
    }

    public void addToSpecialPriceDiff(int n) {
        this.specialPriceDiff += n;
    }

    public void resetSpecialPriceDiff() {
        this.specialPriceDiff = 0;
    }

    public int getSpecialPriceDiff() {
        return this.specialPriceDiff;
    }

    public void setSpecialPriceDiff(int n) {
        this.specialPriceDiff = n;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getXp() {
        return this.xp;
    }

    public boolean isOutOfStock() {
        return this.uses >= this.maxUses;
    }

    public void setToOutOfStock() {
        this.uses = this.maxUses;
    }

    public boolean needsRestock() {
        return this.uses > 0;
    }

    public boolean shouldRewardExp() {
        return this.rewardExp;
    }

    public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.baseCostA.test(itemStack) || itemStack.getCount() < this.getModifiedCostCount(this.baseCostA)) {
            return false;
        }
        if (this.costB.isPresent()) {
            return this.costB.get().test(itemStack2) && itemStack2.getCount() >= this.costB.get().count();
        }
        return itemStack2.isEmpty();
    }

    public boolean take(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.satisfiedBy(itemStack, itemStack2)) {
            return false;
        }
        itemStack.shrink(this.getCostA().getCount());
        if (!this.getCostB().isEmpty()) {
            itemStack2.shrink(this.getCostB().getCount());
        }
        return true;
    }

    public MerchantOffer copy() {
        return new MerchantOffer(this);
    }

    private static void writeToStream(RegistryFriendlyByteBuf registryFriendlyByteBuf, MerchantOffer merchantOffer) {
        ItemCost.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getItemCostA());
        ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getResult());
        ItemCost.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, merchantOffer.getItemCostB());
        registryFriendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
        registryFriendlyByteBuf.writeInt(merchantOffer.getUses());
        registryFriendlyByteBuf.writeInt(merchantOffer.getMaxUses());
        registryFriendlyByteBuf.writeInt(merchantOffer.getXp());
        registryFriendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
        registryFriendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
        registryFriendlyByteBuf.writeInt(merchantOffer.getDemand());
    }

    public static MerchantOffer createFromStream(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        ItemCost itemCost = (ItemCost)ItemCost.STREAM_CODEC.decode(registryFriendlyByteBuf);
        ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
        Optional optional = (Optional)ItemCost.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
        boolean bl = registryFriendlyByteBuf.readBoolean();
        int n = registryFriendlyByteBuf.readInt();
        int n2 = registryFriendlyByteBuf.readInt();
        int n3 = registryFriendlyByteBuf.readInt();
        int n4 = registryFriendlyByteBuf.readInt();
        float f = registryFriendlyByteBuf.readFloat();
        int n5 = registryFriendlyByteBuf.readInt();
        MerchantOffer merchantOffer = new MerchantOffer(itemCost, optional, itemStack, n, n2, n3, f, n5);
        if (bl) {
            merchantOffer.setToOutOfStock();
        }
        merchantOffer.setSpecialPriceDiff(n4);
        return merchantOffer;
    }
}

