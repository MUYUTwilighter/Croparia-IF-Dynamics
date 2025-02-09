package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.item.ElenetCompass;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CropariaItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CropariaIf.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<ElenetCompass<ItemSpec>> ELENET_COMPASS_ITEM = register("elenet_compass_item", () -> new ElenetCompass<>(ItemSpec.TYPE));
    public static final RegistrySupplier<ElenetCompass<FluidSpec>> ELENET_COMPASS_FLUID = register("elenet_compass_fluid", () -> new ElenetCompass<>(FluidSpec.TYPE));

    public static <T extends Item> RegistrySupplier<T> register(String id, Supplier<T> supplier) {
        return ITEMS.register(id, supplier);
    }
}
