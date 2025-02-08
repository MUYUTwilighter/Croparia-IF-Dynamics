package cool.muyucloud.croparia.dynamics.api.repo;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

@SuppressWarnings("unused")
public class FuelUnit<T> implements Repo<T> {
    private final transient T resource;
    private final transient long capacity;
    private transient long amount = 0;

    public FuelUnit(T resource, long capacity) {
        this.resource = resource;
        this.capacity = capacity;
    }

    public void load(JsonObject json) {
        this.amount = GsonHelper.getAsLong(json, "amount", 0L);
    }

    public void save(JsonObject json) {
        json.addProperty("amount", this.amount);
    }

    public boolean isResourceValid(T resource) {
        return this.resource.equals(resource);
    }

    public boolean isEnoughFor(long required) {
        return this.amount >= required;
    }

    public void burn(long required) {
        this.amount -= Math.min(required, this.amount);
    }

    public long getAmount() {
        return this.amount;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty(int i) {
        return this.getAmount() <= 0;
    }

    @Override
    public T resourceFor(int i) {
        return resource;
    }

    @Override
    public long simConsume(int i, T resource, long amount) {
        if (i != 0 || !this.resource.equals(resource)) return 0L;
        return Math.min(amount, this.amount);
    }

    @Override
    public long consume(int i, T resource, long amount) {
        if (i != 0 || !this.resource.equals(resource)) return 0L;
        long consumed = Math.min(amount, this.amount);
        if (consumed <= 0) return 0L;
        this.amount -= consumed;
        return consumed;
    }

    @Override
    public long simAccept(int i, T resource, long amount) {
        if (i != 0 || !this.isResourceValid(resource)) return 0L;
        return Math.min(amount, this.amount);
    }

    @Override
    public long accept(int i, T resource, long amount) {
        if (i != 0 || !this.isResourceValid(resource)) return 0L;
        long accepted = Math.min(this.capacity - this.amount, amount);
        if (accepted <= 0) return 0L;
        this.amount += accepted;
        return accepted;
    }

    @Override
    public long capacityFor(int i, T resource) {
        return i == 0 && this.isResourceValid(resource) ? this.capacity : 0L;
    }

    @Override
    public long amountFor(int i, T resource) {
        return i == 0 && this.resource.equals(resource) ? this.getAmount() : 0L;
    }
}
