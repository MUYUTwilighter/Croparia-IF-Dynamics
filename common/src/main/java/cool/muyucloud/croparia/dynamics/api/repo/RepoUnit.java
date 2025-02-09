package cool.muyucloud.croparia.dynamics.api.repo;

import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public abstract class RepoUnit<T extends ResourceType> implements Repo<T> {
    private final transient Predicate<T> fluidFilter;
    private long amount = 0;
    private transient long capacity;
    private boolean consumable = false;
    private boolean acceptable = false;
    private boolean locked = false;
    private boolean changed = true;

    public RepoUnit(Predicate<T> fluidFilter, long capacity) {
        this.fluidFilter = fluidFilter;
        this.capacity = capacity;
    }

    public void load(JsonObject json) {
        this.amount = GsonHelper.getAsLong(json, "amount", 0L);
        this.consumable = GsonHelper.getAsBoolean(json, "consumable", false);
        this.acceptable = GsonHelper.getAsBoolean(json, "acceptable", false);
        this.locked = GsonHelper.getAsBoolean(json, "locked", false);
    }

    public void load(CompoundTag nbt) {
        this.amount = nbt.getLong("amount");
        this.consumable = nbt.getBoolean("consumable");
        this.acceptable = nbt.getBoolean("acceptable");
        this.locked = nbt.getBoolean("locked");
    }

    public void save(JsonObject json) {
        json.addProperty("amount", this.amount);
        json.addProperty("consumable", this.consumable);
        json.addProperty("acceptable", this.acceptable);
        json.addProperty("locked", this.locked);
    }

    public void save(CompoundTag nbt) {
        nbt.putLong("amount", this.amount);
        nbt.putBoolean("consumable", this.consumable);
        nbt.putBoolean("acceptable", this.acceptable);
        nbt.putBoolean("locked", this.locked);
    }

    public abstract @NotNull T getResource();

    public abstract void setResource(@NotNull T resource);

    public boolean isFluidValid(T fluid) {
        if (this.locked || this.getAmount() != 0) return this.getResource().equals(fluid);
        else return this.fluidFilter.test(fluid);
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public boolean isConsumable() {
        return this.consumable;
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }

    public boolean isAcceptable() {
        return this.acceptable;
    }

    public void setAcceptable(boolean acceptable) {
        this.acceptable = acceptable;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isChanged() {
        return this.changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public T resourceFor(int i) {
        return i == 0 ? getResource() : null;
    }

    @Override
    public long simConsume(int i, T resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.getResource().equals(resource)) return 0L;
        return Math.min(amount, this.amount);
    }

    @Override
    public long consume(int i, T resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.getResource().equals(resource)) return 0L;
        long consumed = Math.min(amount, this.amount);
        if (consumed <= 0) return 0L;
        this.amount -= consumed;
        this.setChanged(true);
        return consumed;
    }

    public long consume(long amount) {
        return this.consume(this.getResource(), amount);
    }

    @Override
    public long simAccept(int i, T resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isFluidValid(resource)) return 0L;
        return Math.min(this.capacity - this.amount, amount);
    }

    @Override
    public long accept(int i, T resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isFluidValid(resource)) return 0L;
        long accepted = Math.min(this.capacity - this.amount, amount);
        if (accepted <= 0) return 0L;
        this.amount += accepted;
        this.setResource(resource);
        this.setChanged(true);
        return accepted;
    }

    @Override
    public long capacityFor(int i, T resource) {
        if (i != 0) return 0L;
        return this.isFluidValid(resource) ? this.capacity : 0L;
    }

    @Override
    public long amountFor(int i, T resource) {
        return i == 0 && this.getResource().equals(resource) ? this.getAmount() : 0L;
    }
}
