package cool.muyucloud.croparia.dynamics.api.elenet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.typetoken.ResourceType;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeTokenAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Elenet<T extends ResourceType> implements TypeTokenAccess {
    public static final MapCodec<Elenet<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TypeToken.CODEC.fieldOf("type").forGetter(Elenet::getType),
        ResourceLocation.CODEC.fieldOf("engrave").forGetter(Elenet::getEngrave),
        Codec.STRING.optionalFieldOf("token").forGetter(Elenet::getToken)
    ).apply(instance, (type, engrave, token) -> new Elenet<>(type, engrave, token.orElse(null))));

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

    protected @NotNull Set<ElenetAddress> getHubs() {
        return hubs;
    }

    public void forEachPeer(@NotNull Function<ElenetPeer, Boolean> processor) {
        for (ElenetAddress address : this.getHubs()) {
            Collection<ElenetAddress> peers = address.tryGetHub().flatMap(hub -> hub.resonatedPeersOfType(this.type)).orElse(List.of());
            for (ElenetAddress peer : peers) {
                Optional<ElenetPeer> peerOptional = peer.tryGetPeer();
                if (peerOptional.isPresent()) {
                    if (!processor.apply(peerOptional.get())) {
                        break;
                    }
                }
            }
        }
    }

    public void forEachHub(@NotNull Function<ElenetHub<?>, Boolean> processor) {
        for (ElenetAddress address : this.getHubs()) {
            Optional<ElenetHub<?>> hubOptional = address.tryGetHub();
            if (hubOptional.isPresent()) {
                if (!processor.apply(hubOptional.get())) {
                    break;
                }
            }
        }
    }

    public void registerHub(@NotNull ElenetAddress address) {
        this.getHubs().add(address);
    }

    public void unregisterHub(@NotNull ElenetAddress address) {
        this.getHubs().remove(address);
        if (this.getHubs().isEmpty()) ElenetManager.remove(this);
    }

    public boolean shouldRemove() {
        return this.getToken().isEmpty() && this.getHubs().isEmpty();
    }
}
