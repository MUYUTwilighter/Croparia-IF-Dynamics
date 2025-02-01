package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class Elenet<T> {
    public static final MapCodec<Elenet<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TypeToken.CODEC.fieldOf("type").forGetter(Elenet::getType),
        ResourceLocation.CODEC.fieldOf("engrave").forGetter(Elenet::getEngrave),
        Codec.STRING.optionalFieldOf("token").forGetter(Elenet::getToken)
    ).apply(instance, (type, engrave, token) -> new Elenet<>(type, engrave, token.orElse(null))));

    @NotNull
    private transient final Map<ElenetAddress, ElenetPeer> peers = new HashMap<>();
    @NotNull
    private transient final Map<ElenetAddress, ElenetNode> nodes = new HashMap<>();
    @NotNull
    private final TypeToken<T> type;
    @NotNull
    private final ResourceLocation engrave;
    @Nullable
    private String token;

    public Elenet(@NotNull TypeToken<T> type) {
        this.type = type;
        this.engrave = ElenetManager.randomId();
        this.token = null;
        ElenetManager.register(this);
    }

    public Elenet(@NotNull TypeToken<T> type, @Nullable String token) {
        this.type = type;
        this.engrave = ElenetManager.randomId();
        this.token = token;
        ElenetManager.register(this);
    }

    public Elenet(@NotNull TypeToken<T> type, @NotNull ResourceLocation engrave, @Nullable String token) {
        this.type = type;
        this.engrave = engrave;
        this.token = token;
        if (!ElenetManager.register(this)) {
            throw new IllegalArgumentException("Cannot register network with ID: %s".formatted(engrave));
        }
    }

    public @NotNull TypeToken<T> getType() {
        return type;
    }

    public @NotNull ResourceLocation getEngrave() {
        return engrave;
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }

    protected @NotNull Map<ElenetAddress, ElenetPeer> getPeers() {
        return peers;
    }

    protected @NotNull Map<ElenetAddress, ElenetNode> getNodes() {
        return nodes;
    }

    public boolean canMergeTo(@NotNull Elenet<T> other) {
        return this.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public boolean canMergeFrom(@NotNull Elenet<T> other) {
        return other.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public void forEachPeer(@NotNull BiConsumer<ElenetAddress, ElenetPeer> consumer) {
        this.getPeers().forEach(consumer);
    }

    public void forEachNode(@NotNull BiConsumer<ElenetAddress, ElenetNode> consumer) {
        this.getNodes().forEach(consumer);
    }

    public boolean registerPeer(@NotNull ElenetAddress address, @NotNull ElenetPeer peer) {
        ElenetPeer existing = this.getPeers().get(address);
        if (existing != null && existing.isAccessibleFrom(address)) {
            return false;
        }
        this.getPeers().put(address, peer);
        return true;
    }

    public boolean unregisterPeer(@NotNull ElenetAddress address) {
        return this.getPeers().remove(address) != null;
    }

    public boolean registerNode(@NotNull ElenetAddress address, @NotNull ElenetNode node) {
        ElenetNode existing = this.getNodes().get(address);
        if (existing != null && existing.isAccessibleFrom(address)) {
            return false;
        }
        this.getNodes().put(address, node);
        return true;
    }

    public boolean unregisterNode(@NotNull ElenetAddress address) {
        return this.getNodes().remove(address) != null;
    }

    public boolean shouldRemove() {
        return this.getToken().isEmpty() && this.getNodes().isEmpty();
    }
}
