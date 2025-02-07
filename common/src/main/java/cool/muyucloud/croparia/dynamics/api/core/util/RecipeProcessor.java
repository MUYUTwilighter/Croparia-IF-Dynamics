package cool.muyucloud.croparia.dynamics.api.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class RecipeProcessor<F> {
    private final ArrayList<RecipeProcessorUnit<F>> units = new ArrayList<>();

    @SafeVarargs
    public RecipeProcessor(RecipeProcessorUnit<F>... units) {
        this.units.addAll(Arrays.asList(units));
    }

    public void tick() {
        for (RecipeProcessorUnit<F> unit : units) {
            unit.tick();
        }
    }

    public boolean isProcessing() {
        for (RecipeProcessorUnit<F> unit : units) {
            if (unit.isProcessing()) {
                return true;
            }
        }
        return false;
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
}
