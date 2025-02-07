package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class FluidAgentImpl extends FluidAgent implements Storage<FluidVariant> {
    public static FluidAgent of(Supplier<Repo<FluidSpec>> repo) {
        return new FluidAgentImpl(repo);
    }

    protected FluidAgentImpl(Supplier<Repo<FluidSpec>> repo) {
        super(repo);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext context) {
        FluidSpec fluidSpec = FabricFluidSpec.from(resource);
        if (context == null) {
            return this.accept(fluidSpec, maxAmount);
        } else {
            long amount = this.simAccept(fluidSpec, maxAmount);
            context.addCloseCallback((ignored, result) -> {
                if (result == TransactionContext.Result.COMMITTED) {
                    this.accept(fluidSpec, amount);
                }
            });
            return amount;
        }
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext context) {
        FluidSpec fluidSpec = FabricFluidSpec.from(resource);
        if (context == null) {
            return this.consume(fluidSpec, maxAmount);
        } else {
            long amount = this.simConsume(fluidSpec, maxAmount);
            context.addCloseCallback((ignored, result) -> {
                if (result == TransactionContext.Result.COMMITTED) {
                    this.consume(fluidSpec, amount);
                }
            });
            return amount;
        }
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return new FluidIterator();
    }

    class FluidIterator implements Iterator<StorageView<FluidVariant>> {
        private int i = 0;

        @Override
        public boolean hasNext() {
            return this.i < FluidAgentImpl.this.size();
        }

        @Override
        public StorageView<FluidVariant> next() {
            return new FluidView(FluidAgentImpl.this, this.i++);
        }
    }

    static class FluidView implements StorageView<FluidVariant> {
        private final Repo<FluidSpec> repo;
        private final int i;

        public FluidView(Repo<FluidSpec> repo, int i) {
            if (repo.size() <= i) {
                throw new IllegalArgumentException("Index %s is out of bounds: %s".formatted(i, repo.size()));
            }
            this.repo = repo;
            this.i = i;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return this.repo.consume(i, FabricFluidSpec.from(resource), maxAmount);
        }

        @Override
        public boolean isResourceBlank() {
            return this.repo.isEmpty(i);
        }

        @Override
        public FluidVariant getResource() {
            return FabricFluidSpec.of(this.repo.resourceFor(i));
        }

        @Override
        public long getAmount() {
            return this.repo.amountFor(i, this.repo.resourceFor(i));
        }

        @Override
        public long getCapacity() {
            return this.repo.capacityFor(i, this.repo.resourceFor(i));
        }
    }
}
