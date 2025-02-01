package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.PlatformFluidAgent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Optional;

public class FluidRepoProviderImpl {
    static void register(FluidRepoProvider provider) {
    }

    static Optional<PlatformFluidAgent> find(Level world, BlockPos pos, Direction direction) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return Optional.empty();
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).map(ForgeFluidAgent::of);
    }
}
