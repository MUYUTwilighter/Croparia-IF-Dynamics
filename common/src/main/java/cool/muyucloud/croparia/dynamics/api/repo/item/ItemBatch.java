package cool.muyucloud.croparia.dynamics.api.repo.item;

import cool.muyucloud.croparia.dynamics.api.repo.RepoBatch;
import cool.muyucloud.croparia.dynamics.api.repo.RepoUnit;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class ItemBatch extends RepoBatch<ItemSpec> {
    @SafeVarargs
    public static ItemBatch of(RepoUnit<ItemSpec>... units) {
        return new ItemBatch(units);
    }

    public static ItemBatch of(Predicate<ItemSpec> itemFilter, long capacity, int count) {
        return new ItemBatch(ItemUnit.of(itemFilter, capacity, count));
    }

    @SafeVarargs
    public ItemBatch(RepoUnit<ItemSpec>... units) {
        super(units);
    }

    public ItemAgent toAgent() {
        return ItemAgent.of(() -> this);
    }
}
