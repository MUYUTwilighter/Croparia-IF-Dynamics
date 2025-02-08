package cool.muyucloud.croparia.dynamics.api.elenet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.dynamics.util.Provider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unused"})
public class ElenetHubImpl<F> implements ElenetHub {
    private static final int FUEL = 4;
    private static final float REFRESH_RATE = 0.05F;

    @NotNull
    private transient final FuelUnit<F> fuelUnit;
    @NotNull
    private transient final Provider<Float> fuelEffect;
    @NotNull
    private transient ElenetAddress address;
    private final Map<TypeToken<?>, ResourceLocation> elenets = new HashMap<>();
    @NotNull
    private final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedHubs = new HashMap<>();
    @NotNull
    private final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedPeers = new HashMap<>();
    private final Collection<TypeToken<?>> validTypes = new HashSet<>();
    private short coverage;
    private boolean autoRefresh = true;
    private transient boolean removed = false;

    public ElenetHubImpl(@NotNull FuelUnit<F> fuelUnit, @NotNull Provider<Float> fuelEffect, @NotNull ElenetAddress address) {
        this.fuelUnit = fuelUnit;
        this.fuelEffect = fuelEffect;
        this.address = address;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public void addType(TypeToken<?> type) {
        if (this.validTypes.contains(type)) return;
        this.validTypes.add(type);
        ElenetTask.subscribe(() -> this.onRefresh(type), this.getRange(), List.of(), List.of());
    }

    public void removeType(TypeToken<?> type) {
        if (!this.validTypes.contains(type)) return;
        this.validTypes.remove(type);
        // onDisable will handle elenet topologies.
        ElenetTask.subscribe(() -> this.onDisable(type), this.getRange(), this.getElenets(), List.of(this));
    }

    public void tick() {
        if (this.isIdle()) return;
        this.fuelUnit.burn(this.calcFuel());
        if (this.autoRefresh && Math.random() < REFRESH_RATE) {
            ElenetTask.subscribeIfAvailable(this::onRefresh, this.getRange(), List.of(), List.of());
        }
    }

    public void setCoverage(short coverage) {
        short oldCov = this.coverage;
        short oldRange = this.getRange();
        this.coverage = coverage;
        this.onCoverageChange(oldCov);
        this.onRangeChange(oldRange);
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public long calcFuel() {
        return (long) Math.max(FUEL * fuelEffect.get(), 1);
    }

    @Override
    public short getCoverage() {
        return this.coverage;
    }

    @Override
    public boolean isIdle() {
        return !removed && fuelUnit.isEnoughFor(this.calcFuel());
    }

    @Override
    public <T extends Type> Optional<Collection<ElenetAddress>> resonatedHubsOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedHubs.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    @Override
    public <T extends Type> Optional<Collection<ElenetAddress>> resonatedPeersOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedPeers.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Type> Optional<Elenet<T>> getElenet(@NotNull TypeToken<T> type) {
        if (!this.isTypeValid(type)) {
            this.elenets.remove(type);
            return Optional.empty();
        }
        Elenet<?> elenet = ElenetManager.getNetwork(this.elenets.get(type)).orElse(null);
        if (elenet == null) {
            return Optional.of(this.initElenet(type));
        } else {
            return Optional.of((Elenet<T>) elenet);
        }
    }

    @Override
    public <T extends Type> void setNetwork(@NotNull Elenet<T> elenet) {
        if (this.isTypeValid(elenet.getType())) {
            this.elenets.put(elenet.getType(), elenet.getEngrave());
        } else {
            throw new IllegalArgumentException("Invalid type %s of Elenet for Elenet HUB %s".formatted(elenet.getType(), this.getAddress()));
        }
    }

    @Override
    public boolean isAccessibleFrom(@NotNull ElenetAddress address) {
        return !removed && this.address.equals(address);
    }

    @Override
    public @NotNull ElenetAddress getAddress() {
        return address;
    }

    public void setAddress(@NotNull ElenetAddress address) {
        this.address = address;
    }

    @Override
    public void forEachType(Consumer<TypeToken<?>> consumer) {
        this.validTypes.forEach(consumer);
    }

    @Override
    public boolean isTypeValid(TypeToken<?> type) {
        return this.validTypes.contains(type);
    }

    public void load(JsonObject json) {
        if (this.removed) throw new IllegalStateException("Cannot load removed Elenet HUB %s".formatted(this.address));
        this.setCoverage(json.get("coverage").getAsShort());
        this.setAutoRefresh(json.get("autoRefresh").getAsBoolean());
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            TypeToken<?> type = TypeToken.get(ResourceLocation.tryParse(entry.getKey())).orElse(null);
            if (type == null)
                throw new IllegalArgumentException("Unknown type %s of Elenet HUB %s".formatted(entry.getKey(), this.getAddress()));
            this.validTypes.add(type);
            JsonObject subRoot = entry.getValue().getAsJsonObject();
            this.elenets.put(type, ResourceLocation.CODEC.decode(JsonOps.INSTANCE, subRoot.get("elenet")).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode elenet: %s".formatted(msg))
            ).getFirst());
            this.resonatedHubs.put(type, new HashSet<>(ElenetAddress.CODEC.codec().listOf().decode(
                JsonOps.INSTANCE, entry.getValue().getAsJsonObject().get("hubs")
            ).getOrThrow(false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode elenet hub addresses: %s".formatted(msg))).getFirst()));
            this.resonatedPeers.put(type, new HashSet<>(ElenetAddress.CODEC.codec().listOf().decode(
                JsonOps.INSTANCE, entry.getValue().getAsJsonObject().get("peers")
            ).getOrThrow(false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode elenet peer addresses: %s".formatted(msg))).getFirst()));
        }
    }

    public void save(JsonObject json) {
        if (this.removed) throw new IllegalStateException("Cannot save removed Elenet HUB %s".formatted(this.address));
        json.addProperty("coverage", this.getCoverage());
        json.addProperty("autoRefresh", this.autoRefresh);
        this.forEachType(type -> {
            JsonObject subRoot = new JsonObject();
            json.add(type.id().toString(), subRoot);
            subRoot.add("elenet", ResourceLocation.CODEC.encodeStart(JsonOps.INSTANCE, this.elenets.get(type)).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode elenet: %s".formatted(msg))
            ));
            subRoot.add("peers", ElenetAddress.CODEC.codec().listOf().encodeStart(
                JsonOps.INSTANCE, List.copyOf(this.resonatedPeersOfType(type).orElse(List.of()))
            ).getOrThrow(false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode elenet peer addresses: %s".formatted(msg))));
            subRoot.add("hubs", ElenetAddress.CODEC.codec().listOf().encodeStart(
                JsonOps.INSTANCE, List.copyOf(this.resonatedHubsOfType(type).orElse(List.of()))
            ).getOrThrow(false, msg -> CropariaIfDynamics.LOGGER.error("Failed to encode elenet hub addresses: %s".formatted(msg))));
        });
    }
}
