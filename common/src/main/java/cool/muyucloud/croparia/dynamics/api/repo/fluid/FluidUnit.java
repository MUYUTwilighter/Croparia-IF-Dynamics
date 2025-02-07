package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class FluidUnit implements Repo<FluidSpec> {
    @NotNull
    private FluidSpec fluid = FluidSpec.EMPTY;
    private final transient Predicate<FluidSpec> fluidFilter;
    private long amount = 0;
    private transient long capacity;
    private boolean consumable = false;
    private boolean acceptable = false;
    private boolean locked = false;
    private boolean recipeUpdate = true;

    public static FluidUnit create(Predicate<FluidSpec> fluidFilter, long capacity) {
        return new FluidUnit(fluidFilter, capacity);
    }

    public FluidUnit(Predicate<FluidSpec> fluidFilter, long capacity) {
        this.fluidFilter = fluidFilter;
        this.capacity = capacity;
    }

    public void load(JsonObject json) {
        if (json.has("fluid")) {
            this.fluid = FluidSpec.CODEC.codec().decode(JsonOps.INSTANCE, json.get("fluid")).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode fluid: %s".formatted(msg))
            ).getFirst();
        }
        this.amount = GsonHelper.getAsLong(json, "amount", 0L);
        this.consumable = GsonHelper.getAsBoolean(json, "consumable", false);
        this.acceptable = GsonHelper.getAsBoolean(json, "acceptable", false);
        this.locked = GsonHelper.getAsBoolean(json, "locked", false);
    }

    public void save(JsonObject json) {
        json.add("fluid", FluidSpec.CODEC.codec().encodeStart(JsonOps.INSTANCE, this.fluid).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode fluid: %s".formatted(msg))
        ));
        json.addProperty("amount", this.amount);
        json.addProperty("consumable", this.consumable);
        json.addProperty("acceptable", this.acceptable);
        json.addProperty("locked", this.locked);
    }

    public @NotNull FluidSpec getFluid() {
        return this.fluid;
    }

    public void setFluid(@NotNull FluidSpec fluid) {
        if (this.getAmount() == 0 && this.isFluidValid(fluid)) {
            this.fluid = fluid;
        }
    }

    public boolean isFluidValid(FluidSpec fluid) {
        if (this.locked || this.getAmount() != 0) return this.getFluid().equals(fluid);
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
        if (this.isLocked()) return fluid.isEmpty();
        else return fluid.isEmpty() || amount == 0;
    }

    @Override
    public FluidSpec resourceFor(int i) {
        return i == 0 ? fluid : null;
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.fluid.equals(resource)) return 0L;
        return Math.min(amount, this.amount);
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        if (i != 0 || !this.isConsumable() || !this.fluid.equals(resource)) return 0L;
        long consumed = Math.min(amount, this.amount);
        if (consumed <= 0) return 0L;
        this.amount -= consumed;
        this.setRecipeUpdate(true);
        return consumed;
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isFluidValid(resource)) return 0L;
        return Math.min(this.capacity - this.amount, amount);
    }

    @Override
    public long accept(int i, FluidSpec resource, long amount) {
        if (i != 0 || !this.isAcceptable() || !this.isFluidValid(resource)) return 0L;
        long accepted = Math.min(this.capacity - this.amount, amount);
        if (accepted <= 0) return 0L;
        this.amount += accepted;
        this.setFluid(resource);
        this.setRecipeUpdate(true);
        return accepted;
    }

    @Override
    public long capacityFor(int i, FluidSpec resource) {
        if (i != 0) return 0L;
        return this.isFluidValid(resource) ? this.capacity : 0L;
    }

    @Override
    public long amountFor(int i, FluidSpec resource) {
        return i == 0 && this.getFluid().equals(resource) ? this.getAmount() : 0L;
    }
}
