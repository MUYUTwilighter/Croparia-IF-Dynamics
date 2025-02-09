package cool.muyucloud.croparia.dynamics.api.core.recipe.type;

import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.repo.RepoBatch;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemUnit;
import cool.muyucloud.croparia.dynamics.api.resource.type.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.resource.type.ItemSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Predicate;

public record EfrType(
    ResourceLocation id, int itemInputSize, int fluidInputSize, int itemOutputSize, int fluidOutputSize,
    Predicate<ItemSpec> itemFilter, Predicate<FluidSpec> fluidFilter
) implements RecipeType<ElemForgeRecipe> {
    public RepoBatch<ItemSpec> itemInputs() {
        return RepoBatch.of(ItemSpec.TYPE, ItemUnit.of(itemFilter, itemInputSize));
    }

    public RepoBatch<FluidSpec> fluidInputs() {
        return RepoBatch.of(FluidSpec.TYPE, FluidUnit.of(fluidFilter, fluidInputSize));
    }

    public RepoBatch<ItemSpec> itemOutputs() {
        return RepoBatch.of(ItemSpec.TYPE, ItemUnit.of(item -> true, itemOutputSize));
    }

    public RepoBatch<FluidSpec> fluidOutputs() {
        return RepoBatch.of(FluidSpec.TYPE, FluidUnit.of(fluid -> true, fluidOutputSize));
    }

    public EfrContainer container() {
        return EfrContainer.of(itemInputs(), fluidInputs(), itemOutputs(), fluidOutputs());
    }
}
