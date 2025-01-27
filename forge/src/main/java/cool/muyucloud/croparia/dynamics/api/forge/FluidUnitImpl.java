package cool.muyucloud.croparia.dynamics.api.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidUnitImpl extends FluidUnit implements IFluidTank, IFluidHandler {
    @SafeVarargs
    public FluidUnitImpl(long capacity, RepoFlag flag, Predicate<Fluid>... predicates) {
        super(capacity, flag, predicates);
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return new FluidStack(this.fluid(), (int) (this.amount() / 81L));
    }

    @Override
    public int getFluidAmount() {
        return (int) (this.amount() / 81L);
    }

    @Override
    public int getCapacity() {
        return (int) (this.capacity() / 81L);
    }

    @Override
    public boolean isFluidValid(FluidStack fluidStack) {
        return this.testFluid(fluidStack.getFluid());
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return i == 0 ? getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int i) {
        return i == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return i == 0 && this.canAccept(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
    }

    @Override
    public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
        return (int) (this.accept(fluidStack.getFluid(), fluidStack.getAmount() * 81L) / 81);
    }

    @Override
    public @NotNull FluidStack drain(int i, IFluidHandler.FluidAction fluidAction) {
        if (fluidAction == IFluidHandler.FluidAction.SIMULATE) {
            long amount = this.amount();
            return new FluidStack(this.fluid(), (int) (amount / 81L));
        } else {
            Fluid fluid = this.fluid();
            long amount = this.amount();
            this.setAmount(0);
            return new FluidStack(fluid, (int) (amount / 81L));
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
        if (fluidAction == IFluidHandler.FluidAction.SIMULATE) {
            long amount = this.amountFor(fluidStack.getFluid());
            return new FluidStack(fluidStack.getFluid(), (int) (amount / 81));
        } else {
            long amount = this.consume(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
            return new FluidStack(fluidStack.getFluid(), (int) (amount / 81));
        }
    }
}
