package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
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
        FluidSpec fluidSpec = this.fluidFor(i);
        return ForgeFluidSpec.of(fluidSpec, this.amountFor(i, fluidSpec));
    }

    @Override
    public int getTankCapacity(int i) {
        return (int) (this.capacityFor(i, this.fluidFor(i)) / 81L);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        FluidSpec fluid = ForgeFluidSpec.from(fluidStack);
        return this.canAccept(fluid, fluidStack.getAmount() * 81L);
    }

    @Override
    public int fill(FluidStack input, FluidAction fluidAction) {
        FluidSpec fluid = ForgeFluidSpec.from(input);
        if (fluidAction.simulate()) {
            return this.canAccept(fluid, input.getAmount() * 81L) ? input.getAmount()
                : (int) (this.spaceFor(fluid) / 81L);
        } else if (fluidAction.execute()) {
            return (int) (this.accept(fluid, input.getAmount() * 81L) / 81L);
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack input, FluidAction fluidAction) {
        FluidSpec fluid = ForgeFluidSpec.from(input);
        if (fluidAction.simulate()) {
            return this.canConsume(fluid, input.getAmount() * 81L) ? input :
                new FluidStack(input.getFluid(), (int) (this.amountFor(fluid) / 81L));
        } else if (fluidAction.execute()) {
            long amount = this.consume(fluid, input.getAmount() * 81L);
            return new FluidStack(input.getFluid(), (int) (amount / 81L));
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public @NotNull FluidStack drain(int amount, FluidAction fluidAction) {
        if (this.size() < 1) return FluidStack.EMPTY;
        FluidSpec fluid = this.fluidFor(0);
        if (fluidAction.simulate()) {
            long available = Math.min(this.amountFor(fluid), amount * 81L);
            if (this.canConsume(fluid, available)) {
                return ForgeFluidSpec.of(fluid, available);
            }
            return FluidStack.EMPTY;
        } else if (fluidAction.execute()) {
            long consumed = this.consume(fluid, amount * 81L);
            return ForgeFluidSpec.of(fluid, (int) consumed);
        } else {
            return FluidStack.EMPTY;
        }
    }
}
