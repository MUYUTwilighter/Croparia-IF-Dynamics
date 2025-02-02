package cool.muyucloud.croparia.dynamics.fabric;

import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class CropariaIfDynamicsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CropariaIfDynamics.init();
        ServerLifecycleEvents.SERVER_STARTING.register(CropariaIfDynamics::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(CropariaIfDynamics::onServerStopping);
    }
}
