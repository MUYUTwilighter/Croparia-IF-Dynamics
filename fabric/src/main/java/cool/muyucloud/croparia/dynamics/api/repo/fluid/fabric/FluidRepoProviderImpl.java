package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.PlatformFluidAgent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FluidRepoProviderImpl {
    static void register(FluidRepoProvider provider) {
        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> {
            FluidAgent agent = provider.fluidAgent(direction);
            if (agent == null) return null;
            if (agent instanceof FluidAgentImpl fluidAgent) return fluidAgent;
            throw new AssertionError("Unknown fluid agent: " + agent);
        });
    }

    static Optional<PlatformFluidAgent> find(Level world, BlockPos pos, Direction direction) {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, pos, direction);
        if (storage == null) return Optional.empty();
        return Optional.of(FabricFluidAgent.of(storage));
    }
}
