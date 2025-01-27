package cool.muyucloud.croparia.dynamics.fabric;

import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import net.fabricmc.api.ModInitializer;

public final class CropariaIfDynamicsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CropariaIfDynamics.init();
    }
}
