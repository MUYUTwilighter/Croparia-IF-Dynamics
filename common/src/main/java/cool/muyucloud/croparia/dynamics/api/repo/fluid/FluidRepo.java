package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.repo.Unreliable;

/**
 * <p>
 * This is where you define the behavior of your fluid repository.<br>
 * To connect this to a fluid API, see {@link FluidAgent} and {@link FluidRepoProvider}.
 * </p>
 * <p>
 * Abstractly, this should be viewed as a composite of "fluid storage units"
 * </p>
 * <p>We use fabric fluid unit here (81000 = 1 bucket)</p>
 */
public interface FluidRepo {
    /**
     * The amount of fluid storage units
     */
    int size();

    /**
     * Whether the specified fluid storage unit is empty
     */
    boolean isEmpty(int i);

    /**
     * Query the {@link FluidSpec} of the specified fluid storage unit
     *
     * @param i The index of the fluid storage unit
     * @return The fluid stored in the specified fluid storage unit
     */
    FluidSpec fluidFor(int i);

    /**
     * Simulates consuming the specified amount of fluid from the total storage.
     *
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount that can be consumed
     */
    default long simConsume(FluidSpec fluid, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simConsume(i, fluid, amount);
        }
        return Math.max(0, amount);
    }

    /**
     * Simulates consuming the specified amount of fluid from the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to consume
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount that can be consumed
     */
    @Unreliable(value = "FORGE", reason = "consumption ignores the index")
    long simConsume(int i, FluidSpec fluid, long amount);

    /**
     * Consumes the specified amount of fluid from the total storage.
     *
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return the amount actually consumed
     */
    default long consume(FluidSpec fluid, long amount) {
        long totalConsumed = 0;
        for (int i = 0; i < size() && totalConsumed < amount; i++) {
            long consumed = consume(i, fluid, amount - totalConsumed);
            totalConsumed += consumed;
        }
        return totalConsumed;
    }

    /**
     * Consumes the specified amount of fluid from the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to consume
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount actually consumed
     */
    @Unreliable(value = "FORGE", reason = "consumption ignores the index")
    long consume(int i, FluidSpec fluid, long amount);

    /**
     * Simulates accepting the specified amount of fluid into the total storage.
     *
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return The amount that can be accepted
     * */
    default long simAccept(FluidSpec fluid, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simAccept(i, fluid, amount);
        }
        return Math.max(0, amount);
    }

    /**
     * Simulates accepting the specified amount of fluid into the specified fluid storage.
     *
     * @param i      The index of the fluid storage unit to accept
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return The amount that can be accepted
     */
    @Unreliable(value = "FORGE", reason = "insertion ignores the index")
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long simAccept(int i, FluidSpec fluid, long amount);

    /**
     * Accepts the specified amount of fluid into the total storage.
     *
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return the amount actually accepted
     */
    default long accept(FluidSpec fluid, long amount) {
        long totalAccepted = 0;
        for (int i = 0; i < size() && totalAccepted < amount; i++) {
            long accepted = accept(i, fluid, amount - totalAccepted);
            totalAccepted += accepted;
        }
        return totalAccepted;
    }

    /**
     * Accepts the specified amount of fluid into the specified fluid storage unit.
     *
     * @param i      The index of the fluid storage unit to accept
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return The amount actually accepted
     */
    @Unreliable(value = "FORGE", reason = "insertion ignores the index")
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long accept(int i, FluidSpec fluid, long amount);

    /**
     * Calculates the total capacity for the specified fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total capacity for the specified fluid
     */
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    default long capacityFor(FluidSpec fluid) {
        long capacity = 0;
        for (int i = 0; i < size(); i++) {
            capacity += capacityFor(i, fluid);
        }
        return capacity;
    }

    /**
     * Calculates the capacity for the specified fluid in the specified fluid storage unit.
     *
     * @param i     The index of the fluid storage unit to check
     * @param fluid The fluid to check
     * @return The capacity for the specified fluid
     */
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    long capacityFor(int i, FluidSpec fluid);

    /**
     * Calculates the total amount of fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total amount of fluid
     */
    default long amountFor(FluidSpec fluid) {
        long amount = 0;
        for (int i = 0; i < size(); i++) {
            amount += amountFor(i, fluid);
        }
        return amount;
    }

    /**
     * Calculates the amount of fluid in the specified fluid storage unit.
     *
     * @param i     The index of the fluid storage unit to check
     * @param fluid The fluid to check
     * @return The amount of fluid
     */
    long amountFor(int i, FluidSpec fluid);
}
