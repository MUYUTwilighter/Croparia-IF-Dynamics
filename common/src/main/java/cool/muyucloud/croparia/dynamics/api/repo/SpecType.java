package cool.muyucloud.croparia.dynamics.api.repo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public record SpecType<T>(@NotNull ResourceLocation id, @NotNull T empty) {
    public static final Codec<SpecType<?>> CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
        Optional<SpecType<Object>> type = get(id);
        if (type.isEmpty()) {
            return DataResult.error(() -> "Undefined SpecType: %s".formatted(id));
        } else {
            return DataResult.success(type.get());
        }
    }, SpecType::id);

    private static final Map<ResourceLocation, SpecType<?>> REGISTRY = new HashMap<>();

    public static final SpecType<ItemSpec> ITEM_SPEC = register(CropariaIf.of("item_spec"), ItemSpec.EMPTY).orElseThrow();
    public static final SpecType<FluidSpec> FLUID_SPEC = register(CropariaIf.of("fluid_spec"), FluidSpec.EMPTY).orElseThrow();

    public static <T> Optional<SpecType<T>> register(ResourceLocation id, T empty) {
        if (REGISTRY.containsKey(id)) {
            return Optional.empty();
        }
        SpecType<T> type = new SpecType<>(id, empty);
        REGISTRY.put(id, type);
        return Optional.of(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<SpecType<T>> get(ResourceLocation id) {
        SpecType<?> type = REGISTRY.get(id);
        try {
            return Optional.of((SpecType<T>) type);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
