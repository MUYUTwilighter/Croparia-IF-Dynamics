package cool.muyucloud.croparia.dynamics.api.repo.item.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.PlatformItemAgent;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FabricItemAgent implements PlatformItemAgent {
    @NotNull
    public static FabricItemAgent of(@NotNull Storage<ItemVariant> storage) {
        return new FabricItemAgent(storage);
    }

    private final Storage<ItemVariant> storage;

    public FabricItemAgent(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    public Storage<ItemVariant> get() {
        return this.storage;
    }

    @Nullable
    public StorageView<ItemVariant> get(int i) {
        int v = 0;
        Iterator<StorageView<ItemVariant>> iterator = this.get().iterator();
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
    public Optional<ItemRepo> peel() {
        return this.get() instanceof ItemRepo repo ? Optional.of(repo) : Optional.empty();
    }

    @Override
    public int size() {
        int i = 0;
        for (StorageView<ItemVariant> ignored : this.get()) {
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
    public long simConsume(ItemSpec item, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        return StorageUtil.simulateExtract(this.get(), FabricItemSpec.of(item), amount, null);
    }

    @Override
    public long simConsume(int i, ItemSpec item, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            return StorageUtil.simulateExtract(view, FabricItemSpec.of(item), amount, null);
        }
    }

    @Override
    public long consume(int i, ItemSpec item, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (view == null) {
            return 0L;
        } else {
            try (Transaction transaction = Transaction.openOuter()) {
                long result = view.extract(FabricItemSpec.of(item), amount, transaction);
                transaction.commit();
                return result;
            }
        }
    }

    @Override
    public long consume(ItemSpec item, long amount) {
        if (!this.get().supportsExtraction()) {
            return 0L;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long result = this.get().extract(FabricItemSpec.of(item), amount, transaction);
            transaction.commit();
            return result;
        }
    }

    @Override
    public long simAccept(ItemSpec item, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        return StorageUtil.simulateInsert(this.get(), FabricItemSpec.of(item), amount, null);
    }

    @Override
    public long simAccept(int i, ItemSpec item, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            try {
                @SuppressWarnings("unchecked")
                Storage<ItemVariant> storage = (Storage<ItemVariant>) s;
                return StorageUtil.simulateInsert(storage, FabricItemSpec.of(item), amount, null);
            } catch (ClassCastException e) {
                return 0L;
            }
        }
    }

    @Override
    public long accept(int i, ItemSpec item, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        StorageView<ItemVariant> view = this.get(i);
        if (!(view instanceof Storage<?> s)) {
            return 0L;
        } else {
            try {
                @SuppressWarnings("unchecked")
                Storage<ItemVariant> storage = (Storage<ItemVariant>) s;
                try (Transaction transaction = Transaction.openOuter()) {
                    long result = storage.insert(FabricItemSpec.of(item), amount, transaction);
                    transaction.commit();
                    return result;
                }
            } catch (ClassCastException e) {
                return 0L;
            }
        }
    }

    @Override
    public long accept(ItemSpec item, long amount) {
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long result = this.get().insert(FabricItemSpec.of(item), amount, transaction);
            transaction.commit();
            return result;
        }
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
        if (!this.get().supportsInsertion()) {
            return 0L;
        }
        long result = 0L;
        for (StorageView<ItemVariant> view : this.get()) {
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
        for (StorageView<ItemVariant> view : this.get()) {
            if (FabricItemSpec.matches(item, view.getResource())) {
                result += view.getAmount();
            }
        }
        return result;
    }

    @Nullable
    @Override
    public ItemSpec resourceFor(int i) {
        StorageView<ItemVariant> view = this.get(i);
        if (view == null) {
            return null;
        } else {
            return FabricItemSpec.from(view.getResource());
        }
    }
}
