package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.repo.Unreliable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public interface PlatformFluidAgent extends FluidRepo {
    /**
     * Simulates consuming the specified amount of fluid from the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to consume
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount that can be consumed
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FORGE", reason = "consumption ignores the index")
    long simConsume(int i, FluidSpec fluid, long amount);

    /**
     * Consumes the specified amount of fluid from the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to consume
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount actually consumed
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FORGE", reason = "consumption ignores the index")
    long consume(int i, FluidSpec fluid, long amount);

    /**
     * Simulates accepting the specified amount of fluid into the specified fluid storage.
     *
     * @param i      The index of the fluid storage unit to accept
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return The amount that can be accepted
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FORGE", reason = "insertion ignores the index")
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long simAccept(int i, FluidSpec fluid, long amount);

    /**
     * Accepts the specified amount of fluid into the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to accept
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return The amount actually accepted
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FORGE", reason = "insertion ignores the index")
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long accept(int i, FluidSpec fluid, long amount);

    /**
     * Calculates the total capacity for the specified fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total capacity for the specified fluid
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    default long capacityFor(FluidSpec fluid) {
        return FluidRepo.super.capacityFor(fluid);
    }

    /**
     * Calculates the capacity for the specified fluid in the specified fluid storage unit.
     *
     * @param i     The index of the fluid storage unit to check
     * @param fluid The fluid to check
     * @return The capacity for the specified fluid
     */
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    long capacityFor(int i, FluidSpec fluid);

    /**
     * Get the proxied repo if it implements the {@link FluidRepo},
     * which is probably implemented by Repo API here and have full function.
     *
     * @return The proxied repo
     */
    Optional<FluidRepo> extract();
}
