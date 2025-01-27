package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class FluidUnitImpl extends FluidUnit implements StorageView<FluidVariant>, Storage<FluidVariant> {
    @SafeVarargs
    public FluidUnitImpl(long capacity, RepoFlag flag, Predicate<Fluid>... predicates) {
        super(capacity, flag, predicates);
    }

    @Override
    public long insert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        return this.accept(fluidVariant.getFluid(), maxAmount);
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return new FluidUnitIterator();
    }

    @Override
    public long extract(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        return this.consume(fluidVariant.getFluid(), maxAmount);
    }

    @Override
    public boolean isResourceBlank() {
        return this.isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.of(this.fluid());
    }

    @Override
    public long getAmount() {
        return this.amount();
    }

    @Override
    public long getCapacity() {
        return this.capacity();
    }

    class FluidUnitIterator implements Iterator<StorageView<FluidVariant>> {
        private final Iterator<FluidUnit> iterator;

        public FluidUnitIterator() {
            this.iterator = FluidUnitImpl.this.units();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public StorageView<FluidVariant> next() {
            return (FluidUnitImpl) this.iterator.next();
        }
    }
}
