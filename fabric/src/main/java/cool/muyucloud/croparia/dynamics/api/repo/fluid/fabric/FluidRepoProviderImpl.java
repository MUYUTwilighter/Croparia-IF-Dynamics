package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

@SuppressWarnings("UnstableApiUsage")
public class FluidRepoProviderImpl {
    static void register(FluidRepoProvider provider) {
        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> {
            FluidAgent agent = provider.fluidAgent(world, pos, state, blockEntity, direction);
            if (agent == null) return null;
            if (agent instanceof FluidAgentImpl fluidAgent) return fluidAgent;
            throw new AssertionError("Unknown fluid agent: " + agent);
        });
    }
}
