package cool.muyucloud.croparia.dynamics.api.repo.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A signal to Item Repo API to identify a block entity for forge.<br>
 * In most cases, your {@link BlockEntity} implements this.
 */
@SuppressWarnings("unused")
public interface ItemRepoProvider {
    /**
     * Register a item repo provider to fabric
     *
     * @param provider the registration callback, probably it's your {@link BlockEntity} that implement this interface.
     */
    @ExpectPlatform
    static void register(ItemRepoProvider provider) {
        throw new AssertionError("Not implemented");
    }

    /**
     * <p>
     * Find a {@link ItemRepo} in world.
     * </p>
     * <p>
     * The return value might not be fully reliable. See methods in {@link ItemRepo} with annotation {@link cool.muyucloud.croparia.dynamics.api.repo.Unreliable}
     * </p>
     *
     * @param world     the world
     * @param pos       the position of the block entity
     * @param direction the direction of the block entity
     * @return the item repo
     */
    @ExpectPlatform
    static Optional<PlatformItemAgent> find(Level world, BlockPos pos, Direction direction) {
        throw new AssertionError("Not implemented");
    }

    /**
     * Provide your {@link ItemAgent}
     *
     * @param direction the directio of interaction
     */
    @Nullable
    ItemAgent itemAgent(@Nullable Direction direction);
}
