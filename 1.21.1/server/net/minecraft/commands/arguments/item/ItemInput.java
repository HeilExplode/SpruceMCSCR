/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput {
    private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("arguments.item.overstacked", object, object2));
    private final Holder<Item> item;
    private final DataComponentPatch components;

    public ItemInput(Holder<Item> holder, DataComponentPatch dataComponentPatch) {
        this.item = holder;
        this.components = dataComponentPatch;
    }

    public Item getItem() {
        return this.item.value();
    }

    public ItemStack createItemStack(int n, boolean bl) throws CommandSyntaxException {
        ItemStack itemStack = new ItemStack(this.item, n);
        itemStack.applyComponents(this.components);
        if (bl && n > itemStack.getMaxStackSize()) {
            throw ERROR_STACK_TOO_BIG.create((Object)this.getItemName(), (Object)itemStack.getMaxStackSize());
        }
        return itemStack;
    }

    public String serialize(HolderLookup.Provider provider) {
        StringBuilder stringBuilder = new StringBuilder(this.getItemName());
        String string = this.serializeComponents(provider);
        if (!string.isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(string);
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private String serializeComponents(HolderLookup.Provider provider) {
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        return this.components.entrySet().stream().flatMap(entry -> {
            DataComponentType dataComponentType = (DataComponentType)entry.getKey();
            ResourceLocation resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);
            if (resourceLocation == null) {
                return Stream.empty();
            }
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                TypedDataComponent typedDataComponent = TypedDataComponent.createUnchecked(dataComponentType, optional.get());
                return typedDataComponent.encodeValue(registryOps).result().stream().map(tag -> resourceLocation.toString() + "=" + String.valueOf(tag));
            }
            return Stream.of("!" + resourceLocation.toString());
        }).collect(Collectors.joining(String.valueOf(',')));
    }

    private String getItemName() {
        return this.item.unwrapKey().map(ResourceKey::location).orElseGet(() -> "unknown[" + String.valueOf(this.item) + "]").toString();
    }
}

