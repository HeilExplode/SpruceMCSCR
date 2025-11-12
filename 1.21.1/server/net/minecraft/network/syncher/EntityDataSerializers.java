/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.network.syncher;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityDataSerializers {
    private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
    public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.forValueType(ByteBufCodecs.BYTE);
    public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.forValueType(ByteBufCodecs.VAR_INT);
    public static final EntityDataSerializer<Long> LONG = EntityDataSerializer.forValueType(ByteBufCodecs.VAR_LONG);
    public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.forValueType(ByteBufCodecs.FLOAT);
    public static final EntityDataSerializer<String> STRING = EntityDataSerializer.forValueType(ByteBufCodecs.STRING_UTF8);
    public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.forValueType(ComponentSerialization.TRUSTED_STREAM_CODEC);
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.forValueType(ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC);
    public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>(){

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemStack> codec() {
            return ItemStack.OPTIONAL_STREAM_CODEC;
        }

        @Override
        public ItemStack copy(ItemStack itemStack) {
            return itemStack.copy();
        }

        @Override
        public /* synthetic */ Object copy(Object object) {
            return this.copy((ItemStack)object);
        }
    };
    public static final EntityDataSerializer<BlockState> BLOCK_STATE = EntityDataSerializer.forValueType(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));
    private static final StreamCodec<ByteBuf, Optional<BlockState>> OPTIONAL_BLOCK_STATE_CODEC = new StreamCodec<ByteBuf, Optional<BlockState>>(){

        @Override
        public void encode(ByteBuf byteBuf, Optional<BlockState> optional) {
            if (optional.isPresent()) {
                VarInt.write(byteBuf, Block.getId(optional.get()));
            } else {
                VarInt.write(byteBuf, 0);
            }
        }

        @Override
        public Optional<BlockState> decode(ByteBuf byteBuf) {
            int n = VarInt.read(byteBuf);
            if (n == 0) {
                return Optional.empty();
            }
            return Optional.of(Block.stateById(n));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final EntityDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = EntityDataSerializer.forValueType(OPTIONAL_BLOCK_STATE_CODEC);
    public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.forValueType(ByteBufCodecs.BOOL);
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = EntityDataSerializer.forValueType(ParticleTypes.STREAM_CODEC);
    public static final EntityDataSerializer<List<ParticleOptions>> PARTICLES = EntityDataSerializer.forValueType(ParticleTypes.STREAM_CODEC.apply(ByteBufCodecs.list()));
    public static final EntityDataSerializer<Rotations> ROTATIONS = EntityDataSerializer.forValueType(Rotations.STREAM_CODEC);
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.forValueType(BlockPos.STREAM_CODEC);
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.forValueType(BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.forValueType(Direction.STREAM_CODEC);
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.forValueType(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>(){

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
            return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
        }

        @Override
        public CompoundTag copy(CompoundTag compoundTag) {
            return compoundTag.copy();
        }

        @Override
        public /* synthetic */ Object copy(Object object) {
            return this.copy((CompoundTag)object);
        }
    };
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = EntityDataSerializer.forValueType(VillagerData.STREAM_CODEC);
    private static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_UNSIGNED_INT_CODEC = new StreamCodec<ByteBuf, OptionalInt>(){

        @Override
        public OptionalInt decode(ByteBuf byteBuf) {
            int n = VarInt.read(byteBuf);
            return n == 0 ? OptionalInt.empty() : OptionalInt.of(n - 1);
        }

        @Override
        public void encode(ByteBuf byteBuf, OptionalInt optionalInt) {
            VarInt.write(byteBuf, optionalInt.orElse(-1) + 1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (OptionalInt)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = EntityDataSerializer.forValueType(OPTIONAL_UNSIGNED_INT_CODEC);
    public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.forValueType(Pose.STREAM_CODEC);
    public static final EntityDataSerializer<Holder<CatVariant>> CAT_VARIANT = EntityDataSerializer.forValueType(CatVariant.STREAM_CODEC);
    public static final EntityDataSerializer<Holder<WolfVariant>> WOLF_VARIANT = EntityDataSerializer.forValueType(WolfVariant.STREAM_CODEC);
    public static final EntityDataSerializer<Holder<FrogVariant>> FROG_VARIANT = EntityDataSerializer.forValueType(FrogVariant.STREAM_CODEC);
    public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.forValueType(PaintingVariant.STREAM_CODEC);
    public static final EntityDataSerializer<Armadillo.ArmadilloState> ARMADILLO_STATE = EntityDataSerializer.forValueType(Armadillo.ArmadilloState.STREAM_CODEC);
    public static final EntityDataSerializer<Sniffer.State> SNIFFER_STATE = EntityDataSerializer.forValueType(Sniffer.State.STREAM_CODEC);
    public static final EntityDataSerializer<Vector3f> VECTOR3 = EntityDataSerializer.forValueType(ByteBufCodecs.VECTOR3F);
    public static final EntityDataSerializer<Quaternionf> QUATERNION = EntityDataSerializer.forValueType(ByteBufCodecs.QUATERNIONF);

    public static void registerSerializer(EntityDataSerializer<?> entityDataSerializer) {
        SERIALIZERS.add(entityDataSerializer);
    }

    @Nullable
    public static EntityDataSerializer<?> getSerializer(int n) {
        return SERIALIZERS.byId(n);
    }

    public static int getSerializedId(EntityDataSerializer<?> entityDataSerializer) {
        return SERIALIZERS.getId(entityDataSerializer);
    }

    private EntityDataSerializers() {
    }

    static {
        EntityDataSerializers.registerSerializer(BYTE);
        EntityDataSerializers.registerSerializer(INT);
        EntityDataSerializers.registerSerializer(LONG);
        EntityDataSerializers.registerSerializer(FLOAT);
        EntityDataSerializers.registerSerializer(STRING);
        EntityDataSerializers.registerSerializer(COMPONENT);
        EntityDataSerializers.registerSerializer(OPTIONAL_COMPONENT);
        EntityDataSerializers.registerSerializer(ITEM_STACK);
        EntityDataSerializers.registerSerializer(BOOLEAN);
        EntityDataSerializers.registerSerializer(ROTATIONS);
        EntityDataSerializers.registerSerializer(BLOCK_POS);
        EntityDataSerializers.registerSerializer(OPTIONAL_BLOCK_POS);
        EntityDataSerializers.registerSerializer(DIRECTION);
        EntityDataSerializers.registerSerializer(OPTIONAL_UUID);
        EntityDataSerializers.registerSerializer(BLOCK_STATE);
        EntityDataSerializers.registerSerializer(OPTIONAL_BLOCK_STATE);
        EntityDataSerializers.registerSerializer(COMPOUND_TAG);
        EntityDataSerializers.registerSerializer(PARTICLE);
        EntityDataSerializers.registerSerializer(PARTICLES);
        EntityDataSerializers.registerSerializer(VILLAGER_DATA);
        EntityDataSerializers.registerSerializer(OPTIONAL_UNSIGNED_INT);
        EntityDataSerializers.registerSerializer(POSE);
        EntityDataSerializers.registerSerializer(CAT_VARIANT);
        EntityDataSerializers.registerSerializer(WOLF_VARIANT);
        EntityDataSerializers.registerSerializer(FROG_VARIANT);
        EntityDataSerializers.registerSerializer(OPTIONAL_GLOBAL_POS);
        EntityDataSerializers.registerSerializer(PAINTING_VARIANT);
        EntityDataSerializers.registerSerializer(SNIFFER_STATE);
        EntityDataSerializers.registerSerializer(ARMADILLO_STATE);
        EntityDataSerializers.registerSerializer(VECTOR3);
        EntityDataSerializers.registerSerializer(QUATERNION);
    }
}

