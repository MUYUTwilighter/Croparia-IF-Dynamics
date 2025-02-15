package cool.muyucloud.croparia.dynamics.api.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class RecipeProcessor<F extends ResourceType> implements Iterable<RecipeProcessorUnit<F>> {
    private final ArrayList<RecipeProcessorUnit<F>> units = new ArrayList<>();

    @SafeVarargs
    public RecipeProcessor(RecipeProcessorUnit<F>... units) {
        this.units.addAll(Arrays.asList(units));
    }

    public void tick(MinecraftServer server) {
        for (RecipeProcessorUnit<F> unit : units) {
            unit.tick(server);
        }
    }

    public boolean isRunning() {
        for (RecipeProcessorUnit<F> unit : units) {
            if (unit.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public boolean isReady() {
        for (RecipeProcessorUnit<F> unit : units) {
            if (!unit.isReady()) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public final void add(RecipeProcessorUnit<F>... units) {
        this.units.addAll(List.of(units));
        this.units.trimToSize();
    }

    public int size() {
        return units.size();
    }

    public RecipeProcessorUnit<F> get(int i) {
        return units.get(i);
    }

    public void load(JsonArray json) {
        for (int i = 0; i < json.size(); i++) {
            JsonObject unit = json.get(i).getAsJsonObject();
            units.get(i).load(unit);
        }
    }

    public void load(ListTag nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundTag unit = nbt.getCompound(i);
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

    public void save(ListTag nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundTag unit = new CompoundTag();
            units.get(i).save(unit);
            nbt.add(unit);
        }
    }

    @NotNull
    @Override
    public Iterator<RecipeProcessorUnit<F>> iterator() {
        return units.iterator();
    }
}
