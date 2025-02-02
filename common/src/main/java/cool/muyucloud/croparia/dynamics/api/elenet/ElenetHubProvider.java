package cool.muyucloud.croparia.dynamics.api.elenet;

import java.util.Optional;

public interface ElenetHubProvider {
    Optional<ElenetHub> getHub(ElenetAddress address);
}
