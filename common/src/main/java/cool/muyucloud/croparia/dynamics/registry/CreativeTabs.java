package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.util.Constants;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public class CreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(CropariaIf.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> DYNAMICS = register(
        "dynamics", () -> CreativeTabRegistry.create(Constants.TAB_DYNAMICS, Items.APPLE::getDefaultInstance)
    );

    public static RegistrySupplier<CreativeModeTab> register(String id, Supplier<CreativeModeTab> supplier) {
        return TABS.register(id, supplier);
    }

    public static void register() {
        TABS.register();
    }
}
