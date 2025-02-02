package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.TypeRepo;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ElenetAccess extends TypeRepo {
    /**
     * <p>
     * Whether this access device is accessible via the given address.
     * </p>
     * <p>
     * For example, a Elenet Peer device is accessible from a the block pos of the peer itself.
     * </p>
     *
     * @param address the address which is a part of the device
     */
    boolean isAccessibleFrom(@NotNull ElenetAddress address);

    @NotNull
    ElenetAddress getAddress();
}
