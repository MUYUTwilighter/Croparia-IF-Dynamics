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
    public long simConsume(ItemSpec item, long amount) {
        return this.get().simConsume(item, amount);
    }

    @Override
    public long simConsume(int i, ItemSpec item, long amount) {
        return this.get().simConsume(i, item, amount);
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
    public long simAccept(ItemSpec item, long amount) {
        return this.get().simAccept(item, amount);
    }

    @Override
    public long simAccept(int i, ItemSpec item, long amount) {
        return this.get().simAccept(i, item, amount);
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
