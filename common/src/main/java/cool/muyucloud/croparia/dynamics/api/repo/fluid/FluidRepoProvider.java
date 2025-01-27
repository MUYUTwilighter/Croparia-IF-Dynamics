package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface FluidRepoProvider {
    FluidRepo get(Level world, BlockPos pos, BlockState state, BlockEntity be, Direction direction);

    Level getLevel();

    BlockPos getBlockPos();
}
