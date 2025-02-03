package cool.muyucloud.croparia.dynamics.api.core;

import cool.muyucloud.croparia.dynamics.annotation.ServerOnly;
import cool.muyucloud.croparia.dynamics.api.elenet.ElenetAddress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

@SuppressWarnings("unused")
public class ServerProvider {
    private static MinecraftServer SERVER = null;

    public static Optional<MinecraftServer> get() {
        if (SERVER == null || !SERVER.isRunning()) {
            return Optional.empty();
        } else {
            return Optional.of(SERVER);
        }
    }

    @ServerOnly
    public static MinecraftServer getOrThrow() {
        return get().orElseThrow(() -> new IllegalStateException("Server is not running, or I am in the client!"));
    }

    public static void set(MinecraftServer server) {
        SERVER = server;
    }

    public static Optional<BlockEntity> getBlockEntity(ElenetAddress address) {
        if (SERVER == null) {
            return Optional.empty();
        } else {
            try (Level world = address.world()) {
                return Optional.ofNullable(world.getBlockEntity(address.pos()));
            } catch (Throwable t) {
                return Optional.empty();
            }
        }
    }
}
