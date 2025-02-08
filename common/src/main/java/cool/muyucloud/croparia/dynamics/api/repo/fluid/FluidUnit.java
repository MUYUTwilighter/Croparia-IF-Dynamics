package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.RepoUnit;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class FluidUnit extends RepoUnit<FluidSpec> {
    public static FluidUnit of(Predicate<FluidSpec> fluidFilter, long capacity) {
        return new FluidUnit(fluidFilter, capacity);
    }

    public static FluidUnit[] of(Predicate<FluidSpec> fluidFilter, long capacity, int count) {
        FluidUnit[] units = new FluidUnit[count];
        for (int i = 0; i < count; i++) {
            units[i] = new FluidUnit(fluidFilter, capacity);
        }
        return units;
    }

    @NotNull
    private FluidSpec fluid = FluidSpec.EMPTY;

    public FluidUnit(Predicate<FluidSpec> fluidFilter, long capacity) {
        super(fluidFilter, capacity);
    }

    @Override
    public TypeToken<FluidSpec> getType() {
        return FluidSpec.TYPE;
    }

    public void load(JsonObject json) {
        super.load(json);
        if (json.has("fluid")) {
            this.fluid = FluidSpec.CODEC.codec().decode(JsonOps.INSTANCE, json.get("fluid")).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode fluid: %s".formatted(msg))
            ).getFirst();
        }
    }

    public void save(JsonObject json) {
        super.save(json);
        json.add("fluid", FluidSpec.CODEC.codec().encodeStart(JsonOps.INSTANCE, this.fluid).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode fluid: %s".formatted(msg))
        ));
    }

    @Override
    public @NotNull FluidSpec getResource() {
        return this.fluid;
    }

    @Override
    public void setResource(@NotNull FluidSpec fluid) {
        if (this.getAmount() == 0 && this.isFluidValid(fluid)) {
            this.fluid = fluid;
        }
    }

    @Override
    public boolean isEmpty(int i) {
        return this.getAmount() <= 0 || this.getResource().isEmpty();
    }
}
