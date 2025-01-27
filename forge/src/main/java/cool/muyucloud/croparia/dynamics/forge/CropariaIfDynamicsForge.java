package cool.muyucloud.croparia.dynamics.forge;

import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CropariaIfDynamics.MOD_ID)
public final class CropariaIfDynamicsForge {
    public CropariaIfDynamicsForge() {
        EventBuses.registerModEventBus(CropariaIfDynamics.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CropariaIfDynamics.init();
    }
}
