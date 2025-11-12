/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
    public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemStack.STRICT_CODEC.fieldOf("icon").forGetter(DisplayInfo::getIcon), (App)ComponentSerialization.CODEC.fieldOf("title").forGetter(DisplayInfo::getTitle), (App)ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::getDescription), (App)ResourceLocation.CODEC.optionalFieldOf("background").forGetter(DisplayInfo::getBackground), (App)AdvancementType.CODEC.optionalFieldOf("frame", (Object)AdvancementType.TASK).forGetter(DisplayInfo::getType), (App)Codec.BOOL.optionalFieldOf("show_toast", (Object)true).forGetter(DisplayInfo::shouldShowToast), (App)Codec.BOOL.optionalFieldOf("announce_to_chat", (Object)true).forGetter(DisplayInfo::shouldAnnounceChat), (App)Codec.BOOL.optionalFieldOf("hidden", (Object)false).forGetter(DisplayInfo::isHidden)).apply((Applicative)instance, DisplayInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(DisplayInfo::serializeToNetwork, DisplayInfo::fromNetwork);
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private final Optional<ResourceLocation> background;
    private final AdvancementType type;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public DisplayInfo(ItemStack itemStack, Component component, Component component2, Optional<ResourceLocation> optional, AdvancementType advancementType, boolean bl, boolean bl2, boolean bl3) {
        this.title = component;
        this.description = component2;
        this.icon = itemStack;
        this.background = optional;
        this.type = advancementType;
        this.showToast = bl;
        this.announceChat = bl2;
        this.hidden = bl3;
    }

    public void setLocation(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public Optional<ResourceLocation> getBackground() {
        return this.background;
    }

    public AdvancementType getType() {
        return this.type;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean shouldShowToast() {
        return this.showToast;
    }

    public boolean shouldAnnounceChat() {
        return this.announceChat;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    private void serializeToNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.title);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.description);
        ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, this.icon);
        registryFriendlyByteBuf.writeEnum(this.type);
        int n = 0;
        if (this.background.isPresent()) {
            n |= 1;
        }
        if (this.showToast) {
            n |= 2;
        }
        if (this.hidden) {
            n |= 4;
        }
        registryFriendlyByteBuf.writeInt(n);
        this.background.ifPresent(registryFriendlyByteBuf::writeResourceLocation);
        registryFriendlyByteBuf.writeFloat(this.x);
        registryFriendlyByteBuf.writeFloat(this.y);
    }

    private static DisplayInfo fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        Component component = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
        Component component2 = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
        ItemStack itemStack = (ItemStack)ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
        AdvancementType advancementType = registryFriendlyByteBuf.readEnum(AdvancementType.class);
        int n = registryFriendlyByteBuf.readInt();
        Optional<ResourceLocation> optional = (n & 1) != 0 ? Optional.of(registryFriendlyByteBuf.readResourceLocation()) : Optional.empty();
        boolean bl = (n & 2) != 0;
        boolean bl2 = (n & 4) != 0;
        DisplayInfo displayInfo = new DisplayInfo(itemStack, component, component2, optional, advancementType, bl, false, bl2);
        displayInfo.setLocation(registryFriendlyByteBuf.readFloat(), registryFriendlyByteBuf.readFloat());
        return displayInfo;
    }
}

