package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.minecraftforge.fluids.FluidStack;

public class ForgeFluidSpec {
    public static FluidStack of(FluidSpec fluidSpec, long amount) {
        return new FluidStack(fluidSpec.getFluid(), (int) Math.min(amount / 81L, Integer.MAX_VALUE), fluidSpec.getNbt());
    }

    public static FluidStack of(FluidSpec fluidSpec, int amount) {
        return new FluidStack(fluidSpec.getFluid(), amount, fluidSpec.getNbt());
    }

    public static FluidSpec from(FluidStack stack) {
        return new FluidSpec(stack.getFluid(), stack.getTag());
    }

    public static boolean matches(FluidSpec a, FluidStack b) {
        return a.getFluid() == b.getFluid() && a.getNbt().equals(b.getTag());
    }
}
