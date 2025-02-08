package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.annotation.SuggestAccess;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ElenetHub extends ElenetAccess {
    /**
     * <p>
     * The radius that represents an area accessible to the <b>Elenet Peers</b>, usually smaller than {@link #getRange()}
     * </p>
     * <p>When the coverage changes, remember to invoke {@link #onCoverageChange(int)}</p>
     */
    short getCoverage();

    /**
     * <p>
     * Whether the current Elenet HUB is not exposed to elenet,
     * considering properties of the device itself, not type or elenet.
     * </p>
     * <p>
     * For example, when the device is running out of power, or the relied BlockEntity was destroyed,
     * this method will return true.
     * </p>
     */
    boolean isIdle();

    @SuggestAccess
    <T extends Type> Optional<Collection<ElenetAddress>> resonatedHubsOfType(TypeToken<T> type);

    @SuggestAccess
    <T extends Type> Optional<Collection<ElenetAddress>> resonatedPeersOfType(TypeToken<T> type);

    <T extends Type> Optional<Elenet<T>> getElenet(@NotNull TypeToken<T> type);

    <T extends Type> void setNetwork(@NotNull Elenet<T> elenet);

    default Collection<Elenet<?>> getElenets() {
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
    default short getRange() {
        return (short) (this.getCoverage() * 2 + 1);
    }

    default void onRangeChange(int from) {
        if (from < this.getRange()) {
            ElenetTask.subscribe(() -> {
                this.onDisable();
                this.onRefresh();
            }, this.getRange(), List.of(), List.of());
        } else if (from > this.getRange()) {
            ElenetTask.subscribe(() -> {
                Map<Elenet<?>, ElenetHub> phMap = new HashMap<>();
                Set<ElenetHub> phSet = new HashSet<>();
                Map<TypeToken<?>, Map<Elenet<?>, ElenetHub>> elenetAccess = new HashMap<>();
                Map<TypeToken<?>, Set<ElenetHub>> emptyHubs = new HashMap<>();
                this.discover(hub -> {
                    if (!this.getAddress().isInRangeWith(hub.getAddress(), from)) {
                        hub.forEachType(type -> hub.getElenet(type).ifPresentOrElse(elenet -> {
                            Map<Elenet<?>, ElenetHub> resonatedHubs = elenetAccess.computeIfAbsent(type, k -> new HashMap<>());
                            resonatedHubs.put(elenet, hub);
                        }, () -> {
                            Set<ElenetHub> hubs = emptyHubs.computeIfAbsent(type, k -> new HashSet<>());
                            hubs.add(hub);
                        }));
                    }
                }, peer -> {
                });
                this.forEachType(type -> {
                    for (ElenetHub hub : elenetAccess.getOrDefault(type, phMap).values()) {
                        ElenetManager.resonate(this, hub, type);
                    }
                    if (this.getElenet(type).isEmpty()) {
                        this.initElenet(type);
                    }
                    for (ElenetHub hub : emptyHubs.getOrDefault(type, phSet)) {
                        ElenetManager.resonate(this, hub);
                    }
                });
            }, this.getRange(), this.getElenets(), List.of(this));
        }
    }

    default void onCoverageChange(int from) {
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

    default void onRefresh() {
        Map<Elenet<?>, ElenetHub> emptyMap = new HashMap<>();
        Set<ElenetHub> emptySet = new HashSet<>();
        Set<ElenetAddress> validHubs = new HashSet<>();
        Map<TypeToken<?>, Map<Elenet<?>, ElenetHub>> elenetAccess = new HashMap<>();
        Map<TypeToken<?>, Set<ElenetHub>> emptyHubs = new HashMap<>();
        discover(hub -> hub.forEachType(type -> hub.getElenet(type).ifPresentOrElse(elenet -> {
            Map<Elenet<?>, ElenetHub> resonatedHubs = elenetAccess.computeIfAbsent(type, k -> new HashMap<>());
            resonatedHubs.put(elenet, hub);
        }, () -> {
            Set<ElenetHub> hubs = emptyHubs.computeIfAbsent(type, k -> new HashSet<>());
            hubs.add(hub);
        })), peer -> ElenetManager.resonate(this, peer));
        this.forEachType(type -> {
            for (ElenetHub hub : elenetAccess.getOrDefault(type, emptyMap).values()) {
                ElenetManager.resonate(this, hub, type);
            }
            if (this.getElenet(type).isEmpty()) {
                this.initElenet(type);
            }
            for (ElenetHub hub : emptyHubs.getOrDefault(type, emptySet)) {
                ElenetManager.resonate(this, hub);
            }
        });
    }

    default <T extends Type> void onRefresh(TypeToken<T> type) {
        Map<Elenet<T>, ElenetHub> elenetAccess = new HashMap<>();
        Set<ElenetHub> emptyHubs = new HashSet<>();
        discover(hub -> hub.getElenet(type).ifPresentOrElse(elenet -> elenetAccess.put(elenet, hub), () -> emptyHubs.add(hub)), peer -> ElenetManager.resonate(this, peer));
        for (ElenetHub hub : elenetAccess.values()) {
            ElenetManager.resonate(this, hub, type);
        }
        if (this.getElenet(type).isEmpty()) {
            this.initElenet(type);
        }
        for (ElenetHub hub : emptyHubs) {
            ElenetManager.resonate(this, hub);
        }
    }

    default void onDisable() {
        this.forEachType(this::onDisable);
    }

    default <T extends Type> void onDisable(TypeToken<T> type) {
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
    default void discover(@Nullable Consumer<ElenetHub> hubConsumer, @Nullable Consumer<ElenetPeer> peerConsumer) {
        if (hubConsumer == null && peerConsumer == null) {
            return;
        }
        @NotNull ElenetAddress address = this.getAddress();
        int range = Math.max(this.getCoverage(), this.getRange());
        BlockPos lower = address.pos().offset(-range, -range, -range);
        BlockPos upper = address.pos().offset(range, range, range);
        Set<ElenetHub> scannedHubs = new HashSet<>();
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

    default <T extends Type> long serveRequest(T resource, long amount, ElenetAddress from) {
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
     * - Other peers are available (exists, accessible)
     */
    default <T extends Type> long serveAccept(T resource, long amount, ElenetAddress from) {
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
    default <T extends Type> Elenet<T> initElenet(TypeToken<T> type) {
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
    default <T extends Type> boolean isHubSuspended(TypeToken<T> type) {
        if (ElenetTask.isSuspended(this)) {
            return true;
        }
        Optional<Elenet<T>> elenet = this.getElenet(type);
        if (elenet.isEmpty()) {
            throw new IllegalArgumentException("Unknown type %s of Elenet HUB %s".formatted(type, this.getAddress()));
        }
        return elenet.filter(ElenetTask::isSuspended).isPresent() && this.getElenet(type).isEmpty();
    }

    default <T extends Type> boolean canServe(TypeToken<T> type) {
        return this.isTypeValid(type) && !this.isIdle() && !this.isHubSuspended(type);
    }

    default <T extends Type> boolean canServe(ElenetPeer peer, TypeToken<T> type) {
        return this.canServe(type) && !this.isHubSuspended(type) && this.canResonate(peer);
    }

    default boolean canResonate(ElenetPeer peer) {
        return ElenetAddress.chebyshev(this.getAddress(), peer.getAddress()) <= this.getCoverage();
    }

    default boolean canResonate(ElenetHub hub) {
        return ElenetAddress.chebyshev(this.getAddress(), hub.getAddress()) <= this.getRange();
    }

    default <T extends Type> void resonate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.add(peer.getAddress()));
    }

    default <T extends Type> void isolate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.remove(peer.getAddress()));
    }

    default <T extends Type> void resonate(ElenetHub hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.add(hub.getAddress()));
    }

    default <T extends Type> void isolate(ElenetHub hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.remove(hub.getAddress()));
    }
}
