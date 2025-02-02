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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ElenetHub extends ElenetAccess {
    /**
     * The radius that represents an area accessible to the Elenet Peers, usually smaller than {@link #getRange()}
     */
    int getCoverage();

    /**
     * The radius that represents an area accessible to the Elenet Nodes, usually larger than {@link #getCoverage()}
     */
    int getRange();

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
    <T extends Type> Optional<Set<ElenetHub>> resonatedHubsOfType(TypeToken<T> type);

    @SuggestAccess
    <T extends Type> Optional<Set<ElenetPeer>> resonatedPeersOfType(TypeToken<T> type);

    @SuggestAccess
    Map<TypeToken<?>, Elenet<?>> getElenets();

    default <T extends Type> void refreshNetwork(TypeToken<T> type) {
        if (this.isIdle()) {
            return;
        }
        if (this.getElenets().get(type) == null) {
            this.initElenet(type);
            ElenetTask.createAndQueue(() -> {
            }, () -> {
            }, List.of(), List.of());
        }
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
                        BlockEntity be = world.getBlockEntity(pos);
                        if (hubConsumer != null && be instanceof ElenetHubProvider provider) {
                            provider.getHub(target).ifPresent(hub -> {
                                if (!scannedHubs.contains(hub) && this.getAddress().isInRangeWith(hub.getAddress(), this.getRange())) {
                                    hubConsumer.accept(hub);
                                    scannedHubs.add(hub);
                                }
                            });
                        }
                        if (peerConsumer != null && be instanceof ElenetPeerProvider provider) {
                            provider.getPeer(target).ifPresent(peer -> {
                                if (!scannedPeers.contains(peer) && this.getAddress().isInRangeWith(peer.getAddress(), this.getRange())) {
                                    peerConsumer.accept(peer);
                                    scannedPeers.add(peer);
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

    default <T extends Type> long serveRequest(T resource, long amount) {
        if (this.isIdle() || !this.isTypeValid(resource.getType()) || this.isHubSuspended(resource.getType())) {
            return 0;
        }
        Set<ElenetPeer> peers = this.resonatedPeersOfType(resource.getType()).orElse(null);
        if (peers == null) {
            return 0;
        }
        AtomicLong remained = new AtomicLong(amount);
        Iterator<ElenetPeer> peerIterator = peers.iterator();
        while (peerIterator.hasNext() && remained.get() > 0) {
            ElenetPeer peer = peerIterator.next();
            remained.set(remained.get() - peer.tryConsume(resource, remained.get()));
        }
        if (remained.get() <= 0) {
            return amount;
        }
        this.getElenet(resource.getType()).ifPresent(elenet -> elenet.forEachPeer(peer -> {
            remained.set(remained.get() - peer.tryConsume(resource, remained.get()));
            return remained.get() > 0;
        }));
        return amount - Math.max(0, remained.get());
    }

    default <T extends Type> long serveAccept(T resource, long amount) {
        if (this.isIdle() || !this.isTypeValid(resource.getType()) || this.isHubSuspended(resource.getType())) {
            return 0;
        }
        Set<ElenetPeer> peers = this.resonatedPeersOfType(resource.getType()).orElse(null);
        if (peers == null) {
            return 0;
        }
        AtomicLong remained = new AtomicLong(amount);
        Iterator<ElenetPeer> peerIterator = peers.iterator();
        while (peerIterator.hasNext() && remained.get() > 0) {
            ElenetPeer peer = peerIterator.next();
            remained.set(remained.get() - peer.tryAccept(resource, remained.get()));
        }
        if (remained.get() <= 0) {
            return amount;
        }
        this.getElenet(resource.getType()).ifPresent(elenet -> elenet.forEachPeer(peer -> {
            remained.set(remained.get() - peer.tryAccept(resource, remained.get()));
            return remained.get() > 0;
        }));
        return amount - Math.max(0, remained.get());
    }

    /**
     * Force to initiate an elenet of specified type.
     */
    default <T extends Type> void initElenet(TypeToken<T> type) {
        Elenet<T> elenet = new Elenet<>(type);
        this.setNetwork(elenet);
    }

    default <T extends Type> void setNetwork(Elenet<T> elenet) {
        if (!this.isTypeValid(elenet.getType())) {
            return;
        }
        this.getElenets().put(elenet.getType(), elenet);
    }

    /**
     * Whether the current Elenet HUB is suspended.<br>
     * Note that empty elenet is also viewed as suspended.
     */
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

    @SuppressWarnings("unchecked")
    default <T extends Type> Optional<Elenet<T>> getElenet(TypeToken<T> type) {
        Elenet<?> elenet = this.getElenets().get(type);
        if (elenet.getType() == type) {
            return Optional.of((Elenet<T>) elenet);
        } else {
            return Optional.empty();
        }
    }

    default <T extends Type> boolean canServe(TypeToken<T> type) {
        return this.isTypeValid(type) && !this.isIdle() && !this.isHubSuspended(type);
    }

    default <T extends Type> boolean canServe(ElenetPeer peer, TypeToken<T> type) {
        return this.canServe(type) && !this.isHubSuspended(type);
    }

    default boolean canResonate(ElenetPeer peer) {
        return ElenetAddress.chebyshev(this.getAddress(), peer.getAddress()) <= this.getCoverage();
    }

    default boolean canResonate(ElenetHub hub) {
        return ElenetAddress.chebyshev(this.getAddress(), hub.getAddress()) <= this.getRange();
    }

    default <T extends Type> void resonate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.add(peer));
    }

    default <T extends Type> void isolate(ElenetPeer peer, TypeToken<T> type) {
        this.resonatedPeersOfType(type).ifPresent(peers -> peers.remove(peer));
    }

    default <T extends Type> void resonate(ElenetHub hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.add(hub));
    }

    default <T extends Type> void isolate(ElenetHub hub, TypeToken<T> type) {
        this.resonatedHubsOfType(type).ifPresent(hubs -> hubs.remove(hub));
    }
}
