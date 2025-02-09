package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.annotation.ClientOnly;
import cool.muyucloud.croparia.dynamics.annotation.ServerOnly;
import cool.muyucloud.croparia.dynamics.api.core.util.ServerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public record ElenetAddress(Level world, BlockPos pos) {
    public static final MapCodec<ElenetAddress> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("world").forGetter(address -> {
            try (Level world = address.world()) {
                return world.dimension().location();
            } catch (Throwable t) {
                throw new IllegalStateException("World not available", t);
            }
        }),
        BlockPos.CODEC.fieldOf("pos").forGetter(ElenetAddress::pos)
    ).apply(instance, ElenetAddress::of));

    @ClientOnly
    public static ElenetAddress ofClient(@NotNull BlockPos pos) {
        return new ElenetAddress(Minecraft.getInstance().level, pos);
    }

    @ServerOnly
    public static ElenetAddress of(@NotNull ResourceLocation worldId, @NotNull BlockPos pos) {
        try (MinecraftServer server = ServerProvider.getOrThrow()) {
            ServerLevel world = server.getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
            if (world == null) {
                throw new IllegalArgumentException("Unknown world: " + worldId);
            }
            return new ElenetAddress(world, pos);
        }
    }

    public static ElenetAddress of(@NotNull Level world, @NotNull BlockPos pos) {
        return new ElenetAddress(world, pos);
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
        return Objects.equals(world, that.world) && Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, pos);
    }

    public boolean isInRangeWith(ElenetAddress other, int range) {
        return ElenetAddress.chebyshev(this, other) <= range;
    }

    public int chebyshev(ElenetAddress other) {
        return ElenetAddress.chebyshev(this, other);
    }

    public Optional<ElenetHub<?>> tryGetHub() {
        try (Level world = this.world()) {
            if (world.isLoaded(this.pos())) {
                BlockEntity be = world.getBlockEntity(this.pos());
                if (be instanceof ElenetHubProvider provider) {
                    return provider.getHub(this);
                }
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    public Optional<ElenetHub<?>> getHub() {
        try (Level world = this.world()) {
            BlockEntity be = world.getBlockEntity(this.pos());
            if (be instanceof ElenetHubProvider provider) {
                return provider.getHub(this);
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    public Optional<ElenetPeer> tryGetPeer() {
        try (Level world = this.world()) {
            if (world.isLoaded(this.pos())) {
                BlockEntity be = world.getBlockEntity(this.pos());
                if (be instanceof ElenetPeerProvider provider) {
                    return provider.getPeer(this);
                }
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    public Optional<ElenetPeer> getPeer() {
        try (Level world = this.world()) {
            BlockEntity be = world.getBlockEntity(this.pos());
            if (be instanceof ElenetPeerProvider provider) {
                return provider.getPeer(this);
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return this.worldId().getNamespace().toUpperCase() + ":" + this.worldId().getPath().toUpperCase()
            + ":" + (this.pos().getX() < 0 ? "N" + Math.abs(this.pos().getX()) : this.pos().getX())
            + ":" + (this.pos().getY() < 0 ? "N" + Math.abs(this.pos().getY()) : this.pos().getY())
            + ":" + (this.pos().getZ() < 0 ? "N" + Math.abs(this.pos().getZ()) : this.pos().getZ());
    }

    public static int chebyshev(ElenetAddress a, ElenetAddress b) {
        return Objects.equals(a.world(), b.world()) ? Math.abs(a.pos().getX() - b.pos().getX()) + Math.abs(a.pos().getY() - b.pos().getY()) + Math.abs(a.pos().getZ() - b.pos().getZ()) : Integer.MAX_VALUE;
    }
}
