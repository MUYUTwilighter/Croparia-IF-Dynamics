package cool.muyucloud.croparia.dynamics.api.repo;

/**
 * Abstraction of {@link cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo} and
 * {@link cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo}<br>
 * <p>
 * Please do not implement this interface if you just want a repo with resource type {@link T} that is already implemented.
 * Use the provided {@link cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo} and
 * {@link cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo} instead.<br>
 * </p>
 */
public interface Repo<T> {
    /**
     * The amount of resource storage units
     */
    int size();

    /**
     * Whether the specified resource storage unit is empty
     */
    boolean isEmpty(int i);

    /**
     * Query the resource type of the specified storage unit
     *
     * @param i The index of the resource storage unit
     * @return The resource stored in the specified resource storage unit
     */
    T resourceFor(int i);

    /**
     * Simulates consuming the specified amount of resource from the total storage.
     *
     * @param resource The resource to consume
     * @param amount   The amount to consume
     * @return The amount that can be consumed
     */
    long simConsume(T resource, long amount);

    /**
     * Simulates consuming the specified amount of resource from the specified resource storage unit.
     *
     * @param i        The index of the resource storage unit to consume
     * @param resource The resource to consume
     * @param amount   The amount to consume
     * @return The amount that can be consumed
     */
    long simConsume(int i, T resource, long amount);

    /**
     * Consumes the specified amount of resource from the total storage.
     *
     * @param resource The resource to consume
     * @param amount   The amount to consume
     * @return the amount actually consumed
     */
    long consume(T resource, long amount);

    /**
     * Consumes the specified amount of resource from the specified resource storage unit.
     *
     * @param i        The index of the resource storage unit to consume
     * @param resource The resource to consume
     * @param amount   The amount to consume
     * @return The amount actually consumed
     */
    long consume(int i, T resource, long amount);

    /**
     * Simulates accepting the specified amount of resource into the total storage.
     *
     * @param resource The resource to accept
     * @param amount   The amount to accept
     * @return The amount that can be accepted
     */
    long simAccept(T resource, long amount);

    /**
     * Simulates accepting the specified amount of resource into the specified resource storage.
     *
     * @param i        The index of the resource storage unit to accept
     * @param resource The resource to accept
     * @param amount   The amount to accept
     * @return The amount that can be accepted
     */
    long simAccept(int i, T resource, long amount);

    /**
     * Accepts the specified amount of resource into the total storage.
     *
     * @param resource The resource to accept
     * @param amount   The amount to accept
     * @return the amount actually accepted
     */
    long accept(T resource, long amount);

    /**
     * Accepts the specified amount of resource into the specified resource storage unit.
     *
     * @param i        The index of the resource storage unit to accept
     * @param resource The resource to accept
     * @param amount   The amount to accept
     * @return The amount actually accepted
     */
    long accept(int i, T resource, long amount);

    /**
     * Calculates the total capacity for the specified resource across all resource storage units.
     *
     * @param resource The resource to check
     * @return The total capacity for the specified resource
     */
    long capacityFor(T resource);

    /**
     * Calculates the capacity for the specified resource in the specified resource storage unit.
     *
     * @param i        The index of the resource storage unit to check
     * @param resource The resource to check
     * @return The capacity for the specified resource
     */
    long capacityFor(int i, T resource);

    /**
     * Calculates the total amount of resource across all resource storage units.
     *
     * @param resource The resource to check
     * @return The total amount of resource
     */
    long amountFor(T resource);

    /**
     * Calculates the amount of resource in the specified resource storage unit.
     *
     * @param i        The index of the resource storage unit to check
     * @param resource The resource to check
     * @return The amount of resource
     */
    long amountFor(int i, T resource);
}
