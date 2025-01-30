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
     * Checks if the total storage for the specified fluid is at or above the specified amount.
     *
     * @param fluid  The fluid to check
     * @param amount The amount to check
     * @return true if the total storage can consume the specified amount, false otherwise
     */
    default boolean canConsume(FluidSpec fluid, long amount) {
        long total = 0;
        for (int i = 0; i < size() && total < amount; i++) {
            total += amountFor(i, fluid);
        }
        return total >= amount;
    }

    /**
     * Determines if the specified fluid storage unit can consume at least the specified amount.
     *
     * @param i      The index of the fluid storage unit to check
     * @param fluid  The fluid to check
     * @param amount The amount to check
     * @return true if the fluid storage unit can consume the specified amount, false otherwise
     */
    @Unreliable(reason = "only responsible for the remaining amount")
    boolean canConsume(int i, FluidSpec fluid, long amount);

    /**
     * Whether the total space for the specified fluid is at or above the specified amount
     *
     * @param fluid  The fluid to check
     * @param amount The amount to check
     * @return result
     */
    @Unreliable(value = "FABRIC", reason = "only responsible for the remaining space")
    default boolean canAccept(FluidSpec fluid, long amount) {
        long total = 0;
        for (int i = 0; i < size() && total < amount; i++) {
            total += spaceFor(i, fluid);
        }
        return false;
    }


    /**
     * Whether the specified fluid storage unit can accept at least the specified amount.
     *
     * @param i      The index of the fluid storage unit to check
     * @param fluid  The fluid to check
     * @param amount The amount to check
     * @return true if the fluid storage unit can accept the specified amount, false otherwise
     */
    @Unreliable(value = "FABRIC", reason = "only responsible for the remaining space")
    boolean canAccept(int i, FluidSpec fluid, long amount);

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
    @Unreliable(value = "FORGE", reason = "consumed fluid might not be from the specified unit")
    long consume(int i, FluidSpec fluid, long amount);

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
    @Unreliable(value = "FORGE", reason = "inserted fluid might not be into the specified unit")
    @Unreliable(value = "FABRIC", reason = "only insertable if specified StorageView extends Storage")
    long accept(int i, FluidSpec fluid, long amount);

    /**
     * The total amount of fluid that can be accepted into the total storage.
     *
     * @param fluid The fluid to check
     * @return The total amount of fluid that can be accepted
     */
    @Unreliable(value = "FABRIC", reason = "calculated from amount & capacity")
    default long spaceFor(FluidSpec fluid) {
        long space = 0;
        for (int i = 0; i < size(); i++) {
            space += spaceFor(i, fluid);
        }
        return space;
    }

    /**
     * The amount of fluid that can be accepted into the specified fluid storage unit.
     *
     * @param i     The index of the fluid storage unit to check
     * @param fluid The fluid to check
     * @return The amount of fluid that can be accepted
     */
    @Unreliable(value = "FABRIC", reason = "calculated from amount & capacity")
    long spaceFor(int i, FluidSpec fluid);

    /**
     * Calculates the total capacity for the specified fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total capacity for the specified fluid
     */
    @Unreliable(value = "FABRIC", reason = "empty StorageView is counted as full capacity")
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
    @Unreliable(value = "FABRIC", reason = "empty StorageView is counted as full capacity")
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

    /**
     * Retrieves the fluid stored in the specified fluid storage unit.
     *
     * @param i The index of the fluid storage unit
     * @return The fluid stored in the specified fluid storage unit
     */
    FluidSpec fluidFor(int i);
}
