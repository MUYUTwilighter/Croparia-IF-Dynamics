package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Wrapper for {@link IFluidHandler}.
 * The fluid unit for params & return values is {@code 81000 = 1 bucket}.
 */
public class ForgeFluidAgent implements FluidRepo {
    public static FluidRepo of(IFluidHandler handler) {
        return new ForgeFluidAgent(handler);
    }

    private final IFluidHandler handler;

    public ForgeFluidAgent(IFluidHandler handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return this.handler.getTanks();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.handler.getFluidInTank(i).isEmpty();
    }

    @Override
    public boolean canConsume(FluidSpec fluid, long amount) {
        FluidStack wanted = ForgeFluidSpec.of(fluid, amount);
        FluidStack consumed = this.handler.drain(wanted, IFluidHandler.FluidAction.SIMULATE);
        return consumed.getAmount() * 81L >= amount;
    }

    @Override
    public boolean canConsume(int i, FluidSpec fluid, long amount) {
        FluidStack stored = this.handler.getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, amount);
        if (stored.containsFluid(wanted)) {
            return stored.getAmount() >= wanted.getAmount();
        } else {
            return false;
        }
    }

    @Override
    public boolean canAccept(int i, FluidSpec fluid, long amount) {
        return this.handler.isFluidValid(i, ForgeFluidSpec.of(fluid, amount));
    }

    @Override
    public boolean canAccept(FluidSpec fluid, long amount) {
        FluidStack wanted = ForgeFluidSpec.of(fluid, amount);
        return this.handler.drain(wanted, IFluidHandler.FluidAction.SIMULATE).getAmount() * 81L >= amount;
    }

    @Override
    public long consume(FluidSpec fluid, long amount) {
        return this.handler.drain(ForgeFluidSpec.of(fluid, amount), IFluidHandler.FluidAction.EXECUTE).getAmount() * 81L;
    }

    @Override
    public long consume(int i, FluidSpec fluid, long amount) {
        FluidStack stored = this.handler.getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, amount);
        if (stored.containsFluid(wanted) && stored.getAmount() >= wanted.getAmount()) {
            return this.consume(fluid, Math.min(amount, stored.getAmount() * 81L));
        }
        return 0;
    }

    @Override
    public long accept(FluidSpec fluid, long amount) {
        return this.handler.fill(ForgeFluidSpec.of(fluid, amount), IFluidHandler.FluidAction.EXECUTE) * 81L;
    }

    @Override
    public long accept(int i, FluidSpec fluid, long amount) {
        FluidStack stored = this.handler.getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, this.spaceFor(i, fluid));
        if (stored.isEmpty() || stored.containsFluid(wanted)) {
            return this.handler.fill(wanted, IFluidHandler.FluidAction.EXECUTE) * 81L;
        }
        return 0;
    }

    @Override
    public long spaceFor(FluidSpec fluid) {
        return this.handler.fill(ForgeFluidSpec.of(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE) * 81L;
    }

    @Override
    public long spaceFor(int i, FluidSpec fluid) {
        int capacity = this.handler.getTankCapacity(i);
        int amount = this.handler.getFluidInTank(i).getAmount();
        int space = capacity - amount;
        if (this.handler.isFluidValid(i, ForgeFluidSpec.of(fluid, space))) {
            return space * 81L;
        } else {
            return 0;
        }
    }

    @Override
    public long capacityFor(int i, FluidSpec fluid) {
        FluidStack stored = this.handler.getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, Integer.MAX_VALUE);
        if (stored.isEmpty() && this.handler.isFluidValid(i, ForgeFluidSpec.of(fluid, 1))) {
            return this.handler.getTankCapacity(i) * 81L;
        } else {
            return stored.containsFluid(wanted) ? this.handler.getTankCapacity(i) * 81L : 0;
        }
    }

    @Override
    public long amountFor(int i, FluidSpec fluid) {
        FluidStack stack = this.handler.getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, Integer.MAX_VALUE);
        return stack.containsFluid(wanted) ? stack.getAmount() * 81L : 0;
    }

    @Override
    public FluidSpec fluidFor(int i) {
        return ForgeFluidSpec.from(this.handler.getFluidInTank(i));
    }
}
