package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;

import java.util.Optional;

@SuppressWarnings("unused")
public interface ElenetPeer extends ElenetAccess {
    <T extends Type> Optional<ElenetHub> resonatedHub(TypeToken<T> type);

    /**
     * Resonate this peer with the given hub, and isolate it from its original hub if type collides.
     * */
    <T extends Type> void resonateHub(ElenetHub hub, TypeToken<T> type);

    <T extends Type> void isolateHub(TypeToken<T> token);

    <T extends Type> long consume(T resource, long amount);

    <T extends Type> long accept(T resource, long amount);
}
