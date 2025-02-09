package cool.muyucloud.croparia.dynamics.api.repo;

import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;

import java.util.function.Supplier;

public class ElenetRepo<T extends ResourceType> implements Repo<T> {
    public static <T extends ResourceType> ElenetRepo<T> of(Supplier<Repo<T>> consumable, Supplier<Repo<T>> acceptable) {
        return new ElenetRepo<>(consumable, acceptable);
    }

    private final Supplier<Repo<T>> consumable;
    private final Supplier<Repo<T>> acceptable;

    public ElenetRepo(Supplier<Repo<T>> consumable, Supplier<Repo<T>> acceptable) {
        this.consumable = consumable;
        this.acceptable = acceptable;
    }

    @Override
    public int size() {
        return this.consumable.get().size() + this.acceptable.get().size();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.consumable.get().isEmpty() && this.acceptable.get().isEmpty();
    }

    @Override
    public T resourceFor(int i) {
        return i >= consumable.get().size() ? acceptable.get().resourceFor(i - consumable.get().size()) : consumable.get().resourceFor(i);
    }

    @Override
    public long simConsume(int i, T resource, long amount) {
        return consumable.get().simConsume(i, resource, amount);
    }

    @Override
    public long consume(int i, T resource, long amount) {
        return consumable.get().consume(i, resource, amount);
    }

    @Override
    public long simAccept(int i, T resource, long amount) {
        return acceptable.get().simAccept(i - consumable.get().size(), resource, amount);
    }

    @Override
    public long accept(int i, T resource, long amount) {
        return acceptable.get().accept(i - consumable.get().size(), resource, amount);
    }

    @Override
    public long capacityFor(int i, T resource) {
        return i >= consumable.get().size() ? acceptable.get().capacityFor(i - consumable.get().size(), resource) : consumable.get().capacityFor(i, resource);
    }

    @Override
    public long amountFor(int i, T resource) {
        return i >= consumable.get().size() ? acceptable.get().amountFor(i - consumable.get().size(), resource) : consumable.get().amountFor(i, resource);
    }

    @Override
    public TypeToken<?> getType() {
        return consumable.get().getType();
    }
}
