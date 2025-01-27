package cool.muyucloud.croparia.dynamics.api.forge;

import cool.muyucloud.croparia.dynamics.api.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidUnitImpl extends FluidUnit implements IFluidTank {
    @SafeVarargs
    public FluidUnitImpl(long capacity, RepoFlag flag, Predicate<Fluid>... predicates) {
        super(capacity, flag, predicates);
    }

    @Override
    public int getFluidAmount() {
        return (int) (this.getAmount() / 81);
    }

    @Override
    public int getCapacity() {
        return (int) (this.capacity() / 81);
    }

    @Override
    public boolean isFluidValid(FluidStack fluidStack) {
        return this.testFluid(fluidStack.getFluid());
    }

    @Override
    public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
        return (int) (this.accept(fluidStack.getFluid(), fluidStack.getAmount() * 81L) / 81);
    }

    @Override
    public @NotNull FluidStack drain(int i, IFluidHandler.FluidAction fluidAction) {

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
