package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class FabricFluidAgent implements FluidRepo {
    @NotNull
    public static FabricFluidAgent of(@NotNull Storage<FluidVariant> storage) {
        return new FabricFluidAgent(storage);
    }

    private final Storage<FluidVariant> storage;

    public FabricFluidAgent(Storage<FluidVariant> storage) {
        this.storage = storage;
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
    public int size() {
        int i = 0;
        for (StorageView<FluidVariant> ignored : this.storage) {
            i++;
        }
        return i;
    }

    @Override
    public boolean isEmpty(int i) {
        StorageView<FluidVariant> view = this.get(i);
        return view == null || view.isResourceBlank();
    }

    @Override
    public boolean canConsume(int i, FluidSpec fluid, long amount) {
        if (!this.storage.supportsExtraction()) {
            return false;
        }
        StorageView<FluidVariant> view = this.get(i);
        return view != null && FabricFluidSpec.matches(view.getResource(), fluid) && view.getAmount() >= amount;
    }

    @Override
    public boolean canConsume(FluidSpec fluid, long amount) {
        if (!this.storage.supportsExtraction()) {
            return false;
        }
        Iterator<StorageView<FluidVariant>> iterator = this.storage.iterator();
        while (iterator.hasNext() && amount > 0L) {
            StorageView<FluidVariant> view = iterator.next();
            if (FabricFluidSpec.matches(view.getResource(), fluid)) {
                amount -= view.getAmount();
            }
        }
        return amount <= 0L;
    }

    @Override
    public boolean canAccept(FluidSpec fluid, long amount) {
        if (!this.storage.supportsInsertion()) {
            return false;
        }
        return this.spaceFor(fluid) >= amount;
    }

    @Override
    public boolean canAccept(int i, FluidSpec fluid, long amount) {
        if (!this.storage.supportsInsertion()) {
            return false;
        }
        return this.spaceFor(i, fluid) >= amount;
    }

    @Override
    public long consume(int i, FluidSpec fluid, long amount) {
        if (!this.storage.supportsExtraction()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            return view.extract(FabricFluidSpec.of(fluid), amount, Transaction.openOuter());
        }
    }

    @Override
    public long consume(FluidSpec fluid, long amount) {
        if (!this.storage.supportsExtraction()) {
            return 0L;
        }
        return this.storage.extract(FabricFluidSpec.of(fluid), amount, Transaction.openOuter());
    }

    @Override
    public long accept(int i, FluidSpec fluid, long amount) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            Storage<FluidVariant> storage = (Storage<FluidVariant>) s;
            return storage.insert(FabricFluidSpec.of(fluid), amount, Transaction.openOuter());
        }
    }

    @Override
    public long accept(FluidSpec fluid, long amount) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        return this.storage.insert(FabricFluidSpec.of(fluid), amount, Transaction.openOuter());
    }

    @Override
    public long spaceFor(int i, FluidSpec fluid) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        StorageView<FluidVariant> view = this.get(i);
        if (view == null || FabricFluidSpec.matches(view.getResource(), fluid)) {
            return 0L;
        } else {
            return view.getCapacity() - view.getAmount();
        }
    }

    @Override
    public long spaceFor(FluidSpec fluid) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<FluidVariant> view : this.storage) {
            if (FabricFluidSpec.matches(view.getResource(), fluid) || view.isResourceBlank()) {
                result += view.getCapacity() - view.getAmount();
            }
        }
        return result;
    }

    @Override
    public long capacityFor(int i, FluidSpec fluid) {
        StorageView<FluidVariant> view = this.get(i);
        if (view == null || !FabricFluidSpec.matches(view.getResource(), fluid)) {
            return 0L;
        } else {
            return view.getCapacity();
        }
    }

    @Override
    public long capacityFor(FluidSpec fluid) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<FluidVariant> view : this.storage) {
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
        for (StorageView<FluidVariant> view : this.storage) {
            if (FabricFluidSpec.matches(view.getResource(), fluid)) {
                result += view.getAmount();
            }
        }
        return result;
    }

    @Nullable
    @Override
    public FluidSpec fluidFor(int i) {
        StorageView<FluidVariant> view = this.get(i);
        if (view == null) {
            return null;
        } else {
            return FabricFluidSpec.from(view.getResource());
        }
    }
}
