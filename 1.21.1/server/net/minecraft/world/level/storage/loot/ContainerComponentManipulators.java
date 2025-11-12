/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;

public interface ContainerComponentManipulators {
    public static final ContainerComponentManipulator<ItemContainerContents> CONTAINER = new ContainerComponentManipulator<ItemContainerContents>(){

        @Override
        public DataComponentType<ItemContainerContents> type() {
            return DataComponents.CONTAINER;
        }

        @Override
        public Stream<ItemStack> getContents(ItemContainerContents itemContainerContents) {
            return itemContainerContents.stream();
        }

        @Override
        public ItemContainerContents empty() {
            return ItemContainerContents.EMPTY;
        }

        @Override
        public ItemContainerContents setContents(ItemContainerContents itemContainerContents, Stream<ItemStack> stream) {
            return ItemContainerContents.fromItems(stream.toList());
        }

        @Override
        public /* synthetic */ Object empty() {
            return this.empty();
        }
    };
    public static final ContainerComponentManipulator<BundleContents> BUNDLE_CONTENTS = new ContainerComponentManipulator<BundleContents>(){

        @Override
        public DataComponentType<BundleContents> type() {
            return DataComponents.BUNDLE_CONTENTS;
        }

        @Override
        public BundleContents empty() {
            return BundleContents.EMPTY;
        }

        @Override
        public Stream<ItemStack> getContents(BundleContents bundleContents) {
            return bundleContents.itemCopyStream();
        }

        @Override
        public BundleContents setContents(BundleContents bundleContents, Stream<ItemStack> stream) {
            BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents).clearItems();
            stream.forEach(mutable::tryInsert);
            return mutable.toImmutable();
        }

        @Override
        public /* synthetic */ Object empty() {
            return this.empty();
        }
    };
    public static final ContainerComponentManipulator<ChargedProjectiles> CHARGED_PROJECTILES = new ContainerComponentManipulator<ChargedProjectiles>(){

        @Override
        public DataComponentType<ChargedProjectiles> type() {
            return DataComponents.CHARGED_PROJECTILES;
        }

        @Override
        public ChargedProjectiles empty() {
            return ChargedProjectiles.EMPTY;
        }

        @Override
        public Stream<ItemStack> getContents(ChargedProjectiles chargedProjectiles) {
            return chargedProjectiles.getItems().stream();
        }

        @Override
        public ChargedProjectiles setContents(ChargedProjectiles chargedProjectiles, Stream<ItemStack> stream) {
            return ChargedProjectiles.of(stream.toList());
        }

        @Override
        public /* synthetic */ Object empty() {
            return this.empty();
        }
    };
    public static final Map<DataComponentType<?>, ContainerComponentManipulator<?>> ALL_MANIPULATORS = Stream.of(CONTAINER, BUNDLE_CONTENTS, CHARGED_PROJECTILES).collect(Collectors.toMap(ContainerComponentManipulator::type, containerComponentManipulator -> containerComponentManipulator));
    public static final Codec<ContainerComponentManipulator<?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap(dataComponentType -> {
        ContainerComponentManipulator<?> containerComponentManipulator = ALL_MANIPULATORS.get(dataComponentType);
        return containerComponentManipulator != null ? DataResult.success(containerComponentManipulator) : DataResult.error(() -> "No items in component");
    }, ContainerComponentManipulator::type);
}

