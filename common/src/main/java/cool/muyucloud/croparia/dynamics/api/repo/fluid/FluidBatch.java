package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.repo.RepoBatch;
import cool.muyucloud.croparia.dynamics.api.repo.RepoUnit;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class FluidBatch extends RepoBatch<FluidSpec> {
    @SafeVarargs
    public static FluidBatch of(RepoUnit<FluidSpec>... units) {
        return new FluidBatch(units);
    }

    public static FluidBatch of(Predicate<FluidSpec> fluidFilter, long capacity, int count) {
        return new FluidBatch(FluidUnit.of(fluidFilter, capacity, count));
    }

    @SafeVarargs
    public FluidBatch(RepoUnit<FluidSpec>... units) {
        super(units);
    }

    public FluidAgent toAgent() {
        return FluidAgent.of(() -> this);
    }
}
