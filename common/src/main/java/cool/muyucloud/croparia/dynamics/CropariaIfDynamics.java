package cool.muyucloud.croparia.dynamics;


import com.mojang.logging.LogUtils;
import cool.muyucloud.croparia.dynamics.api.core.ServerProvider;
import cool.muyucloud.croparia.dynamics.api.elenet.ElenetTask;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public final class CropariaIfDynamics {
    public static final String MOD_ID = "croparia_dynamics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Write common init code here.
    }

    public static void onServerStarting(MinecraftServer server) {
        ServerProvider.set(server);
        ElenetTask.onServerStarting();
    }

    public static void onServerStopping(MinecraftServer server) {
        ElenetTask.onServerStopping();
        ServerProvider.set(null);
    }
}
