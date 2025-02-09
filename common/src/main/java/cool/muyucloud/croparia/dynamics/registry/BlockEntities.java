package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.block.entity.HeatForgeBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(CropariaIf.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HeatForgeBlockEntity>> ELEM_FORGE_BLOCK_ENTITY = register(
        "test",
        () -> BlockEntityType.Builder.of((pos, state) -> new HeatForgeBlockEntity(
            () -> get("test"), pos, state, RecipeTypes.TEST.get(), 3
        ), CropariaBlocks.TEST.get()).build(null)
    );

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityType<T> get(String id) {
        return (BlockEntityType<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE.get(CropariaIf.of(id));
    }

    public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String id, Supplier<BlockEntityType<T>> supplier) {
        return BLOCK_ENTITIES.register(id, supplier);
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
