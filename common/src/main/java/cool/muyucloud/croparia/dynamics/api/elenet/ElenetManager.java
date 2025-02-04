package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public static <T extends Type> void resonate(@NotNull ElenetHub a, @NotNull ElenetHub b) {
        if (!a.canResonate(b)) return;
        for (TypeToken<?> type : a.getTypes()) {
            if (b.isTypeValid(type)) {
                a.resonate(b, type);
                b.resonate(a, type);
            }
        }
    }

    /**
     * If type matches, force to isolate a peer from its original hub and resonate it with a new hub.<br>
     */
    public static <T extends Type> void resonate(@NotNull ElenetHub hub, @NotNull ElenetPeer peer) {
        if (!hub.canResonate(peer)) return;
        for (TypeToken<?> type : hub.getTypes()) {
            if (peer.isTypeValid(type)) {
                peer.isolateOfType(type);
                peer.resonateWith(hub, type);
                hub.resonate(peer, type);
            }
        }
    }
}
