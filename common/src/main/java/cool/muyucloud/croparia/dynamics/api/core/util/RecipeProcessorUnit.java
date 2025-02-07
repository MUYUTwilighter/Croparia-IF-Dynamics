package cool.muyucloud.croparia.dynamics.api.core.util;

import com.google.gson.JsonObject;
import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.CrucibleBatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class RecipeProcessorUnit<F> {
    @NotNull
    private final transient FuelUnit<F> fuel;
    @NotNull
    private final transient CrucibleBatch crucible;
    private final transient @NotNull BiFunction<EfrContainer, Optional<ResourceLocation>, Optional<ElemForgeRecipe>> recipeProvider;
    @NotNull
    private final transient EfrContainer container;
    @Nullable
    private ElemForgeRecipe recipe = null;
    private float progress = 0F;

    public RecipeProcessorUnit(@NotNull BiFunction<EfrContainer, Optional<ResourceLocation>, Optional<ElemForgeRecipe>> recipeProvider, @NotNull CrucibleBatch crucible, @NotNull EfrContainer container, @NotNull FuelUnit<F> fuel) {
        this.recipeProvider = recipeProvider;
        this.crucible = crucible;
        this.container = container;
        this.fuel = fuel;
    }

    public void tick() {
        if ((recipe == null || container.shouldUpdateRecipe())) {
            recipeProvider.apply(container, Optional.empty()).ifPresentOrElse(newRecipe -> {
                if (recipe == null || !recipe.getId().equals(newRecipe.getId())) {
                    this.progress = 0F;
                }
                this.recipe = newRecipe;
            }, () -> {
                this.progress = 0F;
                this.recipe = null;
            });
        }
        if (recipe != null && fuel.isEnoughFor(crucible.getFuelEffect())) {
            fuel.burn(crucible.getFuelEffect());
            progress += crucible.getSpeedEffect();
            if (progress >= 1F) {
                progress = 0F;
                onComplete();
            }
        }
    }

    public void onComplete() {
        boolean itemEffect = crucible.getItemEffect() > Math.random() && crucible.getItemEffect() != 0;
        boolean fluidEffect = crucible.getFluidEffect() > Math.random() && crucible.getFluidEffect() != 0;
        assert recipe != null;
        recipe.getItemEntries().forEach(entry -> {
            if (!entry.canEffect() || !itemEffect) {
                container.consumeItem(entry);
            }
        });
        recipe.getFluidEntries().forEach(entry -> {
            if (!entry.canEffect() || !fluidEffect) {
                container.consumeFluid(entry);
            }
        });
        recipe.assemble(container);
    }

    public boolean isProcessing() {
        return this.recipe != null || fuel.isEnoughFor(crucible.getFuelEffect());
    }

    public void load(JsonObject json) {
        ResourceLocation recipeId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "recipe", "minecraft:missing"));
        recipeId = recipeId == null ? null : ResourceLocation.tryParse("minecraft:missing");
        this.recipe = recipeProvider.apply(container, Optional.ofNullable(recipeId)).orElse(null);
        this.progress = GsonHelper.getAsFloat(json, "progress", 0F);
    }

    public void save(JsonObject json) {
        json.addProperty("recipe", recipe == null ? "minecraft:missing" : recipe.getId().toString());
        json.addProperty("progress", progress);
    }
}
