package cool.muyucloud.croparia.dynamics.api.elenet;

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
    public static <T> Optional<Elenet<T>> getNetwork(TypeToken<T> type, ResourceLocation id) {
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
}
