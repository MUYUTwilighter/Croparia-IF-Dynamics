package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;

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
public interface FluidRepo extends Repo<FluidSpec> {

    /**
     * Simulates consuming the specified amount of fluid from the total storage.
     *
     * @param fluid  The fluid to consume
     * @param amount The amount to consume
     * @return The amount that can be consumed
     */
    @Override
    default long simConsume(FluidSpec fluid, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simConsume(i, fluid, amount);
        }
        return Math.max(0, amount);
    }

    /**
     * Consumes the specified amount of fluid from the total storage.
     *
     * @param resource  The fluid to consume
     * @param amount The amount to consume
     * @return the amount actually consumed
     */
    @Override
    default long consume(FluidSpec resource, long amount) {
        long totalConsumed = 0;
        for (int i = 0; i < size() && totalConsumed < amount; i++) {
            long consumed = consume(i, resource, amount - totalConsumed);
            totalConsumed += consumed;
        }
        return totalConsumed;
    }

    /**
     * Simulates accepting the specified amount of fluid into the total storage.
     *
     * @param resource  The fluid to accept
     * @param amount The amount to accept
     * @return The amount that can be accepted
     * */
    @Override
    default long simAccept(FluidSpec resource, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simAccept(i, resource, amount);
        }
        return Math.max(0, amount);
    }

    /**
     * Accepts the specified amount of fluid into the total storage.
     *
     * @param fluid  The fluid to accept
     * @param amount The amount to accept
     * @return the amount actually accepted
     */
    @Override
    default long accept(FluidSpec fluid, long amount) {
        long totalAccepted = 0;
        for (int i = 0; i < size() && totalAccepted < amount; i++) {
            long accepted = accept(i, fluid, amount - totalAccepted);
            totalAccepted += accepted;
        }
        return totalAccepted;
    }

    /**
     * Calculates the total capacity for the specified fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total capacity for the specified fluid
     */
    @Override
    default long capacityFor(FluidSpec fluid) {
        long capacity = 0;
        for (int i = 0; i < size(); i++) {
            capacity += capacityFor(i, fluid);
        }
        return capacity;
    }

    /**
     * Calculates the total amount of fluid across all fluid storage units.
     *
     * @param fluid The fluid to check
     * @return The total amount of fluid
     */
    @Override
    default long amountFor(FluidSpec fluid) {
        long amount = 0;
        for (int i = 0; i < size(); i++) {
            amount += amountFor(i, fluid);
        }
        return amount;
    }

}
