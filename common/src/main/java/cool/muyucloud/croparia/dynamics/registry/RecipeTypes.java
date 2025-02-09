package cool.muyucloud.croparia.dynamics.registry;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.core.recipe.type.EfrType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;

@SuppressWarnings("unused")
public class RecipeTypes {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(CropariaIf.MOD_ID, Registries.RECIPE_TYPE);

    public static RegistrySupplier<EfrType> registerEfr(String id) {
        return RECIPE_TYPES.register(id, () -> new EfrType(CropariaIf.of(id), 1, 1, 1, 1, item -> true, fluid -> true));
    }
}
