package cool.muyucloud.croparia.dynamics.api.repo.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ItemBatch implements Repo<ItemSpec>, Iterable<Map.Entry<ItemSpec, Long>> {
    private final ArrayList<ItemUnit> units = new ArrayList<>();

    public ItemBatch(List<ItemUnit> units) {
        this.units.addAll(units);
        this.units.trimToSize();
    }

    public void load(JsonArray json) {
        for (int i = 0; i < json.size(); i++) {
            JsonObject unit = json.get(i).getAsJsonObject();
            units.get(i).load(unit);
        }
    }

    public void save(JsonArray json) {
        for (int i = 0; i < json.size(); i++) {
            JsonObject unit = new JsonObject();
            units.get(i).save(unit);
            json.add(unit);
        }
    }

    public boolean shouldUpdateRecipe() {
        for (ItemUnit unit : units) {
            if (unit.shouldUpdateRecipe()) {
                return true;
            }
        }
        return false;
    }

    public void add(ItemUnit... unit) {
        units.addAll(List.of(unit));
        units.trimToSize();
    }

    public ItemUnit remove(int i) {
        return units.remove(i);
    }

    public ItemAgent toAgent() {
        return ItemAgent.of(() -> this);
    }

    @Override
    public int size() {
        return units.size();
    }

    @Override
    public boolean isEmpty(int i) {
        return units.get(i).isEmpty(0);
    }

    @Override
    public ItemSpec resourceFor(int i) {
        return units.get(i).resourceFor(0);
    }

    @Override
    public long simConsume(int i, ItemSpec resource, long amount) {
        return units.get(i).simConsume(0, resource, amount);
    }

    @Override
    public long consume(int i, ItemSpec resource, long amount) {
        return units.get(i).consume(0, resource, amount);
    }

    @Override
    public long simAccept(int i, ItemSpec resource, long amount) {
        return units.get(i).simAccept(0, resource, amount);
    }

    @Override
    public long accept(int i, ItemSpec resource, long amount) {
        return units.get(i).accept(0, resource, amount);
    }

    @Override
    public long capacityFor(int i, ItemSpec resource) {
        return units.get(i).capacityFor(0, resource);
    }

    @Override
    public long amountFor(int i, ItemSpec resource) {
        return units.get(i).amountFor(0, resource);
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<ItemSpec, Long>> iterator() {
        return new BatchIterator();
    }

    class BatchIterator implements Iterator<Map.Entry<ItemSpec, Long>> {
        private final Iterator<ItemUnit> iterator = units.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map.Entry<ItemSpec, Long> next() {
            ItemUnit unit = iterator.next();
            return Map.entry(unit.getItem(), unit.getAmount());
        }
    }
}
