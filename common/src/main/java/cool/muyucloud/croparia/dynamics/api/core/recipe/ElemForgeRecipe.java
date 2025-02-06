package cool.muyucloud.croparia.dynamics.api.core.recipe;

import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrInput;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ElemForgeRecipe implements Recipe<EfrInput> {
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

    public ElemForgeRecipe(
        @NotNull ResourceLocation id, @NotNull EfrType type, @NotNull EfrSerializer serializer,
        @NotNull List<ItemEntry> itemEntries, @NotNull List<FluidEntry> fluidEntries, @NotNull List<ItemResult> itemResults, @NotNull List<FluidResult> fluidResults,
        int duration
    ) {
        this.id = id;
        this.type = type;
        this.serializer = serializer;
        this.itemEntries = itemEntries;
        this.fluidEntries = fluidEntries;
        this.itemResults = itemResults;
        this.fluidResults = fluidResults;
        this.duration = duration;
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

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(EfrInput input, Level level) {
        if (!this.itemEntries.isEmpty()) {
            for (ItemEntry entry : this.itemEntries) {
                AtomicLong required = new AtomicLong(entry.getAmount());
                input.visitItems((item, amount) -> {
                    if (entry.match(item, amount)) {
                        required.set(required.get() - amount);
                    }
                    return required.get() > 0;
                });
                if (required.get() > 0) {
                    return false;
                }
            }
        }
        if (!this.fluidEntries.isEmpty()) {
            for (FluidEntry entry : this.fluidEntries) {
                AtomicLong required = new AtomicLong(entry.getAmount());
                input.visitFluids((fluid, amount) -> {
                    if (entry.match(fluid, amount)) {
                        required.set(required.get() - amount);
                    }
                    return required.get() > 0;
                });
                if (required.get() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Perform the recipe crafting.
     *
     * @param input                The recipe input
     * @param itemConsumeCallback  The callback for consuming items
     * @param fluidConsumeCallback The callback for consuming fluids
     * @param itemAcceptCallback   The callback that provides the item result
     * @param fluidAcceptCallback  The callback that provides the fluid result
     */
    public void craft(
        EfrInput input, Consumer<ItemEntry> itemConsumeCallback, Consumer<FluidEntry> fluidConsumeCallback,
        Consumer<ItemResult> itemAcceptCallback, Consumer<FluidResult> fluidAcceptCallback
    ) {
        if (!this.itemEntries.isEmpty()) {
            for (ItemEntry entry : this.itemEntries) {
                itemConsumeCallback.accept(entry);
            }
        }
        if (!this.fluidEntries.isEmpty()) {
            for (FluidEntry entry : this.fluidEntries) {
                fluidConsumeCallback.accept(entry);
            }
        }
        for (ItemResult result : this.itemResults) {
            itemAcceptCallback.accept(result);
        }
        for (FluidResult result : this.fluidResults) {
            fluidAcceptCallback.accept(result);
        }
    }

    @Override
    public @NotNull ItemStack assemble(EfrInput container, RegistryAccess registryAccess) {
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
