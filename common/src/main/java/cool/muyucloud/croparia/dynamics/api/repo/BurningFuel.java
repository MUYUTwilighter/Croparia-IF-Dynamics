package cool.muyucloud.croparia.dynamics.api.repo;

import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.resource.type.Heat;
import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

public class BurningFuel extends FuelRepo<Heat> {
    private long capacity = 0;
    private long amount = 0;

    public BurningFuel() {
        super(Heat.TYPE, 0);
    }

    @Override
    public void burn(long required) {
        super.burn(required);
    }

    public void refuel(Item item) {
        if (this.isEmpty()) {
            int heat = FuelRegistry.get(item.getDefaultInstance());
            this.capacity = heat;
            this.amount = heat;
        }
    }

    @Override
    public void load(JsonObject json) {
        super.load(json);
        this.capacity = GsonHelper.getAsLong(json, "capacity", 0);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.capacity = nbt.getLong("capacity");
    }

    @Override
    public void save(JsonObject json) {
        super.save(json);
        json.addProperty("capacity", capacity);
    }

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.putLong("capacity", capacity);
    }

    @Override
    public long amountFor(int i, Heat resource) {
        return i == 0 ? amount : 0;
    }

    @Override
    public long amountFor(int i) {
        return i == 0 ? amount : 0;
    }

    @Override
    public long amountFor(Heat resource) {
        return amount;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long capacityFor(Heat resource) {
        return capacity;
    }

    @Override
    public long capacityFor(int i, Heat resource) {
        return i == 0 ? capacity : 0;
    }
}
