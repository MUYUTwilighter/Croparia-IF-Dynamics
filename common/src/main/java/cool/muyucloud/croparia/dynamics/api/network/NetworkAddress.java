package cool.muyucloud.croparia.dynamics.api.network;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

@SuppressWarnings("unused")
public record NetworkAddress(ResourceLocation world, BlockPos pos, Direction side) {
    public static final MapCodec<NetworkAddress> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("world").forGetter(NetworkAddress::world),
        BlockPos.CODEC.fieldOf("pos").forGetter(NetworkAddress::pos),
        Direction.CODEC.fieldOf("side").forGetter(NetworkAddress::side)
    ).apply(instance, NetworkAddress::new));

    public static NetworkAddress of(ResourceLocation world, BlockPos pos, Direction side) {
        return new NetworkAddress(world, pos, side);
    }

    public static NetworkAddress of(ServerLevel world, BlockPos pos, Direction side) {
        ResourceLocation id = world.dimension().location();
        return new NetworkAddress(id, pos, side);
    }

    public ServerLevel getWorld(MinecraftServer server) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, world));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkAddress that)) return false;
        return Objects.equals(pos, that.pos) && side == that.side && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, pos, side);
    }

    @Override
    public String toString() {
        return this.world().getNamespace() + ":" + this.world().getPath()
            + ":" + this.pos().getX() + ":" + this.pos().getY() + ":" + this.pos().getZ()
            + ":" + this.side();
    }
}
