package cool.muyucloud.croparia.dynamics.api.elenet;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ElenetPeer implements ElenetAccess {
    @NotNull
    private transient final Map<TypeToken<?>, Repo<?>> repos = new HashMap<>();
    @NotNull
    private final Map<TypeToken<?>, ElenetAddress> hubs = new HashMap<>();
    @NotNull
    private ElenetAddress address;

    public ElenetPeer(@NotNull ElenetAddress address) {
        this.address = address;
    }

    public void load(JsonObject json) {
        this.hubs.clear();
        this.hubs.putAll(Codec.unboundedMap(TypeToken.CODEC, ElenetAddress.CODEC.codec()).decode(JsonOps.INSTANCE, json.get("hubs")).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode hub addresses: %s".formatted(msg))
        ).getFirst());
    }

    public void save(JsonObject json) {
        json.add("hubs", Codec.unboundedMap(TypeToken.CODEC, ElenetAddress.CODEC.codec()).encodeStart(JsonOps.INSTANCE, this.hubs).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode hub addresses: %s".formatted(msg))
        ));
    }

    public void setAddress(@NotNull ElenetAddress address) {
        this.address = address;
    }

    public void addRepo(@NotNull Repo<?> repo) {
        this.repos.put(repo.getType(), repo);
    }

    public void removeRepo(@NotNull TypeToken<?> type) {
        this.repos.remove(type);
    }

    /**
     * Get the hub that this peer is resonated with the given type.<br>
     * When implementing, you need to check the availability of the resonance via {@link #isResonanceValid(TypeToken)}.
     * If not valid, return {@link Optional#empty()}.
     */
    public <T extends ResourceType> Optional<ElenetHub<?>> resonatedHub(TypeToken<T> type) {
        if (this.isResonanceValid(type)) {
            return this.hubs.get(type).tryGetHub();
        } else {
            return Optional.empty();
        }
    }

    /**
     * Resonate this peer with the given hub, and isolate it from its original hub if type collides.
     */
    public <T extends ResourceType> void resonateWith(ElenetHub<?> hub, TypeToken<T> type) {
        this.hubs.put(type, hub.getAddress());
    }

    /**
     * Force isolate this peer from its original hub of the given type.
     */
    public <T extends ResourceType> void isolateOfType(TypeToken<T> token) {
        this.hubs.remove(token);
    }

    /**
     * Consume the specified amount of resource from this peer to the elenet.<br>
     * You should check {@link #canBeServed(TypeToken)} before you invoke this method.<br>
     */
    @SuppressWarnings("unchecked")
    protected <T extends ResourceType> long consume(@NotNull T resource, long amount) {
        if (!this.isTypeValid(resource.getType())) return 0;
        Repo<T> repo = (Repo<T>) this.repos.get(resource.getType());
        if (repo == null) {
            return 0;
        } else {
            return repo.consume(resource, amount);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceType> long accept(T resource, long amount) {
        if (!this.isTypeValid(resource.getType())) return 0;
        Repo<T> repo = (Repo<T>) this.repos.get(resource.getType());
        if (repo == null) {
            return 0;
        } else {
            return repo.accept(resource, amount);
        }
    }

    public void onRemove() {
        this.forEachType(type -> {
            this.isolateOfType(type);
            this.resonatedHub(type).ifPresent(hub -> hub.isolate(this, type));
        });
    }

    public <T extends ResourceType> long tryConsume(T resource, long amount) {
        return this.canBeServed(resource.getType()) ? this.consume(resource, amount) : 0;
    }

    public <T extends ResourceType> long tryAccept(T resource, long amount) {
        return this.canBeServed(resource.getType()) ? this.accept(resource, amount) : 0;
    }

    public boolean canBeServed(TypeToken<?> type) {
        return this.isTypeValid(type) && this.resonatedHub(type).map(hub -> hub.canServe(this, type)).orElse(false);
    }

    public boolean isResonanceValid(TypeToken<?> type) {
        return this.resonatedHub(type).map(hub -> hub.canResonate(this)).orElse(false);
    }

    @Override
    public boolean isAccessibleFrom(@NotNull ElenetAddress address) {
        return this.getAddress().equals(address);
    }

    @NotNull
    @Override
    public ElenetAddress getAddress() {
        return address;
    }

    @Override
    public void forEachType(Consumer<TypeToken<?>> consumer) {
        this.hubs.keySet().forEach(consumer);
    }

    @Override
    public boolean isTypeValid(TypeToken<?> type) {
        return this.repos.containsKey(type);
    }
}
