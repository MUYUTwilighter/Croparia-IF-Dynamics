package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.item.ElenetAstrolabe;
import cool.muyucloud.croparia.dynamics.api.resource.type.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.resource.type.ItemSpec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class CropariaItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CropariaIf.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<ElenetAstrolabe<ItemSpec>> ELENET_ASTROLABE_ITEM = register(
        "elenet_astrolabe_item", () -> new ElenetAstrolabe<>(ItemSpec.TYPE, new Item.Properties().arch$tab(CreativeTabs.DYNAMICS))
    );
    public static final RegistrySupplier<ElenetAstrolabe<FluidSpec>> ELENET_ASTROLABE_FLUID = register(
        "elenet_astrolabe_fluid", () -> new ElenetAstrolabe<>(FluidSpec.TYPE, new Item.Properties().arch$tab(CreativeTabs.DYNAMICS))
    );
    public static final RegistrySupplier<BlockItem> TEST = register(
        "test", () -> new BlockItem(CropariaBlocks.TEST.get(), new Item.Properties().arch$tab(CreativeTabs.DYNAMICS))
    );

    public static <T extends Item> RegistrySupplier<T> register(String id, Supplier<T> supplier) {
        return ITEMS.register(id, supplier);
    }

    public static void register() {
        ITEMS.register();
    }
}
