package cool.muyucloud.croparia.dynamics.api.elenet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.annotation.SuggestAccess;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.typetoken.ResourceType;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.dynamics.util.Provider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ElenetHub<F extends ResourceType> implements ElenetAccess {
    private static final int FUEL = 4;
    private static final float REFRESH_RATE = 0.05F;

    @NotNull
    protected transient final FuelUnit<F> fuelUnit;
    @NotNull
    protected transient final Provider<Float> fuelEffect;
    @NotNull
    protected transient ElenetAddress address;
    protected final Map<TypeToken<?>, ResourceLocation> elenets = new HashMap<>();
    @NotNull
    protected final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedHubs = new HashMap<>();
    @NotNull
    protected final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedPeers = new HashMap<>();
    protected final Collection<TypeToken<?>> validTypes = new HashSet<>();
    protected short coverage;
    protected boolean autoRefresh = true;
    private transient boolean removed = false;

    public ElenetHub(@NotNull FuelUnit<F> fuelUnit, @NotNull Provider<Float> fuelEffect, @NotNull ElenetAddress address) {
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

    public short getCoverage() {
        return this.coverage;
    }

    public boolean isIdle() {
        return !removed && fuelUnit.isEnoughFor(this.calcFuel());
    }

    public <T extends ResourceType> Optional<Collection<ElenetAddress>> resonatedHubsOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedHubs.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    public <T extends ResourceType> Optional<Collection<ElenetAddress>> resonatedPeersOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedPeers.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceType> Optional<Elenet<T>> getElenet(@NotNull TypeToken<T> type) {
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

    public <T extends ResourceType> void setNetwork(@NotNull Elenet<T> elenet) {
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

    protected Collection<Elenet<?>> getElenets() {
        List<Elenet<?>> elenets = new ArrayList<>();
        this.forEachType(type -> this.getElenet(type).ifPresent(elenets::add));
        return elenets;
    }

    /**
     * <p>
     * The radius that represents an area accessible to the <b>Elenet Nodes</b>, usually larger than {@link #getCoverage()}
     * </p>
     * <p>
     * When the range changes, remember to invoke {@link #onRangeChange(int)}.
     * </p>
     */
    public short getRange() {
        return (short) (this.getCoverage() * 2 + 1);
    }

    public void onRangeChange(int from) {
        if (from < this.getRange()) {
            ElenetTask.subscribe(() -> {
                this.onDisable();
                this.onRefresh();
            }, this.getRange(), List.of(), List.of());
        } else if (from > this.getRange()) {
            ElenetTask.subscribe(() -> {
                Map<Elenet<?>, ElenetHub<?>> phMap = new HashMap<>();
                Set<ElenetHub<?>> phSet = new HashSet<>();
                Map<TypeToken<?>, Map<Elenet<?>, ElenetHub<?>>> elenetAccess = new HashMap<>();
                Map<TypeToken<?>, Set<ElenetHub<?>>> emptyHubs = new HashMap<>();
                this.discover(hub -> {
                    if (!this.getAddress().isInRangeWith(hub.getAddress(), from)) {
                        hub.forEachType(type -> hub.getElenet(type).ifPresentOrElse(elenet -> {
                            Map<Elenet<?>, ElenetHub<?>> resonatedHubs = elenetAccess.computeIfAbsent(type, k -> new HashMap<>());
                            resonatedHubs.put(elenet, hub);
                        }, () -> {
                            Set<ElenetHub<?>> hubs = emptyHubs.computeIfAbsent(type, k -> new HashSet<>());
                            hubs.add(hub);
                        }));
                    }
                }, peer -> {
                });
                this.forEachType(type -> {
                    for (ElenetHub<?> hub : elenetAccess.getOrDefault(type, phMap).values()) {
                        ElenetManager.resonate(this, hub, type);
                    }
                    if (this.getElenet(type).isEmpty()) {
                        this.initElenet(type);
                    }
                    for (ElenetHub<?> hub : emptyHubs.getOrDefault(type, phSet)) {
                        ElenetManager.resonate(this, hub);
                    }
                });
            }, this.getRange(), this.getElenets(), List.of(this));
        }
    }

    public void onCoverageChange(int from) {
        if (from < this.getCoverage()) {
            this.discover(hub -> {
            }, peer -> ElenetManager.resonate(this, peer));
        } else if (from > this.getCoverage()) {
            Map<TypeToken<?>, Set<ElenetAddress>> toRemove = new HashMap<>();
            this.forEachType(type -> this.resonatedPeersOfType(type).ifPresent(addresses -> addresses.forEach(address -> {
                if (this.getAddress().isInRangeWith(address, this.getCoverage())) {
                    toRemove.computeIfAbsent(type, k -> new HashSet<>()).add(address);
                }
            })));
            toRemove.forEach((type, address) -> this.resonatedHubsOfType(type).ifPresent(addresses -> addresses.removeAll(toRemove.get(type))));
        }
    }

    public void onRefresh() {
        Map<Elenet<?>, ElenetHub<?>> emptyMap = new HashMap<>();
        Set<ElenetHub<?>> emptySet = new HashSet<>();
        Set<ElenetAddress> validHubs = new HashSet<>();
        Map<TypeToken<?>, Map<Elenet<?>, ElenetHub<?>>> elenetAccess = new HashMap<>();
        Map<TypeToken<?>, Set<ElenetHub<?>>> emptyHubs = new HashMap<>();
        discover(hub -> hub.forEachType(type -> hub.getElenet(type).ifPresentOrElse(elenet -> {
            Map<Elenet<?>, ElenetHub<?>> resonatedHubs = elenetAccess.computeIfAbsent(type, k -> new HashMap<>());
            resonatedHubs.put(elenet, hub);
        }, () -> {
            Set<ElenetHub<?>> hubs = emptyHubs.computeIfAbsent(type, k -> new HashSet<>());
            hubs.add(hub);
        })), peer -> ElenetManager.resonate(this, peer));
        this.forEachType(type -> {
            for (ElenetHub<?> hub : elenetAccess.getOrDefault(type, emptyMap).values()) {
                ElenetManager.resonate(this, hub, type);
            }
            if (this.getElenet(type).isEmpty()) {
                this.initElenet(type);
            }
            for (ElenetHub<?> hub : emptyHubs.getOrDefault(type, emptySet)) {
                ElenetManager.resonate(this, hub);
            }
        });
    }

    public <T extends ResourceType> void onRefresh(TypeToken<T> type) {
        Map<Elenet<T>, ElenetHub<?>> elenetAccess = new HashMap<>();
        Set<ElenetHub<?>> emptyHubs = new HashSet<>();
        discover(hub -> hub.getElenet(type).ifPresentOrElse(elenet -> elenetAccess.put(elenet, hub), () -> emptyHubs.add(hub)), peer -> ElenetManager.resonate(this, peer));
        for (ElenetHub<?> hub : elenetAccess.values()) {
            ElenetManager.resonate(this, hub, type);
        }
        if (this.getElenet(type).isEmpty()) {
            this.initElenet(type);
        }
        for (ElenetHub<?> hub : emptyHubs) {
            ElenetManager.resonate(this, hub);
        }
    }

    public void onDisable() {
        this.forEachType(this::onDisable);
    }

    public <T extends ResourceType> void onDisable(TypeToken<T> type) {
        AtomicBoolean flag = new AtomicBoolean(false);
        this.resonatedHubsOfType(type).ifPresent(addresses -> {
            addresses.forEach(address -> address.getHub().ifPresent(hub -> {
                hub.isolate(this, type);
                if (flag.get()) {
                    Elenet<T> elenet = new Elenet<>(type);
                    ElenetManager.updateNetwork(hub, elenet);
                } else {
                    flag.set(true);
                }
            }));
            addresses.clear();
        });
        this.getElenet(type).ifPresent(elenet -> elenet.unregisterHub(this.getAddress()));
        this.resonatedPeersOfType(type).ifPresent(addresses -> {
            addresses.forEach(address -> address.getPeer().ifPresent(peer -> ElenetManager.isolate(this, peer, type)));
            addresses.clear();
        });
    }

    @SuggestAccess
    public void discover(@Nullable Consumer<ElenetHub<?>> hubConsumer, @Nullable Consumer<ElenetPeer> peerConsumer) {
        if (hubConsumer == null && peerConsumer == null) {
            return;
        }
        @NotNull ElenetAddress address = this.getAddress();
        int range = Math.max(this.getCoverage(), this.getRange());
        BlockPos lower = address.pos().offset(-range, -range, -range);
        BlockPos upper = address.pos().offset(range, range, range);
        Set<ElenetHub<?>> scannedHubs = new HashSet<>();
        Set<ElenetPeer> scannedPeers = new HashSet<>();
        for (int x = lower.getX(); x < upper.getX(); ++x) {
            for (int y = lower.getY(); y < upper.getY(); ++y) {
                for (int z = lower.getZ(); z < upper.getZ(); ++z) {
                    try (Level world = address.world()) {
                        BlockPos pos = new BlockPos(x, y, z);
                        ElenetAddress target = ElenetAddress.of(world, pos);
                        if (target.equals(address)) {
                            continue;
                        }
                        @Nullable BlockEntity be = world.getBlockEntity(pos);
                        if (hubConsumer != null && be instanceof @NotNull ElenetHubProvider provider) {
                            provider.getHub(target).ifPresent(hub -> {
                                if (!scannedHubs.contains(hub) && this.getAddress().isInRangeWith(hub.getAddress(), this.getRange())) {
                                    hubConsumer.accept(hub);
                                    scannedHubs.add(hub);
                                }
                            });
                        }
                        if (peerConsumer != null && be instanceof @NotNull ElenetPeerProvider provider) {
                            provider.getPeer(target).ifPresent(peer -> {
                                if (!scannedPeers.contains(peer)) {
                                    if (this.getAddress().isInRangeWith(peer.getAddress(), this.getCoverage())) {
                                        peerConsumer.accept(peer);
                                        scannedPeers.add(peer);
                                    } else {
                                        this.forEachType(type -> ElenetManager.isolate(this, peer, type));
                                    }
                                }
                            });
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }

    public <T extends ResourceType> long serveRequest(T resource, long amount, ElenetAddress from) {
        if (!this.canServe(resource.getType())) {
            return 0;
        }
        Collection<ElenetAddress> addresses = this.resonatedPeersOfType(resource.getType()).orElse(null);
        if (addresses == null) {
            return 0;
        }
        AtomicLong remained = new AtomicLong(amount);
        Iterator<ElenetAddress> addressIterator = addresses.iterator();
        while (addressIterator.hasNext() && remained.get() > 0) {
            ElenetAddress address = addressIterator.next();
            if (address.equals(from)) {
                continue;
            }
            address.tryGetPeer().ifPresent(peer -> remained.set(remained.get() - peer.tryConsume(resource, remained.get())));
        }
        if (remained.get() <= 0) {
            return amount;
        }
        this.getElenet(resource.getType()).ifPresent(elenet -> elenet.forEachPeer(peer -> {
            if (peer.getAddress().equals(from)) {
                return true;
            }
            remained.set(remained.get() - peer.tryConsume(resource, remained.get()));
            return remained.get() > 0;
        }));
        return amount - Math.max(0, remained.get());
    }

    /**
     * This ensures:
     * - the hub is not idle
     * - the type is valid
     * - the hub is not suspended
     * - Other peers are available (exists & accessible)
     */
    public <T extends ResourceType> long serveAccept(T resource, long amount, ElenetAddress from) {
        if (!this.canServe(resource.getType())) {
            return 0;
        }
        Collection<ElenetAddress> addresses = this.resonatedPeersOfType(resource.getType()).orElse(null);
        if (addresses == null) {
            return 0;
        }
        AtomicLong remained = new AtomicLong(amount);
        Iterator<ElenetAddress> addressIterator = addresses.iterator();
        while (addressIterator.hasNext() && remained.get() > 0) {
            ElenetAddress address = addressIterator.next();
            if (address.equals(from)) {
                continue;
            }
            Optional<ElenetPeer> optional = address.tryGetPeer();
            address.tryGetPeer().ifPresent(peer -> remained.set(remained.get() - peer.tryAccept(resource, remained.get())));
        }
        if (remained.get() <= 0) {
            return amount;
        }
        this.getElenet(resource.getType()).ifPresent(elenet -> elenet.forEachPeer(peer -> {
            if (peer.getAddress().equals(from)) {
                return true;
            }
            remained.set(remained.get() - peer.tryConsume(resource, remained.get()));
            return remained.get() > 0;
        }));
        return amount - Math.max(0, remained.get());
    }

    /**
     * Force to initiate an elenet of specified type.
     */
    @NotNull
    public <T extends ResourceType> Elenet<T> initElenet(TypeToken<T> type) {
        Elenet<T> elenet = new Elenet<>(type);
        this.setNetwork(elenet);
        ElenetManager.register(elenet);
        return elenet;
    }

    /**
     * Whether the current Elenet HUB is suspended.<br>
     * Note that empty elenet is also viewed as suspended.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public <T extends ResourceType> boolean isHubSuspended(TypeToken<T> type) {
        if (ElenetTask.isSuspended(this)) {
            return true;
        }
        Optional<Elenet<T>> elenet = this.getElenet(type);
        if (elenet.isEmpty()) {
            throw new IllegalArgumentException("Unknown type %s of Elenet HUB %s".formatted(type, this.getAddress()));
        }
        return elenet.filter(ElenetTask::isSuspended).isPresent() && this.getElenet(type).isEmpty();
    }

    public <T extends ResourceType> boolean canServe(TypeToken<T> type) {
        return this.isTypeValid(type) && !this.isIdle() && !this.isHubSuspended(type);
    }

    public <T extends ResourceType> boolean canServe(ElenetPeer peer, TypeToken<T> type) {
        return this.canServe(type) && !this.isHubSuspended(type) && this.canResonate(peer);
    }

    public boolean canResonate(ElenetPeer peer) {
        return ElenetAddress.chebyshev(this.getAddress(), peer.getAddress()) <= this.getCoverage();
    }

    public boolean canResonate(ElenetHub<?> hub) {
        return ElenetAddress.chebyshev(this.getAddress(), hub.getAddress()) <= this.getRange();
    }

    public <T extends ResourceType> void resonate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.add(peer.getAddress()));
    }

    public <T extends ResourceType> void isolate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.remove(peer.getAddress()));
    }

    public <T extends ResourceType> void resonate(ElenetHub<?> hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.add(hub.getAddress()));
    }

    public <T extends ResourceType> void isolate(ElenetHub<?> hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.remove(hub.getAddress()));
    }
}
