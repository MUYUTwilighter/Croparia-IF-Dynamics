package cool.muyucloud.croparia.dynamics.api.repo.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class ItemUnit implements Repo<ItemSpec> {
    public static ItemUnit create(Predicate<ItemSpec> itemFilter, long capacity) {
        return new ItemUnit(itemFilter, capacity);
    }

    public static ItemUnit[] create(Predicate<ItemSpec> itemFilter, long capacity, int count) {
        ItemUnit[] units = new ItemUnit[count];
        for (int i = 0; i < count; i++) {
            units[i] = create(itemFilter, capacity);
        }
        return units;
    }

    @NotNull
    private ItemSpec item = ItemSpec.EMPTY;
    private final transient Predicate<ItemSpec> itemFilter;
    private long amount = 0;
    private transient long capacity;
    private boolean consumable = false;
    private boolean acceptable = false;
    private boolean locked = false;
    private boolean recipeUpdate = true;

    public ItemUnit(Predicate<ItemSpec> itemFilter, long capacity) {
        this.itemFilter = itemFilter;
        this.capacity = capacity;
    }

    public void load(JsonObject json) {
        if (json.has("fluid")) {
            this.item = ItemSpec.CODEC.codec().decode(JsonOps.INSTANCE, json.get("fluid")).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode fluid: %s".formatted(msg))
            ).getFirst();
        }
        this.amount = GsonHelper.getAsLong(json, "amount", 0L);
        this.consumable = GsonHelper.getAsBoolean(json, "consumable", false);
        this.acceptable = GsonHelper.getAsBoolean(json, "acceptable", false);
        this.locked = GsonHelper.getAsBoolean(json, "locked", false);
    }

    public void save(JsonObject json) {
        json.add("item", ItemSpec.CODEC.codec().encodeStart(JsonOps.INSTANCE, this.getItem()).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode fluid: %s".formatted(msg))
        ));
        json.addProperty("amount", this.amount);
        json.addProperty("consumable", this.consumable);
        json.addProperty("acceptable", this.acceptable);
        json.addProperty("locked", this.locked);
    }

    public boolean isItemValid(ItemSpec item) {
        if (this.locked || this.getAmount() != 0) return this.getItem().equals(item);
        else return this.itemFilter.test(item);
    }

    public @NotNull ItemSpec getItem() {
        return !this.isLocked() && this.getAmount() == 0 ? ItemSpec.EMPTY : this.item;
    }

    public void setItem(@NotNull ItemSpec item) {
        if (this.getAmount() == 0) {
            this.item = item;
        }
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

    public boolean shouldUpdateRecipe() {
        return this.recipeUpdate;
    }

    public void setRecipeUpdate(boolean recipeUpdate) {
        this.recipeUpdate = recipeUpdate;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty(int i) {
        if (i != 0) return true;
        if (this.isLocked()) return item.isEmpty();
        else return item.isEmpty() || amount == 0;
    }

    @Override
    public ItemSpec resourceFor(int i) {
        return i == 0 ? item : null;
    }

    @Override
    public long simConsume(int i, ItemSpec resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.item.equals(resource)) return 0L;
        return Math.min(amount, this.amount);
    }

    @Override
    public long consume(int i, ItemSpec resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.item.equals(resource)) return 0L;
        long consumed = Math.min(amount, this.amount);
        if (consumed <= 0) return 0L;
        this.amount -= consumed;
        this.setRecipeUpdate(true);
        return consumed;
    }

    @Override
    public long simAccept(int i, ItemSpec resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isItemValid(resource)) return 0L;
        return Math.min(this.capacity - this.amount, amount);
    }

    @Override
    public long accept(int i, ItemSpec resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isItemValid(resource)) return 0L;
        long accepted = Math.min(this.capacity - this.amount, amount);
        if (accepted <= 0) return 0L;
        this.amount += accepted;
        this.setItem(resource);
        this.setRecipeUpdate(true);
        return accepted;
    }

    @Override
    public long capacityFor(int i, ItemSpec resource) {
        return i == 0 && this.isItemValid(resource) ? this.capacity : 0L;
    }

    @Override
    public long amountFor(int i, ItemSpec resource) {
        return i == 0 && this.getItem().equals(resource) ? this.getAmount() : 0L;
    }
}
