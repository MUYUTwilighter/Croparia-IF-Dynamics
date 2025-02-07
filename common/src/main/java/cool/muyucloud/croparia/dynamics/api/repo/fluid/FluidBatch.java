package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class FluidBatch implements Repo<FluidSpec>, Iterable<FluidUnit> {
    protected final ArrayList<FluidUnit> units = new ArrayList<>();

    public FluidBatch(FluidUnit... units) {
        this.units.addAll(List.of(units));
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

    public void add(FluidUnit... unit) {
        units.addAll(List.of(unit));
        units.trimToSize();
    }

    public FluidUnit remove(int i) {
        return units.remove(i);
    }

    public FluidAgent toAgent() {
        return FluidAgent.of(() -> this);
    }

    public boolean shouldUpdateRecipe() {
        for (FluidUnit unit : units) {
            if (unit.shouldUpdateRecipe()) {
                return true;
            }
        }
        return false;
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
    public FluidSpec resourceFor(int i) {
        return units.get(i).resourceFor(0);
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        return units.get(i).simConsume(0, resource, amount);
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        return units.get(i).consume(0, resource, amount);
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        return units.get(i).simAccept(0, resource, amount);
    }

    @Override
    public long accept(int i, FluidSpec resource, long amount) {
        return units.get(i).accept(0, resource, amount);
    }

    @Override
    public long capacityFor(int i, FluidSpec resource) {
        return units.get(i).capacityFor(0, resource);
    }

    @Override
    public long amountFor(int i, FluidSpec resource) {
        return units.get(i).amountFor(0, resource);
    }

    @NotNull
    @Override
    public Iterator<FluidUnit> iterator() {
        return units.iterator();
    }
}
