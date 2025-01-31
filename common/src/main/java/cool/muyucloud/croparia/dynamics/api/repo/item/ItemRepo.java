package cool.muyucloud.croparia.dynamics.api.repo.item;

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
     * Query the {@link ItemSpec} stored in the specified item slot.
     *
     * @param i The index of the item slot
     * @return The fluid stored in the specified item slot
     */
    ItemSpec itemFor(int i);

    default long simConsume(ItemSpec item, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simConsume(i, item, amount);
        }
        return Math.max(0, amount);
    }

    long simConsume(int i, ItemSpec item, long amount);

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
     * Simulates accepting the specified amount of item into the total storage.
     *
     * @param item   The item to accept
     * @param amount The amount to accept
     * @return the amount actually accepted
     */
    default long simAccept(ItemSpec item, long amount) {
        int i = 0;
        while (i < size() && amount > 0) {
            amount -= simAccept(i, item, amount);
        }
        return Math.max(0, amount);
    }

    /**
     * Simulates accepting the specified amount of item into the specified item slot.
     *
     * @param i      The index of the item slot to accept
     * @param item   The item to accept
     * @param amount The amount to accept
     * @return The amount actually accepted
     */
    long simAccept(int i, ItemSpec item, long amount);

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
    long accept(int i, ItemSpec item, long amount);

    /**
     * Calculates the total capacity for the specified fluid across all item slots.
     *
     * @param item The fluid to check
     * @return The total capacity for the specified fluid
     */
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
}
