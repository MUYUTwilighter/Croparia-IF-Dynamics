package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.block.ElemForgeBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CropariaBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(CropariaIf.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<ElemForgeBlock> TEST = register(
        "test", () -> new ElemForgeBlock(BlockBehaviour.Properties.of(), () -> BlockEntities.get("test"))
    );

    public static <T extends Block> RegistrySupplier<T> register(String id, Supplier<T> supplier) {
        return BLOCKS.register(id, supplier);
    }

    public static void register() {
        BLOCKS.register();
    }
}
