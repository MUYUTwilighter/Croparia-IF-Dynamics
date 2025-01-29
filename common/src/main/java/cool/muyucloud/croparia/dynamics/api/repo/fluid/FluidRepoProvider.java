package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A signal to Fluid Repo API to identify a block entity for forge.<br>
 * In most cases, your {@link BlockEntity} implements this.
 */
public interface FluidRepoProvider {
    /**
     * Register a fluid repo provider to fabric
     *
     * @param provider the registration callback, probably it's your {@link BlockEntity} that implement this interface.
     */
    @ExpectPlatform
    static void register(FluidRepoProvider provider) {
        throw new AssertionError("Not implemented");
    }

    /**
     * Provide your {@link FluidAgent}
     *
     * @param world     the world, {@code null} if in forge
     * @param pos       the position, {@code null} if in forge
     * @param state     the state, {@code null} if in forge
     * @param be        the block entity, {@code null} if in forge
     * @param direction the directio of interaction
     */
    @Nullable
    FluidAgent fluidAgent(@Nullable Level world, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity be, Direction direction);
}
