package cool.muyucloud.croparia.dynamics.api;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class FluidUnit implements FluidRepo {
    private final RepoFlag flag;
    private final Predicate<Fluid>[] predicates;
    private Fluid fluid;
    private long getCapacity;
    private long amount = 0;

    public FluidUnit(long capacity, RepoFlag flag, Predicate<Fluid>... predicates) {
        this.fluid = Fluids.EMPTY;
        this.getCapacity = capacity;
        this.flag = flag;
        this.predicates = predicates;
    }

    public long getAmount() {
        return this.amount;
    }

    public long capacity() {
        return this.getCapacity;
    }

    public RepoFlag getFlag() {
        return this.flag;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    protected void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setCapacity(long capacity) {
        this.getCapacity = capacity;
    }

    public boolean testFluid(Fluid fluid) {
        if (this.predicates.length == 0) {
            return true;
        }
        for (Predicate<Fluid> predicate : this.predicates) {
            if (predicate.test(fluid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.getAmount() == 0 || this.getFluid() == Fluids.EMPTY;
    }

    @Override
    public long amountFor(@Nullable Fluid fluid) {
        if (this.getFluid() == fluid) {
            return this.getAmount();
        } else {
            return 0;
        }
    }

    @Override
    public boolean canConsume(Fluid fluid, long amount) {
        if (!this.getFlag().isConsumable()) {
            return false;
        }
        if (this.getFluid() == fluid) {
            return this.getAmount() >= amount;
        } else {
            return false;
        }
    }

    @Override
    public boolean canAccept(Fluid fluid, long amount) {
        if (!this.getFlag().isAcceptable()) {
            return false;
        }
        if (this.isEmpty()) {
            return this.testFluid(fluid) && amount >= this.capacity();
        } else {
            return this.spaceFor(fluid) >= amount;
        }
    }

    @Override
    public long consume(Fluid fluid, long amount) {
        if (!this.getFlag().isConsumable()) {
            return 0;
        }
        if (this.getFluid() == fluid) {
            long decrement = Math.min(this.getAmount(), amount);
            this.setAmount(this.getAmount() - decrement);
            return decrement;
        } else {
            return 0;
        }
    }

    @Override
    public long accept(Fluid fluid, long amount) {
        if (!this.getFlag().isAcceptable()) {
            return 0;
        }
        if (this.isEmpty() && this.testFluid(fluid) && amount <= this.capacity()) {
            this.setFluid(fluid);
            this.setAmount(amount);
            return 0;
        } else if (this.getFluid() == fluid) {
            long increment = Math.min(this.spaceFor(fluid), amount);
            this.setAmount(this.getAmount() + increment);
            return increment;
        } else {
            return 0;
        }
    }

    @Override
    public long spaceFor(Fluid fluid) {
        if (this.isEmpty()) {
            return this.testFluid(fluid) ? this.capacity() : 0;
        } else if (this.getFluid() == fluid) {
            return Math.max(this.capacity() - this.getAmount(), 0);
        } else {
            return 0;
        }
    }

    @Override
    public long capacityFor(Fluid fluid) {
        if (this.isEmpty()) {
            return this.testFluid(fluid) ? this.capacity() : 0;
        } else if (this.getFluid() == fluid) {
            return this.capacity();
        } else {
            return 0;
        }
    }

    @Override
    public Iterator<FluidUnit> units() {
        return new FluidUnitIterator();
    }

    class FluidUnitIterator implements Iterator<FluidUnit> {
        boolean iterated = false;
        private final FluidUnit fluidUnit;

        public FluidUnitIterator() {
            this.fluidUnit = FluidUnit.this;
        }

        @Override
        public boolean hasNext() {
            return !iterated;
        }

        @Override
        public FluidUnit next() {
            if (this.hasNext()) {
                this.iterated = true;
                return this.fluidUnit;
            } else {
                return null;
            }
        }
    }
}
