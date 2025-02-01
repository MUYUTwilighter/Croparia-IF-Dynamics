package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.annotation.ClientOnly;
import cool.muyucloud.croparia.dynamics.annotation.ServerOnly;
import cool.muyucloud.croparia.dynamics.api.core.ServerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public record ElenetAddress(Level world, BlockPos pos, Direction side) {
    public static final MapCodec<ElenetAddress> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("world").forGetter(address -> {
            try (Level world = address.world()) {
                return world.dimension().location();
            } catch (Throwable t) {
                throw new IllegalStateException("World not available", t);
            }
        }),
        BlockPos.CODEC.fieldOf("pos").forGetter(ElenetAddress::pos),
        Direction.CODEC.fieldOf("side").forGetter(ElenetAddress::side)
    ).apply(instance, ElenetAddress::of));

    @ClientOnly
    public static ElenetAddress ofClient(BlockPos pos, Direction side) {
        return new ElenetAddress(Minecraft.getInstance().level, pos, side);
    }

    @ServerOnly
    public static ElenetAddress of(ResourceLocation worldId, BlockPos pos, Direction side) {
        try (MinecraftServer server = ServerProvider.getOrThrow()) {
            ServerLevel world = server.getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
            if (world == null) {
                throw new IllegalArgumentException("Unknown world: " + worldId);
            }
            return new ElenetAddress(world, pos, side);
        }
    }

    public static ElenetAddress of(Level world, BlockPos pos, Direction side) {
        return new ElenetAddress(world, pos, side);
    }

    @NotNull
    public ResourceLocation worldId() {
        try (Level world = this.world()) {
            return world.dimension().location();
        } catch (Throwable t) {
            throw new IllegalStateException("World not available", t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElenetAddress that)) return false;
        return Objects.equals(pos, that.pos) && side == that.side && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, pos, side);
    }

    @Override
    public String toString() {
        return this.worldId().getNamespace() + ":" + this.worldId().getPath()
            + ":" + this.pos().getX() + ":" + this.pos().getY() + ":" + this.pos().getZ()
            + ":" + this.side();
    }
}
