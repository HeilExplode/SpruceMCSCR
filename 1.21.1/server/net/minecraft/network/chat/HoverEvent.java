/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoverEvent {
    public static final Codec<HoverEvent> CODEC = Codec.withAlternative((Codec)TypedHoverEvent.CODEC.codec(), (Codec)TypedHoverEvent.LEGACY_CODEC.codec()).xmap(HoverEvent::new, hoverEvent -> hoverEvent.event);
    private final TypedHoverEvent<?> event;

    public <T> HoverEvent(Action<T> action, T t) {
        this(new TypedHoverEvent<T>(action, t));
    }

    private HoverEvent(TypedHoverEvent<?> typedHoverEvent) {
        this.event = typedHoverEvent;
    }

    public Action<?> getAction() {
        return this.event.action;
    }

    @Nullable
    public <T> T getValue(Action<T> action) {
        if (this.event.action == action) {
            return action.cast(this.event.value);
        }
        return null;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        return ((HoverEvent)object).event.equals(this.event);
    }

    public String toString() {
        return this.event.toString();
    }

    public int hashCode() {
        return this.event.hashCode();
    }

    static final class TypedHoverEvent<T>
    extends Record {
        final Action<T> action;
        final T value;
        public static final MapCodec<TypedHoverEvent<?>> CODEC = Action.CODEC.dispatchMap("action", TypedHoverEvent::action, action -> action.codec);
        public static final MapCodec<TypedHoverEvent<?>> LEGACY_CODEC = Action.CODEC.dispatchMap("action", TypedHoverEvent::action, action -> action.legacyCodec);

        TypedHoverEvent(Action<T> action, T t) {
            this.action = action;
            this.value = t;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TypedHoverEvent.class, "action;value", "action", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TypedHoverEvent.class, "action;value", "action", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TypedHoverEvent.class, "action;value", "action", "value"}, this, object);
        }

        public Action<T> action() {
            return this.action;
        }

        public T value() {
            return this.value;
        }
    }

    public static class Action<T>
    implements StringRepresentable {
        public static final Action<Component> SHOW_TEXT = new Action<Component>("show_text", true, ComponentSerialization.CODEC, (component, registryOps) -> DataResult.success((Object)component));
        public static final Action<ItemStackInfo> SHOW_ITEM = new Action<ItemStackInfo>("show_item", true, ItemStackInfo.CODEC, ItemStackInfo::legacyCreate);
        public static final Action<EntityTooltipInfo> SHOW_ENTITY = new Action<EntityTooltipInfo>("show_entity", true, EntityTooltipInfo.CODEC, EntityTooltipInfo::legacyCreate);
        public static final Codec<Action<?>> UNSAFE_CODEC = StringRepresentable.fromValues(() -> new Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY});
        public static final Codec<Action<?>> CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
        private final String name;
        private final boolean allowFromServer;
        final MapCodec<TypedHoverEvent<T>> codec;
        final MapCodec<TypedHoverEvent<T>> legacyCodec;

        public Action(String string, boolean bl, Codec<T> codec, final LegacyConverter<T> legacyConverter) {
            this.name = string;
            this.allowFromServer = bl;
            this.codec = codec.xmap(object -> new TypedHoverEvent<Object>(this, object), typedHoverEvent -> typedHoverEvent.value).fieldOf("contents");
            this.legacyCodec = new Codec<TypedHoverEvent<T>>(){

                public <D> DataResult<Pair<TypedHoverEvent<T>, D>> decode(DynamicOps<D> dynamicOps, D d) {
                    return ComponentSerialization.CODEC.decode(dynamicOps, d).flatMap(pair -> {
                        DataResult dataResult;
                        if (dynamicOps instanceof RegistryOps) {
                            RegistryOps registryOps = (RegistryOps)dynamicOps;
                            dataResult = legacyConverter.parse((Component)pair.getFirst(), registryOps);
                        } else {
                            dataResult = legacyConverter.parse((Component)pair.getFirst(), null);
                        }
                        return dataResult.map(object -> Pair.of(new TypedHoverEvent<Object>(this, object), (Object)pair.getSecond()));
                    });
                }

                public <D> DataResult<D> encode(TypedHoverEvent<T> typedHoverEvent, DynamicOps<D> dynamicOps, D d) {
                    return DataResult.error(() -> "Can't encode in legacy format");
                }

                public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                    return this.encode((TypedHoverEvent)object, dynamicOps, object2);
                }
            }.fieldOf("value");
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        T cast(Object object) {
            return (T)object;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action<?>> filterForSerialization(@Nullable Action<?> action) {
            if (action == null) {
                return DataResult.error(() -> "Unknown action");
            }
            if (!action.isAllowedFromServer()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success(action, (Lifecycle)Lifecycle.stable());
        }
    }

    public static interface LegacyConverter<T> {
        public DataResult<T> parse(Component var1, @Nullable RegistryOps<?> var2);
    }

    public static class ItemStackInfo {
        public static final Codec<ItemStackInfo> FULL_CODEC = ItemStack.CODEC.xmap(ItemStackInfo::new, ItemStackInfo::getItemStack);
        private static final Codec<ItemStackInfo> SIMPLE_CODEC = ItemStack.SIMPLE_ITEM_CODEC.xmap(ItemStackInfo::new, ItemStackInfo::getItemStack);
        public static final Codec<ItemStackInfo> CODEC = Codec.withAlternative(FULL_CODEC, SIMPLE_CODEC);
        private final Holder<Item> item;
        private final int count;
        private final DataComponentPatch components;
        @Nullable
        private ItemStack itemStack;

        ItemStackInfo(Holder<Item> holder, int n, DataComponentPatch dataComponentPatch) {
            this.item = holder;
            this.count = n;
            this.components = dataComponentPatch;
        }

        public ItemStackInfo(ItemStack itemStack) {
            this(itemStack.getItemHolder(), itemStack.getCount(), itemStack.getComponentsPatch());
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            ItemStackInfo itemStackInfo = (ItemStackInfo)object;
            return this.count == itemStackInfo.count && this.item.equals(itemStackInfo.item) && this.components.equals(itemStackInfo.components);
        }

        public int hashCode() {
            int n = this.item.hashCode();
            n = 31 * n + this.count;
            n = 31 * n + this.components.hashCode();
            return n;
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count, this.components);
            }
            return this.itemStack;
        }

        private static DataResult<ItemStackInfo> legacyCreate(Component component, @Nullable RegistryOps<?> registryOps) {
            try {
                CompoundTag compoundTag = TagParser.parseTag(component.getString());
                NbtOps nbtOps = registryOps != null ? registryOps.withParent(NbtOps.INSTANCE) : NbtOps.INSTANCE;
                return ItemStack.CODEC.parse((DynamicOps)nbtOps, (Object)compoundTag).map(ItemStackInfo::new);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                return DataResult.error(() -> "Failed to parse item tag: " + commandSyntaxException.getMessage());
            }
        }
    }

    public static class EntityTooltipInfo {
        public static final Codec<EntityTooltipInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(entityTooltipInfo -> entityTooltipInfo.type), (App)UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter(entityTooltipInfo -> entityTooltipInfo.id), (App)ComponentSerialization.CODEC.lenientOptionalFieldOf("name").forGetter(entityTooltipInfo -> entityTooltipInfo.name)).apply((Applicative)instance, EntityTooltipInfo::new));
        public final EntityType<?> type;
        public final UUID id;
        public final Optional<Component> name;
        @Nullable
        private List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, @Nullable Component component) {
            this(entityType, uUID, Optional.ofNullable(component));
        }

        public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, Optional<Component> optional) {
            this.type = entityType;
            this.id = uUID;
            this.name = optional;
        }

        public static DataResult<EntityTooltipInfo> legacyCreate(Component component2, @Nullable RegistryOps<?> registryOps) {
            try {
                CompoundTag compoundTag = TagParser.parseTag(component2.getString());
                JsonOps jsonOps = registryOps != null ? registryOps.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE;
                DataResult dataResult = ComponentSerialization.CODEC.parse((DynamicOps)jsonOps, (Object)JsonParser.parseString((String)compoundTag.getString("name")));
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(compoundTag.getString("type")));
                UUID uUID = UUID.fromString(compoundTag.getString("id"));
                return dataResult.map(component -> new EntityTooltipInfo(entityType, uUID, (Component)component));
            }
            catch (Exception exception) {
                return DataResult.error(() -> "Failed to parse tooltip: " + exception.getMessage());
            }
        }

        public List<Component> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList<Component>();
                this.name.ifPresent(this.linesCache::add);
                this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(Component.literal(this.id.toString()));
            }
            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            EntityTooltipInfo entityTooltipInfo = (EntityTooltipInfo)object;
            return this.type.equals(entityTooltipInfo.type) && this.id.equals(entityTooltipInfo.id) && this.name.equals(entityTooltipInfo.name);
        }

        public int hashCode() {
            int n = this.type.hashCode();
            n = 31 * n + this.id.hashCode();
            n = 31 * n + this.name.hashCode();
            return n;
        }
    }
}

