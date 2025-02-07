package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.PlatformFluidAgent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage"})
public class FabricFluidAgent implements PlatformFluidAgent {
    @NotNull
    public static FabricFluidAgent of(@NotNull Storage<FluidVariant> storage) {
        return new FabricFluidAgent(storage);
    }

    private final Storage<FluidVariant> storage;

    public FabricFluidAgent(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    public Storage<FluidVariant> get() {
        return this.storage;
    }

    @Nullable
    public StorageView<FluidVariant> get(int i) {
        int v = 0;
        Iterator<StorageView<FluidVariant>> iterator = this.storage.iterator();
        StorageView<FluidVariant> view = null;
        while (iterator.hasNext() && i > v) {
            v++;
            view = iterator.next();
        }
        if (i != v || view == null) {
            return null;
        } else {
            return view;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Repo<FluidSpec>> peel() {
        return this.get() instanceof Repo<?> fluidRepo ? Optional.of((Repo<FluidSpec>) fluidRepo) : Optional.empty();
    }

    @Override
    public int size() {
        int i = 0;
        for (StorageView<FluidVariant> ignored : this.get()) {
            i++;
        }
        return i;
    }

    @Override
    public boolean isEmpty(int i) {
        StorageView<FluidVariant> view = this.get(i);
        return view == null || view.isResourceBlank();
    }

    @Nullable
    @Override
    public FluidSpec resourceFor(int i) {
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return null;
        } else {
            return FabricFluidSpec.from(view.getResource());
        }
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            return StorageUtil.simulateExtract(view, FabricFluidSpec.of(resource), amount, null);
        }
    }

    @Override
    public long consume(FluidSpec resource, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long result = this.get().extract(FabricFluidSpec.of(resource), amount, transaction);
            transaction.commit();
            return result;
        }
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            try (Transaction transaction = Transaction.openOuter()) {
                long result = view.extract(FabricFluidSpec.of(resource), amount, transaction);
                transaction.commit();
                return result;
            }
        }
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            try {
                @SuppressWarnings("unchecked")
                Storage<FluidVariant> storage = (Storage<FluidVariant>) s;
                return StorageUtil.simulateInsert(storage, FabricFluidSpec.of(resource), amount, null);
            } catch (ClassCastException e) {
                return 0L;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public long accept(int i, FluidSpec fluid, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            try {
                @SuppressWarnings("unchecked")
                Storage<FluidVariant> storage = (Storage<FluidVariant>) s;
                try (Transaction transaction = Transaction.openOuter()) {
                    long result = storage.insert(FabricFluidSpec.of(fluid), amount, transaction);
                    transaction.commit();
                    return result;
                }
            } catch (ClassCastException e) {
                return 0L;
            }
        }
    }

    @Override
    public long accept(FluidSpec fluid, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long result = this.get().insert(FabricFluidSpec.of(fluid), amount, transaction);
            transaction.commit();
            return result;
        }
    }

    @Override
    public long capacityFor(int i, FluidSpec fluid) {
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else if (view.isResourceBlank() || FabricFluidSpec.matches(view.getResource(), fluid)) {
            return view.getCapacity();
        } else {
            return 0L;
        }
    }

    @Override
    public long capacityFor(FluidSpec fluid) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<FluidVariant> view : this.get()) {
            if (FabricFluidSpec.matches(view.getResource(), fluid) || view.isResourceBlank()) {
                result += view.getCapacity();
            }
        }
        return result;
    }

    @Override
    public long amountFor(int i, FluidSpec fluid) {
        StorageView<FluidVariant> view = this.get(i);
        if (view == null || !FabricFluidSpec.matches(view.getResource(), fluid)) {
            return 0L;
        } else {
            return view.getAmount();
        }
    }

    @Override
    public long amountFor(FluidSpec fluid) {
        long result = 0L;
        for (StorageView<FluidVariant> view : this.get()) {
            if (FabricFluidSpec.matches(view.getResource(), fluid)) {
                result += view.getAmount();
            }
        }
        return result;
    }
}
