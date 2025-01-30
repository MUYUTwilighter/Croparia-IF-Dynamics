package cool.muyucloud.croparia.dynamics.api.repo.item;

import cool.muyucloud.croparia.dynamics.api.repo.Unreliable;

public interface ItemRepo {
    /**
     * The amount of item slots
     */
    int size();

    /**
     * Whether the specified item slot is empty
     */
    boolean isEmpty(int i);

    /**
     * Checks if the total storage for the specified item is at or above the specified amount.
     *
     * @param item   The item to check
     * @param amount The amount to check
     * @return true if the total storage can consume the specified amount, false otherwise
     */
    default boolean canConsume(ItemSpec item, long amount) {
        long total = 0;
        for (int i = 0; i < size() && total < amount; i++) {
            total += amountFor(i, item);
        }
        return total >= amount;
    }

    /**
     * Determines if the specified item slot can consume at least the specified amount.
     *
     * @param i      The index of the item slot to check
     * @param item   The item to check
     * @param amount The amount to check
     * @return true if the item slot can consume the specified amount, false otherwise
     */
    @Unreliable(reason = "only responsible for the remaining amount")
    boolean canConsume(int i, ItemSpec item, long amount);

    /**
     * Whether the total space for the specified item is at or above the specified amount
     *
     * @param item   The item to check
     * @param amount The amount to check
     * @return result
     */
    @Unreliable(value = "FABRIC", reason = "only responsible for the remaining space")
    default boolean canAccept(ItemSpec item, long amount) {
        long total = 0;
        for (int i = 0; i < size() && total < amount; i++) {
            total += spaceFor(i, item);
        }
        return false;
    }


    /**
     * Whether the specified item slot can accept at least the specified amount.
     *
     * @param i      The index of the item slot to check
     * @param item   The item to check
     * @param amount The amount to check
     * @return true if the item slot can accept the specified amount, false otherwise
     */
    @Unreliable(value = "FABRIC", reason = "only responsible for the remaining space")
    boolean canAccept(int i, ItemSpec item, long amount);

    /**
     * Consumes the specified amount of item from the total storage.
     *
     * @param item   The item to consume
     * @param amount The amount to consume
     * @return the amount actually consumed
     */
    default long consume(ItemSpec item, long amount) {
        long totalConsumed = 0;
        for (int i = 0; i < size() && totalConsumed < amount; i++) {
            long consumed = consume(i, item, amount - totalConsumed);
            totalConsumed += consumed;
        }
        return totalConsumed;
    }

    /**
     * Consumes the specified amount of item from the specified item slot.
     *
     * @param i      The index of the item slot to consume
     * @param item   The item to consume
     * @param amount The amount to consume
     * @return The amount actually consumed
     */
    long consume(int i, ItemSpec item, long amount);

    /**
     * Accepts the specified amount of item into the total storage.
     *
     * @param item   The item to accept
     * @param amount The amount to accept
     * @return the amount actually accepted
     */
    default long accept(ItemSpec item, long amount) {
        long totalAccepted = 0;
        for (int i = 0; i < size() && totalAccepted < amount; i++) {
            long accepted = accept(i, item, amount - totalAccepted);
            totalAccepted += accepted;
        }
        return totalAccepted;
    }

    /**
     * Accepts the specified amount of item into the specified item slot.
     *
     * @param i      The index of the item slot to accept
     * @param item   The item to accept
     * @param amount The amount to accept
     * @return The amount actually accepted
     */
    @Unreliable(value = "FABRIC", reason = "only insertable if specified StorageView extends Storage")
    long accept(int i, ItemSpec item, long amount);

    /**
     * The total amount of item that can be accepted into the total storage.
     *
     * @param item The item to check
     * @return The total amount of item that can be accepted
     */
    @Unreliable(value = "FABRIC", reason = "calculated from amount & capacity")
    default long spaceFor(ItemSpec item) {
        long space = 0;
        for (int i = 0; i < size(); i++) {
            space += spaceFor(i, item);
        }
        return space;
    }

    /**
     * The amount of item that can be accepted into the specified item slot.
     *
     * @param i    The index of the item slot to check
     * @param item The item to check
     * @return The amount of item that can be accepted
     */
    @Unreliable(value = "FABRIC", reason = "calculated from amount & capacity")
    long spaceFor(int i, ItemSpec item);

    /**
     * Calculates the total capacity for the specified fluid across all item slots.
     *
     * @param item The fluid to check
     * @return The total capacity for the specified fluid
     */
    @Unreliable(value = "FABRIC", reason = "empty StorageView is counted as full capacity")
    default long capacityFor(ItemSpec item) {
        long capacity = 0;
        for (int i = 0; i < size(); i++) {
            capacity += capacityFor(i, item);
        }
        return capacity;
    }

    /**
     * Calculates the capacity for the specified item in the specified item slot.
     *
     * @param i    The index of the item slot to check
     * @param item The item to check
     * @return The capacity for the specified item
     */
    @Unreliable(reason = "empty StorageView is counted as full capacity")
    long capacityFor(int i, ItemSpec item);

    /**
     * Calculates the total amount of item across all item slots.
     *
     * @param item The item to check
     * @return The total amount of item
     */
    default long amountFor(ItemSpec item) {
        long amount = 0;
        for (int i = 0; i < size(); i++) {
            amount += amountFor(i, item);
        }
        return amount;
    }

    /**
     * Calculates the amount of item in the specified item slot.
     *
     * @param i    The index of the item slot to check
     * @param item The item to check
     * @return The amount of item
     */
    long amountFor(int i, ItemSpec item);

    /**
     * Retrieves the item stored in the specified item slot.
     *
     * @param i The index of the item slot
     * @return The fluid stored in the specified item slot
     */
    ItemSpec itemFor(int i);
}
