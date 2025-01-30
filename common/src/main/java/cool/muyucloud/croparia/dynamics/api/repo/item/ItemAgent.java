package cool.muyucloud.croparia.dynamics.api.repo.item;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.function.Supplier;

public abstract class ItemAgent implements ItemRepo {
    @ExpectPlatform
    public static ItemAgent of(Supplier<ItemRepo> repo) {
        throw new AssertionError("Not implemented");
    }

    private final ItemRepo repo;

    public ItemAgent(Supplier<ItemRepo> repo) {
        this.repo = repo.get();
    }

    public ItemRepo get() {
        return repo;
    }

    @Override
    public int size() {
        return this.get().size();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.get().isEmpty(i);
    }

    @Override
    public boolean canConsume(ItemSpec item, long amount) {
        return this.get().canConsume(item, amount);
    }

    @Override
    public boolean canConsume(int i, ItemSpec item, long amount) {
        return this.canConsume(item, amount);
    }

    @Override
    public boolean canAccept(ItemSpec item, long amount) {
        return this.get().canAccept(item, amount);
    }

    @Override
    public boolean canAccept(int i, ItemSpec item, long amount) {
        return this.get().canAccept(i, item, amount);
    }

    @Override
    public long consume(ItemSpec item, long amount) {
        return this.get().consume(item, amount);
    }

    @Override
    public long consume(int i, ItemSpec item, long amount) {
        return this.get().consume(i, item, amount);
    }

    @Override
    public long accept(ItemSpec item, long amount) {
        return this.get().accept(item, amount);
    }

    @Override
    public long accept(int i, ItemSpec item, long amount) {
        return this.get().accept(i, item, amount);
    }

    @Override
    public long spaceFor(ItemSpec item) {
        return this.get().spaceFor(item);
    }

    @Override
    public long spaceFor(int i, ItemSpec item) {
        return this.get().spaceFor(i, item);
    }

    @Override
    public long capacityFor(ItemSpec item) {
        return this.get().capacityFor(item);
    }

    @Override
    public long capacityFor(int i, ItemSpec item) {
        return this.get().capacityFor(i, item);
    }

    @Override
    public long amountFor(ItemSpec item) {
        return this.get().amountFor(item);
    }

    @Override
    public long amountFor(int i, ItemSpec item) {
        return this.get().amountFor(i, item);
    }

    @Override
    public ItemSpec itemFor(int i) {
        return this.get().itemFor(i);
    }
}
