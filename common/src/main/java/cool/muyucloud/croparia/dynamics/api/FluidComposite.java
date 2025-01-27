package cool.muyucloud.croparia.dynamics.api;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public abstract class FluidComposite implements FluidRepo {
    /**
     * Creates a new FluidStorage instance.
     *
     * @param divide   whether IO operation can be divided into each sub repo
     * @param children sub repos
     * @return a new FluidStorage instance
     */
    @ExpectPlatform
    static FluidComposite create(boolean divide, FluidComposite... children) {
        throw new IllegalStateException("Cannot create FluidInteraction");
    }

    protected final FluidRepo[] children;
    protected final boolean divide;

    public FluidComposite(boolean divide, @NotNull FluidRepo... children) {
        this.divide = divide;
        this.children = children;
    }

    @Override
    public boolean isEmpty() {
        for (FluidRepo child : this.children) {
            if (!child.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether this repo separate IO operation into each sub repo.<br>
     * Influenced methods:
     * <li>{@link #canConsume(Fluid, long)}</li>
     * <li>{@link #canAccept(Fluid, long)}</li>
     * <li>{@link #consume(Fluid, long)}</li>
     * <li>{@link #accept(Fluid, long)}</li>
     * <li>{@link #spaceFor(Fluid)}</li>
     * <li>{@link #capacityFor(Fluid)}</li>
     * <li>{@link #amountFor(Fluid)}</li>
     */
    public boolean canDivide() {
        return this.divide;
    }

    /**
     * Whether you can consume the specified amount of fluid from this repo.<br>
     *
     * @param fluid  the type of fluid to consume
     * @param amount the amount of fluid to consume
     * @return If {@link #canDivide()} is false, return {@code true} only when one of the sub repos can <b>fully</b> accept the fluid.
     * Otherwise, return {@code true} when space from all sub repos is enough.
     */
    @Override
    public boolean canConsume(Fluid fluid, long amount) {
        if (this.canDivide()) {
            long remain = amount;
            for (int i = 0; i < this.children.length && remain > 0; i++) {
                remain -= this.children[i].amountFor(fluid);
            }
            return remain <= 0;
        } else {
            for (FluidRepo child : this.children) {
                if (child.canConsume(fluid, amount)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Whether this repo can accept the specified amount of fluid.<br>
     * If {@link #canDivide()} is false, this method returns true only when one of the sub repos can accept <b>all</b> the fluid.
     * Otherwise, this method returns true when space from all sub repos is enough.
     *
     * @param fluid  the type of fluid to consume
     * @param amount the amount of fluid to consume
     * @return If {@link #canDivide()} is false, return {@code true} only when one of the sub repos can <b>fully</b> meet the amount.
     * Otherwise, return {@code true} when total amount from all sub repos is enough.
     */
    @Override
    public boolean canAccept(Fluid fluid, long amount) {
        if (this.canDivide()) {
            long remain = amount;
            for (int i = 0; i < this.children.length && remain > 0; i++) {
                remain -= this.children[i].spaceFor(fluid);
            }
            return remain <= 0;
        } else {
            for (FluidRepo child : this.children) {
                if (child.canAccept(fluid, amount)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Consumes the specified amount of fluid from this repo.<br>
     * If {@link #canDivide()} is false, this method only takes fluid from repo with maximum amount.
     * Otherwise, it will take fluid from all sub repos until the amount is met.
     *
     * @param fluid  the type of fluid to consume
     * @param amount the amount of fluid to consume
     * @return the amount of fluid actually consumed
     */
    @Override
    public long consume(Fluid fluid, long amount) {
        if (this.canDivide()) {
            long remain = amount;
            for (int i = 0; i < this.children.length && remain > 0; i++) {
                remain -= this.children[i].consume(fluid, remain);
            }
            return amount - remain;
        } else {
            long maxSpace = 0;
            FluidRepo maxRepo = null;
            for (FluidRepo child : this.children) {
                long space = child.spaceFor(fluid);
                if (space >= amount) {
                    return child.consume(fluid, amount);
                }
                if (maxSpace < space) {
                    maxRepo = child;
                }
            }
            if (maxRepo != null) {
                return maxRepo.consume(fluid, amount);
            }
            return 0;
        }
    }

    /**
     * Consumes the specified amount of fluid from this repo.<br>
     * If {@link #canDivide()} is false, this method only put fluid into repo with maximum space.
     * Otherwise, it will put fluid into all sub repos until the amount is met.
     *
     * @param fluid  the type of fluid to consume
     * @param amount the amount of fluid to consume
     * @return the amount of fluid actually consumed
     */
    @Override
    public long accept(Fluid fluid, long amount) {
        if (this.canDivide()) {
            long remain = amount;
            for (int i = 0; i < this.children.length && remain > 0; i++) {
                remain -= this.children[i].accept(fluid, remain);
            }
            return amount - remain;
        } else {
            for (FluidRepo child : this.children) {
                if (child.canAccept(fluid, 0)) {
                    return child.accept(fluid, amount);
                }
            }
            return 0;
        }
    }

    /**
     * The available space for the specified fluid.<br>
     * if {@link #canDivide()}, this method returns the total space from all sub repos.
     * Otherwise, this method returns the maximum space from one of the sub repos.
     *
     * @param fluid the type of fluid
     * @return the available space
     */
    @Override
    public long spaceFor(Fluid fluid) {
        if (this.canDivide()) {
            long space = 0;
            for (FluidRepo child : this.children) {
                space += child.spaceFor(fluid);
            }
            return space;
        } else {
            long maxSpace = 0;
            for (FluidRepo child : this.children) {
                maxSpace = Math.max(maxSpace, child.spaceFor(fluid));
            }
            return maxSpace;
        }
    }

    /**
     * The capacity for the specified fluid.<br>
     * if {@link #canDivide()}, this method returns the total capacity from all sub repos.
     * Otherwise, this method returns the maximum capacity from one of the sub repos.
     *
     * @param fluid the type of fluid
     * @return the capacity
     */
    @Override
    public long capacityFor(Fluid fluid) {
        if (this.canDivide()) {
            long capacity = 0;
            for (FluidRepo child : this.children) {
                capacity += child.capacityFor(fluid);
            }
            return capacity;
        } else {
            long maxCapacity = 0;
            for (FluidRepo child : this.children) {
                maxCapacity = Math.max(maxCapacity, child.capacityFor(fluid));
            }
            return maxCapacity;
        }
    }

    /**
     * The amount of the specified fluid.<br>
     * if {@link #canDivide()}, this method returns the total amount from all sub repos.
     * Otherwise, this method returns the maximum amount from one of the sub repos.
     *
     * @param fluid the type of fluid
     * @return the amount
     */
    @Override
    public long amountFor(Fluid fluid) {
        if (this.canDivide()) {
            long amount = 0;
            for (FluidRepo child : this.children) {
                amount += child.amountFor(fluid);
            }
            return amount;
        } else {
            long maxAmount = 0;
            for (FluidRepo child : this.children) {
                maxAmount = Math.max(maxAmount, child.amountFor(fluid));
            }
            return maxAmount;
        }
    }

    @Override
    public Iterator<FluidUnit> units() {
        return new FluidCompositeIterator();
    }

    class FluidCompositeIterator implements Iterator<FluidUnit> {
        private final FluidComposite repo;
        private final int size;
        private int i = 0;
        private Iterator<FluidUnit> iterator = null;

        public FluidCompositeIterator() {
            this.repo = FluidComposite.this;
            this.size = repo.children.length;
        }

        @Override
        public boolean hasNext() {
            return i >= size;
        }

        @Override
        public FluidUnit next() {
            if (this.iterator != null && this.iterator.hasNext()) {
                return this.iterator.next();
            } else if (this.hasNext()) {
                FluidRepo child = repo.children[i];
                i++;
                if (child instanceof FluidUnit unit) {
                    return unit;
                } else {
                    this.iterator = child.units();
                    return this.iterator.next();
                }
            } else {
                return null;
            }
        }
    }
}
