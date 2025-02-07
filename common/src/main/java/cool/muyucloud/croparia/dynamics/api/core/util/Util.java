package cool.muyucloud.croparia.dynamics.api.core.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Stack;

public class Util {
    @SuppressWarnings("unchecked")
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

    public static boolean matchNbt(@Nullable CompoundTag primary, @Nullable CompoundTag secondary) {
        if (primary == null) return true;
        if (secondary == null) return false;
        Stack<CompoundTag> primaryStack = new Stack<>();
        Stack<CompoundTag> secondaryStack = new Stack<>();
        primaryStack.push(primary);
        secondaryStack.push(secondary);
        while (!primaryStack.isEmpty() && !secondaryStack.isEmpty()) {
            CompoundTag primaryTag = primaryStack.pop();
            CompoundTag secondaryTag = secondaryStack.pop();
            for (String key : primaryTag.getAllKeys()) {
                if (!secondaryTag.contains(key)) {
                    return false;
                }
                Tag primarySub = primaryTag.get(key);
                Tag secondarySub = secondaryTag.get(key);
                assert primarySub != null && secondarySub != null;
                if (primarySub instanceof CompoundTag) {
                    if (!(secondarySub instanceof CompoundTag)) {
                        return false;
                    }
                    primaryStack.push((CompoundTag) primarySub);
                    secondaryStack.push((CompoundTag) secondarySub);
                    continue;
                }
                if (!primarySub.equals(secondarySub)) {
                    return false;
                }
            }
        }
        return primaryStack.isEmpty();
    }

    public static CompoundTag mergeNbt(@NotNull CompoundTag primary, @NotNull CompoundTag secondary) {
        CompoundTag result = primary.copy();
        Stack<CompoundTag> primaryStack = new Stack<>();
        Stack<CompoundTag> secondaryStack = new Stack<>();
        primaryStack.push(result);
        secondaryStack.push(secondary);
        while (!primaryStack.isEmpty() && !secondaryStack.isEmpty()) {
            CompoundTag primaryTag = primaryStack.pop();
            CompoundTag secondaryTag = secondaryStack.pop();
            for (String key : secondaryTag.getAllKeys()) {
                Tag primarySub = primaryTag.get(key);
                Tag secondarySub = secondaryTag.get(key);
                assert secondarySub != null;
                if (primarySub == null) {
                    result.put(key, secondarySub);
                }
                if (primarySub instanceof CompoundTag && secondarySub instanceof CompoundTag) {
                    primaryStack.push((CompoundTag) primarySub);
                    secondaryStack.push((CompoundTag) secondarySub);
                    continue;
                }
                result.put(key, secondarySub);
            }
        }
        return result;
    }
}
