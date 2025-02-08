package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import cool.muyucloud.croparia.api.element.ElementsEnum;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.registry.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
public class CrucibleBatch implements Repo<FluidSpec>, Iterable<FluidUnit> {
    private final Map<ElementsEnum, FluidUnit> units = ImmutableMap.of(
        ElementsEnum.EARTH, FluidUnit.of(fluid -> fluid.is(Fluids.EARTH.get()), 81000 * 3),
        ElementsEnum.WATER, FluidUnit.of(fluid -> fluid.is(Fluids.WATER.get()), 81000 * 3),
        ElementsEnum.FIRE, FluidUnit.of(fluid -> fluid.is(Fluids.FIRE.get()), 81000 * 3),
        ElementsEnum.AIR, FluidUnit.of(fluid -> fluid.is(Fluids.AIR.get()), 81000 * 4)
    );

    @Override
    public TypeToken<?> getType() {
        return FluidSpec.TYPE;
    }

    public void enable(ElementsEnum element) {
        units.get(element).setAcceptable(true);
        units.get(element).setConsumable(true);
    }

    public void disable(ElementsEnum element) {
        units.get(element).setAcceptable(false);
        units.get(element).setConsumable(false);
    }

    public boolean isEnabled(ElementsEnum element) {
        return units.get(element).isAcceptable() && units.get(element).isConsumable();
    }

    public boolean isDisabled(ElementsEnum element) {
        return !isEnabled(element);
    }

    public void load(JsonObject json) {
        for (String key : json.keySet()) {
            JsonObject unitJson = json.get(key).getAsJsonObject();
            ElementsEnum element = ElementsEnum.valueOf(key.toUpperCase());
            units.get(element).load(unitJson);
        }
    }

    public void save(JsonObject json) {
        for (ElementsEnum element : units.keySet()) {
            JsonObject unitJson = new JsonObject();
            units.get(element).save(unitJson);
            json.add(element.getSerializedName(), unitJson);
        }
    }

    public float getSpeedEffect() {
        FluidUnit unit = units.get(ElementsEnum.FIRE);
        if (this.isDisabled(ElementsEnum.FIRE) || unit.isEmpty(0)) return 1;
        return units.get(ElementsEnum.FIRE).getAmount() * 3F / 81000 / 4 + 1;
    }

    public float getFuelEffect() {
        FluidUnit unit = units.get(ElementsEnum.AIR);
        if (this.isDisabled(ElementsEnum.AIR) || unit.isEmpty(0)) return 1F;
        return 81000F / (81000F + unit.getAmount() * 3F / 4F);
    }

    public float getItemEffect() {
        FluidUnit unit = units.get(ElementsEnum.EARTH);
        if (this.isDisabled(ElementsEnum.EARTH) || unit.isEmpty(0)) return 1F;
        return unit.getAmount() / 81000F / 8F;
    }

    public float getFluidEffect() {
        FluidUnit unit = units.get(ElementsEnum.WATER);
        if (this.isDisabled(ElementsEnum.WATER) || unit.isEmpty(0)) return 1F;
        return unit.getAmount() / 81000F / 8F;
    }

    public boolean canAffectItem() {
        return this.isEnabled(ElementsEnum.EARTH) && this.units.get(ElementsEnum.EARTH).getAmount() >= 20250 && this.getItemEffect() > Math.random();
    }

    public boolean canAffectFluid() {
        return this.isEnabled(ElementsEnum.WATER) && this.units.get(ElementsEnum.WATER).getAmount() >= 20250 && this.getFluidEffect() > Math.random();
    }

    public void onItemAffected() {
        this.consume(FluidSpec.of(Fluids.EARTH.get()), 20250);
    }

    public void onFluidAffected() {
        this.consume(FluidSpec.of(Fluids.WATER.get()), 20250);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty(int i) {
        return false;
    }

    @Override
    public FluidSpec resourceFor(int i) {
        return null;
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        return 0;
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        return 0;
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        return 0;
    }

    @Override
    public long accept(int i, FluidSpec resource, long amount) {
        return 0;
    }

    @Override
    public long capacityFor(int i, FluidSpec resource) {
        return 0;
    }

    @Override
    public long amountFor(int i, FluidSpec resource) {
        return 0;
    }

    @NotNull
    @Override
    public Iterator<FluidUnit> iterator() {
        return units.values().iterator();
    }
}
