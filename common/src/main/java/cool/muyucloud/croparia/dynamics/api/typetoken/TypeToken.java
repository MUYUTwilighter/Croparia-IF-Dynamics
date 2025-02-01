package cool.muyucloud.croparia.dynamics.api.typetoken;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public record TypeToken<T>(@NotNull ResourceLocation id, @NotNull T empty) {
    public static final Codec<TypeToken<?>> CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
        Optional<TypeToken<Object>> type = get(id);
        if (type.isEmpty()) {
            return DataResult.error(() -> "Undefined SpecType: %s".formatted(id));
        } else {
            return DataResult.success(type.get());
        }
    }, TypeToken::id);

    private static final Map<ResourceLocation, TypeToken<?>> REGISTRY_BY_ID = new HashMap<>();
    private static final Map<Object, TypeToken<?>> REGISTRY_BY_TOKEN = new HashMap<>();

    public static <T> Optional<TypeToken<T>> register(ResourceLocation id, T empty) {
        if (REGISTRY_BY_ID.containsKey(id) || REGISTRY_BY_TOKEN.containsKey(empty)) {
            return Optional.empty();
        }
        TypeToken<T> type = new TypeToken<>(id, empty);
        REGISTRY_BY_ID.put(id, type);
        REGISTRY_BY_TOKEN.put(empty, type);
        return Optional.of(type);
    }

    public static <T> TypeToken<T> registerOrThrow(ResourceLocation id, T empty) {
        return register(id, empty).orElseThrow(() -> new IllegalArgumentException("Duplicate TypeToken: %s".formatted(id)));
    }


    @SuppressWarnings("unchecked")
    public static <T> Optional<TypeToken<T>> get(ResourceLocation id) {
        TypeToken<?> type = REGISTRY_BY_ID.get(id);
        try {
            return Optional.of((TypeToken<T>) type);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<TypeToken<T>> get(T token) {
        TypeToken<?> type = REGISTRY_BY_TOKEN.get(token);
        try {
            return Optional.of((TypeToken<T>) type);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
