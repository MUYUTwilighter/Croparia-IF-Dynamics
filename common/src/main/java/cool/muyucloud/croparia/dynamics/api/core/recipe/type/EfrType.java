package cool.muyucloud.croparia.dynamics.api.core.recipe.type;

import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public record EfrType(ResourceLocation id) implements RecipeType<ElemForgeRecipe> {
}
