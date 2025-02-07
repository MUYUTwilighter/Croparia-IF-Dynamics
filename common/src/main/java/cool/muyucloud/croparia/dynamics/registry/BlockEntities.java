package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("unused")
public class BlockEntities {
    private static final DeferredRegister<BlockEntityType<? extends BlockEntity>> BLOCK_ENTITIES = DeferredRegister.create(CropariaIf.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<? extends T>> register(String id, BlockEntityType.BlockEntitySupplier<? extends T> supplier, Block... blocks) {
        return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(supplier, blocks).build(null));
    }
}
