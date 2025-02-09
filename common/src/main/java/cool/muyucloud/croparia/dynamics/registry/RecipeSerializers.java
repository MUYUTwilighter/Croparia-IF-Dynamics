package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.recipe.serializer.EfrSerializer;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class RecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(CropariaIf.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<EfrSerializer> TEST = register("test", () -> EfrSerializer.INSTANCE);

    public static <T extends RecipeSerializer<?>> RegistrySupplier<T> register(String id, Supplier<T> supplier) {
        return SERIALIZERS.register(id, supplier);
    }
}
