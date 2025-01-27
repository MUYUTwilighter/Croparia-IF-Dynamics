package cool.muyucloud.croparia.dynamics.api.fabric;

import cool.muyucloud.croparia.dynamics.api.FluidComposite;
import cool.muyucloud.croparia.dynamics.api.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.FluidUnit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class FluidCompositeImpl extends FluidComposite implements Storage<FluidVariant> {
    public FluidCompositeImpl(boolean divide, @NotNull FluidRepo... children) {
        super(divide, children);
    }

    @Override
    public long insert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        return this.accept(fluidVariant.getFluid(), maxAmount);
    }

    @Override
    public long extract(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        return this.consume(fluidVariant.getFluid(), maxAmount);
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return new FluidRepoIterator();
    }

    class FluidRepoIterator implements Iterator<StorageView<FluidVariant>> {
        private final Iterator<FluidUnit> iterator;

        public FluidRepoIterator() {
            this.iterator = FluidCompositeImpl.this.units();
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
