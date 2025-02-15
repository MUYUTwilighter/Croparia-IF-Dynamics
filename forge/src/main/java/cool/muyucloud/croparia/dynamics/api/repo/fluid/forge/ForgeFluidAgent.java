package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.PlatformFluidAgent;
import cool.muyucloud.croparia.dynamics.api.resource.type.FluidSpec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

/**
 * Wrapper for {@link IFluidHandler}.
 * The fluid unit for params & return values is {@code 81000 = 1 bucket}.
 */
public class ForgeFluidAgent implements PlatformFluidAgent {
    public static PlatformFluidAgent of(IFluidHandler handler) {
        return new ForgeFluidAgent(handler);
    }

    private final IFluidHandler handler;

    public ForgeFluidAgent(IFluidHandler handler) {
        this.handler = handler;
    }

    public IFluidHandler get() {
        return this.handler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Repo<FluidSpec>> peel() {
        return this.get() instanceof Repo<?> repo ? Optional.of((Repo<FluidSpec>) repo) : Optional.empty();
    }

    @Override
    public int size() {
        return this.get().getTanks();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.get().getFluidInTank(i).isEmpty();
    }

    @Override
    public long simConsume(FluidSpec fluid, long amount) {
        return this.get().drain(ForgeFluidSpec.of(fluid, amount), IFluidHandler.FluidAction.SIMULATE).getAmount() * 81L;
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        FluidStack stored = this.get().getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(resource, Math.min(stored.getAmount(), amount));
        if (ForgeFluidSpec.matches(resource, stored)) {
            return this.get().drain(wanted, IFluidHandler.FluidAction.SIMULATE).getAmount() * 81L;
        }
        return 0;
    }

    @Override
    public long consume(FluidSpec resource, long amount) {
        return this.get().drain(ForgeFluidSpec.of(resource, amount), IFluidHandler.FluidAction.EXECUTE).getAmount() * 81L;
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        FluidStack stored = this.get().getFluidInTank(i);
        FluidStack wanted = ForgeFluidSpec.of(resource, amount);
        if (stored.containsFluid(wanted) && stored.getAmount() >= wanted.getAmount()) {
            return this.consume(resource, Math.min(amount, stored.getAmount() * 81L));
        }
        return 0;
    }

    @Override
    public long simAccept(FluidSpec resource, long amount) {
        return this.get().fill(ForgeFluidSpec.of(resource, amount), IFluidHandler.FluidAction.SIMULATE) * 81L;
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        FluidStack stored = this.get().getFluidInTank(i);
        int capacity = this.get().getTankCapacity(i);
        FluidStack wanted = ForgeFluidSpec.of(resource, Math.min(capacity - stored.getAmount(), amount / 81L));
        if (ForgeFluidSpec.matches(resource, stored) || stored.isEmpty()) {
            return this.get().fill(wanted, IFluidHandler.FluidAction.SIMULATE) * 81L;
        } else {
            return 0;
        }
    }

    @Override
    public long accept(FluidSpec fluid, long amount) {
        return this.get().fill(ForgeFluidSpec.of(fluid, amount), IFluidHandler.FluidAction.EXECUTE) * 81L;
    }

    @Override
    public long accept(int i, FluidSpec fluid, long amount) {
        FluidStack stored = this.get().getFluidInTank(i);
        int capacity = this.get().getTankCapacity(i);
        FluidStack wanted = ForgeFluidSpec.of(fluid, Math.min(capacity - stored.getAmount(), amount / 81L));
        if (ForgeFluidSpec.matches(fluid, stored) || stored.isEmpty()) {
            return this.get().fill(wanted, IFluidHandler.FluidAction.SIMULATE) * 81L;
        } else {
            return 0;
        }
    }

    @Override
    public long capacityFor(int i, FluidSpec fluid) {
        FluidStack stored = this.get().getFluidInTank(i);
        if (stored.isEmpty() && this.get().isFluidValid(i, ForgeFluidSpec.of(fluid, 1))) {
            return this.get().getTankCapacity(i) * 81L;
        } else {
            return ForgeFluidSpec.matches(fluid, stored) ? this.get().getTankCapacity(i) * 81L : 0;
        }
    }

    @Override
    public long amountFor(int i, FluidSpec fluid) {
        FluidStack stack = this.get().getFluidInTank(i);
        return ForgeFluidSpec.matches(fluid, stack) ? stack.getAmount() * 81L : 0;
    }

    @Override
    public FluidSpec resourceFor(int i) {
        return ForgeFluidSpec.from(this.get().getFluidInTank(i));
    }
}
