package cool.muyucloud.croparia.dynamics.api.core;

import cool.muyucloud.croparia.dynamics.annotation.ServerOnly;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

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
}
