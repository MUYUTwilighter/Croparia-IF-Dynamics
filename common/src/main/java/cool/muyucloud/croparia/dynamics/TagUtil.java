package cool.muyucloud.croparia.dynamics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TagUtil {
    public static <T> boolean isIn(@NotNull TagKey<T> tagKey, @NotNull T entry) {
        Optional<? extends Registry<?>> maybeRegistry = BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().location());
        if (maybeRegistry.isPresent()) {
            if (tagKey.isFor(maybeRegistry.get().key())) {
                Registry<T> registry = (Registry<T>) maybeRegistry.get();
                Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);
                // Check synced tag
                if (maybeKey.isPresent()) {
                    return registry.getHolderOrThrow(maybeKey.get()).is(tagKey);
                }
            }
        }

        return false;
    }
}
