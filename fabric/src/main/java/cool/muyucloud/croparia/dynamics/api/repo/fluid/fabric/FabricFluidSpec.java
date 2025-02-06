package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

@SuppressWarnings("UnstableApiUsage")
public class FabricFluidSpec {
    public static FluidVariant of(FluidSpec fluid) {
        return FluidVariant.of(fluid.getFluid(), fluid.getNbt().orElse(null));
    }

    public static FluidSpec from(FluidVariant fluid) {
        return new FluidSpec(fluid.getFluid(), fluid.getNbt());
    }

    public static boolean matches(FluidVariant a, FluidSpec b) {
        return a.getFluid() == b.getFluid() && a.nbtMatches(b.getNbt().orElse(null));
    }
}
