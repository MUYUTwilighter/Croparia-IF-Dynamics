package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FluidAgentImpl extends FluidAgent implements IFluidHandler {
    public static FluidAgent of(Supplier<FluidRepo> repo) {
        return new FluidAgentImpl(repo);
    }

    public FluidAgentImpl(Supplier<FluidRepo> repo) {
        super(repo);
    }

    @Override
    public int getTanks() {
        return this.get().size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return new FluidStack(this.fluidFor(i), (int) (this.amountFor(i, this.fluidFor(i)) / 81L));
    }

    @Override
    public int getTankCapacity(int i) {
        return (int) (this.capacityFor(i, this.fluidFor(i)) / 81L);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return this.canAccept(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return (int) (this.accept(fluidStack.getFluid(), fluidStack.getAmount() * 81L) / 81L);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        long amount = this.consume(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
        return new FluidStack(fluidStack.getFluid(), (int) (amount / 81L));
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        Fluid fluid = this.fluidFor(i);
        long amount = this.consume(i, fluid, this.capacityFor(i, fluid));
        return new FluidStack(fluid, (int) (amount / 81L));
    }
}
