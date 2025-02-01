package cool.muyucloud.croparia.dynamics.api.network;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.repo.SpecType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class Network<T> {
    public static final MapCodec<Network<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        SpecType.CODEC.fieldOf("type").forGetter(Network::getType)
    ).apply(instance, Network::new));

    @NotNull
    private transient final Map<NetworkAddress, NetworkPeer<T>> peers = new HashMap<>();
    @NotNull
    private transient final Map<NetworkAddress, NetworkNode<T>> nodes = new HashMap<>();
    @NotNull
    private final SpecType<T> type;
    @NotNull
    private final ResourceLocation id;
    @Nullable
    private final String engrave;

    public Network(@NotNull SpecType<T> type) {
        this.type = type;
        this.id = NetworkManager.randomId();
        this.engrave = null;
        NetworkManager.register(this);
    }

    public Network(@NotNull SpecType<T> type, @Nullable String engrave) {
        this.type = type;
        this.id = NetworkManager.randomId();
        this.engrave = engrave;
        NetworkManager.register(this);
    }

    public Network(@NotNull SpecType<T> type, @NotNull ResourceLocation id, @Nullable String engrave) {
        this.type = type;
        this.id = id;
        this.engrave = engrave;
        if (!NetworkManager.register(this)) {
            throw new IllegalArgumentException("Cannot register network with ID: %s".formatted(id));
        }
    }

    public @NotNull ResourceLocation getId() {
        return id;
    }

    public Optional<String> getEngrave() {
        return Optional.ofNullable(engrave);
    }

    protected @NotNull Map<NetworkAddress, NetworkPeer<T>> getPeers() {
        return peers;
    }

    protected @NotNull Map<NetworkAddress, NetworkNode<T>> getNodes() {
        return nodes;
    }

    public @NotNull SpecType<T> getType() {
        return this.type;
    }

    public boolean canMergeTo(@NotNull Network<T> other) {
        return this.getEngrave().isEmpty() || Objects.equals(this.getEngrave(), other.getEngrave());
    }

    public boolean canMergeFrom(@NotNull Network<T> other) {
        return other.getEngrave().isEmpty() || Objects.equals(this.getEngrave(), other.getEngrave());
    }

    public void forEachPeer(@NotNull BiConsumer<NetworkAddress, NetworkPeer<T>> consumer) {
        this.getPeers().forEach(consumer);
    }

    public void forEachNode(@NotNull BiConsumer<NetworkAddress, NetworkNode<T>> consumer) {
        this.getNodes().forEach(consumer);
    }

    public boolean registerPeer(@NotNull NetworkAddress address, @NotNull NetworkPeer<T> peer) {
        NetworkPeer<T> existing = this.getPeers().get(address);
        if (existing != null && existing.isAccessibleFrom(address)) {
            return false;
        }
        this.getPeers().put(address, peer);
        return true;
    }

    public boolean unregisterPeer(@NotNull NetworkAddress address) {
        return this.getPeers().remove(address) != null;
    }

    public boolean registerNode(@NotNull NetworkAddress address, @NotNull NetworkNode<T> node) {
        NetworkNode<T> existing = this.getNodes().get(address);
        if (existing != null && existing.isAccessibleFrom(address)) {
            return false;
        }
        this.getNodes().put(address, node);
        return true;
    }

    public boolean unregisterNode(@NotNull NetworkAddress address) {
        return this.getNodes().remove(address) != null;
    }

    public boolean shouldRemove() {
        return this.getEngrave().isEmpty() && this.getNodes().isEmpty();
    }
}
