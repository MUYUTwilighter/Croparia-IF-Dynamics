package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeTokenAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Elenet<T extends Type> implements TypeTokenAccess {
    public static final MapCodec<Elenet<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TypeToken.CODEC.fieldOf("type").forGetter(Elenet::getType),
        ResourceLocation.CODEC.fieldOf("engrave").forGetter(Elenet::getEngrave),
        Codec.STRING.optionalFieldOf("token").forGetter(Elenet::getToken)
    ).apply(instance, (type, engrave, token) -> new Elenet<>(type, engrave, token.orElse(null))));

    @NotNull
    private transient final Set<ElenetAddress> peers = new HashSet<>();
    @NotNull
    private transient final Set<ElenetAddress> hubs = new HashSet<>();
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

    protected @NotNull Set<ElenetAddress> getPeers() {
        return peers;
    }

    protected @NotNull Set<ElenetAddress> getHubs() {
        return hubs;
    }

    public boolean canMergeTo(@NotNull Elenet<T> other) {
        return this.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public boolean canMergeFrom(@NotNull Elenet<T> other) {
        return other.getToken().isEmpty() || Objects.equals(this.getToken(), other.getToken());
    }

    public void forEachPeer(@NotNull Function<ElenetPeer, Boolean> processor) {
        for (ElenetAddress address : this.getPeers()) {
            if (!address.tryGetPeer().map(processor).orElse(false)) {
                break;
            }
        }
    }

    public void forEachHub(@NotNull Function<ElenetHub, Boolean> processor) {
        for (ElenetAddress address : this.getHubs()) {
            if (!address.tryGetHub().map(processor).orElse(false)) {
                break;
            }
        }
    }

    public void registerPeer(@NotNull ElenetAddress address) {
        this.getPeers().add(address);
    }

    public void unregisterPeer(@NotNull ElenetAddress address) {
        this.getPeers().remove(address);
    }

    public void registerHub(@NotNull ElenetAddress address) {
        this.getHubs().add(address);
    }

    public void unregisterHub(@NotNull ElenetAddress address) {
        this.getHubs().remove(address);
    }

    public boolean shouldRemove() {
        return this.getToken().isEmpty() && this.getHubs().isEmpty();
    }
}
