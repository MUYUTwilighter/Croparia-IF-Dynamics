package cool.muyucloud.croparia.dynamics.api.network;

import java.util.Optional;

@SuppressWarnings("unused")
public interface NetworkNode<T> extends NetworkAccess<T> {
    Optional<Network<T>> getNetwork();

    boolean registerPeer();

    boolean unregisterPeer();

    boolean registerNode();

    boolean unregisterNode();
}
