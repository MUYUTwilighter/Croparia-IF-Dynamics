package cool.muyucloud.croparia.dynamics.api.repo.item;

import cool.muyucloud.croparia.dynamics.api.repo.Unreliable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public interface PlatformItemAgent extends ItemRepo {
    @Override
    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long simAccept(int i, ItemSpec item, long amount);

    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "rely on Iterable<StorageView>")
    long accept(int i, ItemSpec item, long amount);

    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    default long capacityFor(ItemSpec item) {
        long capacity = 0;
        for (int i = 0; i < size(); i++) {
            capacity += capacityFor(i, item);
        }
        return capacity;
    }

    @ApiStatus.Experimental
    @Unreliable(value = "FABRIC", reason = "no canAccept check")
    long capacityFor(int i, ItemSpec item);

    /**
     * Get the proxied repo if it implements the {@link ItemRepo},
     * which is probably implemented by Repo API here and have full function.
     *
     * @return The proxied repo
     */
    @Nullable
    Optional<ItemRepo> peel();
}
