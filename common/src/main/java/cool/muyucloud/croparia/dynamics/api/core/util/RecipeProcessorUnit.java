package cool.muyucloud.croparia.dynamics.api.core.util;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import cool.muyucloud.croparia.dynamics.annotation.ServerOnly;
import cool.muyucloud.croparia.dynamics.api.core.ServerProvider;
import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.CrucibleBatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class RecipeProcessorUnit<F> {
    public static <F> RecipeProcessorUnit<F> of(
        RecipeType<? extends ElemForgeRecipe> recipeType, CrucibleBatch crucible, EfrContainer container, FuelUnit<F> fuel
    ) {
        return new RecipeProcessorUnit<>(recipeType, crucible, container, fuel);
    }

    @NotNull
    private final transient FuelUnit<F> fuel;
    @NotNull
    private final transient CrucibleBatch crucible;
    private final transient @NotNull RecipeType<? extends ElemForgeRecipe> recipeType;
    @NotNull
    private final transient EfrContainer container;
    @Nullable
    private ElemForgeRecipe recipe = null;
    private float progress = 0F;

    public RecipeProcessorUnit(
        @NotNull RecipeType<? extends ElemForgeRecipe> recipeType, @NotNull CrucibleBatch crucible,
        @NotNull EfrContainer container, @NotNull FuelUnit<F> fuel
    ) {
        this.recipeType = recipeType;
        this.crucible = crucible;
        this.container = container;
        this.fuel = fuel;
    }

    public @NotNull FuelUnit<F> getFuel() {
        return fuel;
    }

    public @NotNull CrucibleBatch getCrucible() {
        return crucible;
    }

    public @NotNull EfrContainer getContainer() {
        return container;
    }

    public float getProgress() {
        return progress;
    }

    public void tick(MinecraftServer server) {
        if ((recipe == null || container.shouldUpdateRecipe())) {
            this.findRecipe(server, null).ifPresentOrElse(newRecipe -> {
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
        boolean itemEffect = crucible.canAffectItem();
        boolean fluidEffect = crucible.canAffectFluid();
        assert recipe != null;
        AtomicBoolean itemAffected = new AtomicBoolean(false);
        AtomicBoolean fluidAffected = new AtomicBoolean(false);
        recipe.getItemEntries().forEach(entry -> {
            if (!entry.canEffect() || !itemEffect) {
                container.consumeItem(entry);
            } else {
                itemAffected.set(true);
            }
        });
        recipe.getFluidEntries().forEach(entry -> {
            if (!entry.canEffect() || !fluidEffect) {
                container.consumeFluid(entry);
            } else {
                fluidAffected.set(true);
            }
        });
        if (itemAffected.get()) {
            crucible.onItemAffected();
        }
        if (fluidAffected.get()) {
            crucible.onFluidAffected();
        }
        recipe.assemble(container);
    }

    public boolean isRunning() {
        return this.recipe != null || fuel.isEnoughFor(crucible.getFuelEffect());
    }

    @ServerOnly
    public void load(JsonObject json) {
        ResourceLocation recipeId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "recipe", "minecraft:missing"));
        recipeId = recipeId == null ? null : ResourceLocation.tryParse("minecraft:missing");
        this.recipe = this.findRecipe(ServerProvider.getOrThrow(), recipeId).orElse(null);
        this.progress = GsonHelper.getAsFloat(json, "progress", 0F);
    }

    public void save(JsonObject json) {
        json.addProperty("recipe", recipe == null ? "minecraft:missing" : recipe.getId().toString());
        json.addProperty("progress", progress);
    }

    public Optional<? extends ElemForgeRecipe> findRecipe(MinecraftServer server, @Nullable ResourceLocation id) {
        return server.getRecipeManager().getRecipeFor(recipeType, container, null, id).map(Pair::getSecond);
    }
}
