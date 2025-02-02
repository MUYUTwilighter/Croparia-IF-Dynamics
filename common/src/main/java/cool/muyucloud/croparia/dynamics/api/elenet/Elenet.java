package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeTokenAccess;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Elenet<T extends Type> implements TypeTokenAccess {
    public static final MapCodec<Elenet<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TypeToken.CODEC.fieldOf("type").forGetter(Elenet::getType),
        ResourceLocation.CODEC.fieldOf("engrave").forGetter(Elenet::getEngrave),
        Codec.STRING.optionalFieldOf("token").forGetter(Elenet::getToken)
    ).apply(instance, (type, engrave, token) -> new Elenet<>(type, engrave, token.orElse(null))));

    @NotNull
    private transient final Set<ElenetPeer> peers = new HashSet<>();
    @NotNull
    private transient final Set<ElenetHub> hubs = new HashSet<>();
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

    protected @NotNull Set<ElenetPeer> getPeers() {
        return peers;
    }

    protected @NotNull Set<ElenetHub> getHubs() {
        return hubs;
    }

    public boolean canMergeTo(@NotNull Elenet<T> other) {
        return this.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public boolean canMergeFrom(@NotNull Elenet<T> other) {
        return other.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public void forEachPeer(@NotNull Function<ElenetPeer, Boolean> processor) {
        for (ElenetPeer peer : this.getPeers()) {
            if (!processor.apply(peer)) {
                break;
            }
        }
    }

    public void forEachHub(@NotNull Function<ElenetHub, Boolean> consumer) {
        for (ElenetHub hub : this.getHubs()) {
            if (!consumer.apply(hub)) {
                break;
            }
        }
    }

    public void registerPeer(@NotNull ElenetPeer peer) {
        this.getPeers().add(peer);
    }

    public void unregisterPeer(@NotNull ElenetPeer peer) {
        this.getPeers().remove(peer);
    }

    public void registerHub(@NotNull ElenetHub node) {
        this.getHubs().add(node);
    }

    public void unregisterHub(@NotNull ElenetHub hub) {
        this.getHubs().remove(hub);
    }

    public boolean shouldRemove() {
        return this.getToken().isEmpty() && this.getHubs().isEmpty();
    }
}
