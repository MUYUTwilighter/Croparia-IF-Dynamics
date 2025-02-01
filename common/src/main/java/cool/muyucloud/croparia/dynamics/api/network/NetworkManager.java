package cool.muyucloud.croparia.dynamics.api.network;

import cool.muyucloud.croparia.dynamics.api.repo.SpecType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class NetworkManager {
    private static final Map<ResourceLocation, Network<?>> NETWORKS = new HashMap<>();

    public static boolean register(Network<?> network) {
        ResourceLocation id = network.getId();
        Network<?> old = NETWORKS.put(id, network);
        if (old != null) {
            return false;
        }
        NETWORKS.put(id, network);
        return true;
    }

    @NotNull
    public static ResourceLocation randomId() {
        int i = (int) (Math.random() * NETWORKS.size());
        do {
            ResourceLocation id = new ResourceLocation("croparia", "network_" + i);
            @Nullable Network<?> network = NETWORKS.get(id);
            if (network == null) {
                return id;
            } else if (network.shouldRemove()) {
                NETWORKS.remove(id);
            }
            i++;
        } while (true);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Network<T>> getNetwork(SpecType<T> type, ResourceLocation id) {
        Network<?> network = NETWORKS.get(id);
        if (network.getType() == type) {
            return Optional.of((Network<T>) network);
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Network<T>> getNetwork(ResourceLocation id) {
        Network<?> network = NETWORKS.get(id);
        try {
            return Optional.of((Network<T>) network);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
