package cool.muyucloud.croparia.dynamics.forge;

import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.core.ServerProvider;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CropariaIfDynamics.MOD_ID)
@Mod.EventBusSubscriber(modid = CropariaIfDynamics.MOD_ID)
public final class CropariaIfDynamicsForge {
    public CropariaIfDynamicsForge() {
        EventBuses.registerModEventBus(CropariaIfDynamics.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CropariaIfDynamics.init();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ServerProvider.set(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerProvider.set(null);
    }
}
