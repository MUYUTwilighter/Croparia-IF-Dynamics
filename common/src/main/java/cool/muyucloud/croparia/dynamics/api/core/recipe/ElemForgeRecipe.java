package cool.muyucloud.croparia.dynamics.api.core.recipe;

import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.core.recipe.serializer.EfrSerializer;
import cool.muyucloud.croparia.dynamics.api.core.recipe.type.EfrType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class ElemForgeRecipe implements Recipe<EfrContainer> {
    @NotNull
    private final ResourceLocation id;
    @NotNull
    private final EfrType type;
    @NotNull
    private final RecipeSerializer<ElemForgeRecipe> serializer;
    @NotNull
    private final List<ItemEntry> itemEntries;
    @NotNull
    private final List<FluidEntry> fluidEntries;
    @NotNull
    private final List<ItemResult> itemResults;
    @NotNull
    private final List<FluidResult> fluidResults;
    private final int duration;
    private final int fuel;

    public ElemForgeRecipe(
        @NotNull ResourceLocation id, @NotNull EfrType type, @NotNull EfrSerializer serializer,
        @NotNull List<ItemEntry> itemEntries, @NotNull List<FluidEntry> fluidEntries, @NotNull List<ItemResult> itemResults, @NotNull List<FluidResult> fluidResults,
        int duration, int fuel
    ) {
        this.id = id;
        this.type = type;
        this.serializer = serializer;
        this.itemEntries = itemEntries;
        this.fluidEntries = fluidEntries;
        this.itemResults = itemResults;
        this.fluidResults = fluidResults;
        this.duration = duration;
        this.fuel = fuel;
    }

    public @NotNull List<ItemEntry> getItemEntries() {
        return itemEntries;
    }

    public @NotNull List<FluidEntry> getFluidEntries() {
        return fluidEntries;
    }

    public @NotNull List<ItemResult> getItemResults() {
        return itemResults;
    }

    public @NotNull List<FluidResult> getFluidResults() {
        return fluidResults;
    }

    public int getFuel() {
        return fuel;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(EfrContainer input, Level level) {
        return input.canConsumeItems(this.itemEntries) && input.canConsumeFluids(this.fluidEntries)
            && input.canAcceptItems(this.itemResults) && input.canAcceptFluids(this.fluidResults);
    }

    public void assemble(EfrContainer container) {
        container.acceptItems(this.itemResults);
        container.acceptFluids(this.fluidResults);
    }

    @Override
    public @NotNull ItemStack assemble(EfrContainer container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public @NotNull EfrType getType() {
        return this.type;
    }
}
