package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

@SuppressWarnings("unused")
public class ElenetManager {
    private static final Map<ResourceLocation, Elenet<?>> NETWORKS = new HashMap<>();

    public static boolean register(Elenet<?> elenet) {
        ResourceLocation id = elenet.getEngrave();
        Elenet<?> old = NETWORKS.put(id, elenet);
        if (old != null) {
            return false;
        }
        NETWORKS.put(id, elenet);
        return true;
    }

    @NotNull
    public static ResourceLocation randomId() {
        int i = (int) (Math.random() * NETWORKS.size());
        do {
            ResourceLocation id = new ResourceLocation("croparia", "network_" + i);
            @Nullable Elenet<?> elenet = NETWORKS.get(id);
            if (elenet == null) {
                return id;
            } else if (elenet.shouldRemove()) {
                NETWORKS.remove(id);
            }
            i++;
        } while (true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> Optional<Elenet<T>> getNetwork(TypeToken<T> type, ResourceLocation id) {
        Elenet<?> elenet = NETWORKS.get(id);
        try {
            if (elenet.getType() == type) {
                return Optional.of((Elenet<T>) elenet);
            } else {
                return Optional.empty();
            }
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    public static void resonate(@NotNull ElenetHub a, @NotNull ElenetHub b) {
        if (!a.canResonate(b)) return;
        for (TypeToken<?> type : a.getTypes()) {
            resonate(a, b, type);
        }
    }

    public static <T extends Type> void resonate(@NotNull ElenetHub a, @NotNull ElenetHub b, @NotNull TypeToken<T> type) {
        if (a.isTypeValid(type) && b.isTypeValid(type) && a.canResonate(b)) {
            Optional<Elenet<T>> optionalElenetA = a.getElenet(type);
            Optional<Elenet<T>> optionalElenetB = b.getElenet(type);
            Optional<String> optionalTokenA = optionalElenetA.flatMap(Elenet::getToken);
            Optional<String> optionalTokenB = optionalElenetB.flatMap(Elenet::getToken);
            if (optionalElenetA.isPresent()) {
                Elenet<T> elenetA = optionalElenetA.get();
                if (optionalElenetB.isEmpty()) {
                    a.resonate(b, type);
                    b.resonate(a, type);
                    elenetA.registerHub(b.getAddress());
                    b.setNetwork(optionalElenetA.get());
                } else if (optionalTokenB.isEmpty() || optionalTokenA.isEmpty() || optionalTokenA.get().equals(optionalTokenB.get())) {
                    Elenet<T> elenetB = optionalElenetB.get();
                    a.resonate(b, type);
                    b.resonate(a, type);
                    elenetB.forEachHub(hub -> {
                        elenetA.registerHub(hub.getAddress());
                        elenetB.unregisterHub(hub.getAddress());
                        hub.setNetwork(elenetA);
                        return true;
                    });
                    elenetB.forEachPeer(peer -> {
                        elenetA.registerPeer(peer.getAddress());
                        elenetB.unregisterHub(peer.getAddress());
                        return true;
                    });
                }
            } else if (optionalElenetB.isPresent()) {
                Elenet<T> elenetB = optionalElenetB.get();
                a.resonate(b, type);
                b.resonate(a, type);
                elenetB.registerHub(a.getAddress());
                a.setNetwork(optionalElenetB.get());
            }
        }
    }

    /**
     * If type match, peer is reachable by hub and peer is isolated of type, resonate.
     */
    public static <T extends Type> void resonate(@NotNull ElenetHub hub, @NotNull ElenetPeer peer) {
        if (!hub.canResonate(peer)) return;
        for (TypeToken<?> type : hub.getTypes()) {
            if (peer.isTypeValid(type) && !peer.isResonanceValid(type)) {
                peer.isolateOfType(type);
                peer.resonateWith(hub, type);
                hub.resonate(peer, type);
            }
        }
    }

    public static <T extends Type> void resonate(@NotNull ElenetHub hub, @NotNull ElenetPeer peer, @NotNull TypeToken<T> type) {
        if (hub.isTypeValid(type) && hub.canResonate(peer) && peer.isTypeValid(type) && !peer.isResonanceValid(type)) {
            peer.isolateOfType(type);
            peer.resonateWith(hub, type);
            hub.resonate(peer, type);
        } else if (peer.resonatedHub(type).map(pHub -> hub == pHub).orElse(false)) {
            peer.isolateOfType(type);
            hub.isolate(peer, type);
            hub.getElenet(type).ifPresent(elenet -> elenet.unregisterPeer(peer.getAddress()));
        }
    }

    public static <T extends Type> void isolate(@NotNull ElenetHub hub, @NotNull ElenetPeer peer, @NotNull TypeToken<T> type) {
        if (peer.resonatedHub(type).map(pHub -> pHub == hub).orElse(false)) {
            peer.isolateOfType(type);
            hub.isolate(peer, type);
            hub.getElenet(type).ifPresent(elenet -> elenet.unregisterPeer(peer.getAddress()));
        }
    }

    public static <T extends Type> void updateNetwork(@NotNull ElenetHub from, @NotNull Elenet<T> newElenet) {
        Stack<ElenetHub> hubs = new Stack<>();
        hubs.push(from);
        while (!hubs.isEmpty()) {
            ElenetHub hub = hubs.pop();
            hub.setNetwork(newElenet);
            hub.resonatedHubsOfType(newElenet.getType()).ifPresent(
                addresses -> addresses.forEach(address -> address.getHub().ifPresent(hubs::push))
            );
        }
    }
}
