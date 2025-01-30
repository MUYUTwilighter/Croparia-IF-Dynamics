package cool.muyucloud.croparia.dynamics.api.repo.item.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class FabricItemAgent implements ItemRepo {
    @NotNull
    public static FabricItemAgent of(@NotNull Storage<ItemVariant> storage) {
        return new FabricItemAgent(storage);
    }

    private final Storage<ItemVariant> storage;

    public FabricItemAgent(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Nullable
    public StorageView<ItemVariant> get(int i) {
        int v = 0;
        Iterator<StorageView<ItemVariant>> iterator = this.storage.iterator();
        StorageView<ItemVariant> view = null;
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
        for (StorageView<ItemVariant> ignored : this.storage) {
            i++;
        }
        return i;
    }

    @Override
    public boolean isEmpty(int i) {
        StorageView<ItemVariant> view = this.get(i);
        return view == null || view.isResourceBlank();
    }

    @Override
    public boolean canConsume(int i, ItemSpec item, long amount) {
        if (!this.storage.supportsExtraction()) {
            return false;
        }
        StorageView<ItemVariant> view = this.get(i);
        return view != null && FabricItemSpec.matches(item, view.getResource()) && view.getAmount() >= amount;
    }

    @Override
    public boolean canConsume(ItemSpec item, long amount) {
        if (!this.storage.supportsExtraction()) {
            return false;
        }
        Iterator<StorageView<ItemVariant>> iterator = this.storage.iterator();
        while (iterator.hasNext() && amount > 0L) {
            StorageView<ItemVariant> view = iterator.next();
            if (FabricItemSpec.matches(item, view.getResource())) {
                amount -= view.getAmount();
            }
        }
        return amount <= 0L;
    }

    @Override
    public boolean canAccept(ItemSpec item, long amount) {
        if (!this.storage.supportsInsertion()) {
            return false;
        }
        return this.spaceFor(item) >= amount;
    }

    @Override
    public boolean canAccept(int i, ItemSpec item, long amount) {
        if (!this.storage.supportsInsertion()) {
            return false;
        }
        return this.spaceFor(i, item) >= amount;
    }

    @Override
    public long consume(int i, ItemSpec item, long amount) {
        if (!this.storage.supportsExtraction()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            return view.extract(FabricItemSpec.of(item), amount, Transaction.openOuter());
        }
    }

    @Override
    public long consume(ItemSpec item, long amount) {
        if (!this.storage.supportsExtraction()) {
            return 0L;
        }
        return this.storage.extract(FabricItemSpec.of(item), amount, Transaction.openOuter());
    }

    @Override
    @SuppressWarnings("unchecked")
    public long accept(int i, ItemSpec item, long amount) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            Storage<ItemVariant> storage = (Storage<ItemVariant>) s;
            return storage.insert(FabricItemSpec.of(item), amount, Transaction.openOuter());
        }
    }

    @Override
    public long accept(ItemSpec item, long amount) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        return this.storage.insert(FabricItemSpec.of(item), amount, Transaction.openOuter());
    }

    @Override
    public long spaceFor(int i, ItemSpec item) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (view == null || FabricItemSpec.matches(item, view.getResource())) {
            return 0L;
        } else {
            return view.getCapacity() - view.getAmount();
        }
    }

    @Override
    public long spaceFor(ItemSpec item) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<ItemVariant> view : this.storage) {
            if (FabricItemSpec.matches(item, view.getResource()) || view.isResourceBlank()) {
                result += view.getCapacity() - view.getAmount();
            }
        }
        return result;
    }

    @Override
    public long capacityFor(int i, ItemSpec item) {
        StorageView<ItemVariant> view = this.get(i);
        if (view == null || !FabricItemSpec.matches(item, view.getResource())) {
            return 0L;
        } else {
            return view.getCapacity();
        }
    }

    @Override
    public long capacityFor(ItemSpec item) {
        if (!this.storage.supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<ItemVariant> view : this.storage) {
            if (FabricItemSpec.matches(item, view.getResource()) || view.isResourceBlank()) {
                result += view.getCapacity();
            }
        }
        return result;
    }

    @Override
    public long amountFor(int i, ItemSpec item) {
        StorageView<ItemVariant> view = this.get(i);
        if (view == null || !FabricItemSpec.matches(item, view.getResource())) {
            return 0L;
        } else {
            return view.getAmount();
        }
    }

    @Override
    public long amountFor(ItemSpec item) {
        long result = 0L;
        for (StorageView<ItemVariant> view : this.storage) {
            if (FabricItemSpec.matches(item, view.getResource())) {
                result += view.getAmount();
            }
        }
        return result;
    }

    @Nullable
    @Override
    public ItemSpec itemFor(int i) {
        StorageView<ItemVariant> view = this.get(i);
        if (view == null) {
            return null;
        } else {
            return FabricItemSpec.from(view.getResource());
        }
    }
}
