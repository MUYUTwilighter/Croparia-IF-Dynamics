package cool.muyucloud.croparia.dynamics.api.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidComposite;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FluidCompositeImpl extends FluidComposite implements IFluidHandler {
    private final List<FluidUnit> units;

    public FluidCompositeImpl(boolean divide, @NotNull FluidRepo... children) {
        super(divide, children);
        List<FluidUnit> units = new LinkedList<>();
        Iterator<FluidUnit> iterator = this.units();
        while (iterator.hasNext()) {
            units.add(iterator.next());
        }
        this.units = List.copyOf(units);
    }

    @Override
    public int getTanks() {
        return units.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        FluidUnit unit = units.get(i);
        return new FluidStack(unit.fluid(), (int) (unit.amount() / 81L));
    }

    @Override
    public int getTankCapacity(int i) {
        return (int) (units.get(i).capacity() / 81L);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return units.get(i).canAccept(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return (int) (this.accept(fluidStack.getFluid(), fluidStack.getAmount() * 81L) / 81);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        long amount = this.consume(fluidStack.getFluid(), fluidStack.getAmount() * 81L);
        return new FluidStack(fluidStack.getFluid(), (int) (amount / 81));
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        if (this.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidUnit unit = units.get(0);
        long amount = unit.amount();
        unit.setAmount(0);
        return new FluidStack(unit.fluid(), (int) (amount / 81));
    }
}
