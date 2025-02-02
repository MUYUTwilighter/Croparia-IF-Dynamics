package cool.muyucloud.croparia.dynamics.api.elenet;

import java.util.Optional;

public interface ElenetPeerProvider {
    Optional<ElenetPeer> getPeer(ElenetAddress address);
}
