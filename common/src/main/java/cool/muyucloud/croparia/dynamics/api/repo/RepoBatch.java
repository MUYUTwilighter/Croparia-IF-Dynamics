package cool.muyucloud.croparia.dynamics.api.repo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class RepoBatch<T> implements Repo<T>, Iterable<RepoUnit<T>> {
    private final ArrayList<RepoUnit<T>> units = new ArrayList<>();

    @SafeVarargs
    public RepoBatch(RepoUnit<T>... units) {
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

    public boolean shouldUpdateRecipe() {
        for (RepoUnit<T> unit : units) {
            if (unit.shouldUpdateRecipe()) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public final void add(RepoUnit<T>... unit) {
        units.addAll(List.of(unit));
        units.trimToSize();
    }

    public RepoUnit<T> remove(int i) {
        return units.remove(i);
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
    public T resourceFor(int i) {
        return units.get(i).resourceFor(0);
    }

    @Override
    public long simConsume(int i, T resource, long amount) {
        return units.get(i).simConsume(0, resource, amount);
    }

    @Override
    public long consume(int i, T resource, long amount) {
        return units.get(i).consume(0, resource, amount);
    }

    @Override
    public long simAccept(int i, T resource, long amount) {
        return units.get(i).simAccept(0, resource, amount);
    }

    @Override
    public long accept(int i, T resource, long amount) {
        return units.get(i).accept(0, resource, amount);
    }

    @Override
    public long capacityFor(int i, T resource) {
        return units.get(i).capacityFor(0, resource);
    }

    @Override
    public long amountFor(int i, T resource) {
        return units.get(i).amountFor(0, resource);
    }

    @NotNull
    @Override
    public Iterator<RepoUnit<T>> iterator() {
        return units.iterator();
    }
}
