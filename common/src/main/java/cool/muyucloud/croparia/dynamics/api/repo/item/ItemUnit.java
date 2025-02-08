package cool.muyucloud.croparia.dynamics.api.repo.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.RepoUnit;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class ItemUnit extends RepoUnit<ItemSpec> {
    public static ItemUnit of(Predicate<ItemSpec> itemFilter, long capacity) {
        return new ItemUnit(itemFilter, capacity);
    }

    public static ItemUnit[] of(Predicate<ItemSpec> itemFilter, long capacity, int count) {
        ItemUnit[] units = new ItemUnit[count];
        for (int i = 0; i < count; i++) {
            units[i] = of(itemFilter, capacity);
        }
        return units;
    }

    @NotNull
    private ItemSpec item = ItemSpec.EMPTY;

    public ItemUnit(Predicate<ItemSpec> itemFilter, long capacity) {
        super(itemFilter, capacity);
    }

    @Override
    public TypeToken<ItemSpec> getType() {
        return ItemSpec.TYPE;
    }

    public void load(JsonObject json) {
        super.load(json);
        if (json.has("fluid")) {
            this.item = ItemSpec.CODEC.codec().decode(JsonOps.INSTANCE, json.get("fluid")).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode fluid: %s".formatted(msg))
            ).getFirst();
        }
    }

    public void save(JsonObject json) {
        super.save(json);
        json.add("item", ItemSpec.CODEC.codec().encodeStart(JsonOps.INSTANCE, this.getResource()).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode fluid: %s".formatted(msg))
        ));
    }

    @Override
    public @NotNull ItemSpec getResource() {
        return this.item;
    }

    @Override
    public void setResource(@NotNull ItemSpec item) {
        if (this.getAmount() == 0 && this.isFluidValid(item)) {
            this.item = item;
        }
    }

    @Override
    public boolean isEmpty(int i) {
        return this.getAmount() <= 0 || this.getResource().isEmpty();
    }
}
