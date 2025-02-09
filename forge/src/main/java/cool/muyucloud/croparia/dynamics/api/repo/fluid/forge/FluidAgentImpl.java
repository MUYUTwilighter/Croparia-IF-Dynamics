package cool.muyucloud.croparia.dynamics.api.repo.fluid.forge;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.resource.type.FluidSpec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FluidAgentImpl extends FluidAgent implements IFluidHandler {
    public static FluidAgent of(Supplier<Repo<FluidSpec>> repo) {
        return new FluidAgentImpl(repo);
    }

    public FluidAgentImpl(Supplier<Repo<FluidSpec>> repo) {
        super(repo);
    }

    @Override
    public int getTanks() {
        return this.get().size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        FluidSpec fluidSpec = this.resourceFor(i);
        return ForgeFluidSpec.of(fluidSpec, this.amountFor(i, fluidSpec));
    }

    @Override
    public int getTankCapacity(int i) {
        return (int) (this.capacityFor(i, this.resourceFor(i)) / 81L);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack input) {
        FluidSpec fluid = ForgeFluidSpec.from(input);
        long amount = input.getAmount() * 81L;
        return this.simAccept(i, fluid, amount) >= amount;
    }

    @Override
    public int fill(FluidStack input, FluidAction fluidAction) {
        FluidSpec fluid = ForgeFluidSpec.from(input);
        if (fluidAction.simulate()) {
            return (int) (this.simAccept(fluid, input.getAmount() * 81L) / 81);
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
            long consumed = this.simConsume(fluid, input.getAmount() * 81L);
            return ForgeFluidSpec.of(fluid, consumed);
        } else if (fluidAction.execute()) {
            long consumed = this.consume(fluid, input.getAmount() * 81L);
            return ForgeFluidSpec.of(fluid, consumed);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public @NotNull FluidStack drain(int amount, FluidAction fluidAction) {
        if (this.size() < 1) return FluidStack.EMPTY;
        FluidSpec fluid = this.resourceFor(0);
        if (fluidAction.simulate()) {
            long consumed = this.simConsume(fluid, amount * 81L);
            return ForgeFluidSpec.of(fluid, consumed);
        } else if (fluidAction.execute()) {
            long consumed = this.consume(fluid, amount * 81L);
            return ForgeFluidSpec.of(fluid, consumed);
        } else {
            return FluidStack.EMPTY;
        }
    }
}
