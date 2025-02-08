package cool.muyucloud.croparia.dynamics.api.core.block.entity;

import cool.muyucloud.croparia.dynamics.api.core.item.ElemCrucible;
import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.core.util.Constants;
import cool.muyucloud.croparia.dynamics.api.core.util.RecipeProcessor;
import cool.muyucloud.croparia.dynamics.api.core.util.RecipeProcessorUnit;
import cool.muyucloud.croparia.dynamics.api.elenet.ElenetPeerProvider;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.repo.RepoBatch;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.CrucibleBatch;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static cool.muyucloud.croparia.dynamics.api.core.block.ElemForgeBlock.RUNNING;

@SuppressWarnings("unused")
public abstract class ElemForgeBlockEntity<F> extends BlockEntity implements ElenetPeerProvider {
    private final RecipeProcessor<F> recipeProcessor = new RecipeProcessor<>();
    private final RecipeType<? extends ElemForgeRecipe> recipeType;
    private final CrucibleBatch crucibleBatch = new CrucibleBatch();
    private final ItemUnit crucibleSlot = new ItemUnit(item -> item.isOf(Constants.TAG_CRUCIBLES), 1);

    public ElemForgeBlockEntity(
        BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends ElemForgeRecipe> recipeType
    ) {
        super(blockEntityType, blockPos, blockState);
        this.recipeType = recipeType;
    }

    public abstract FuelUnit<F> getFuelUnit();

    protected RecipeType<? extends ElemForgeRecipe> getRecipeType() {
        return recipeType;
    }

    public CrucibleBatch getCrucibleBatch() {
        return crucibleBatch;
    }

    public RecipeProcessor<F> getRecipeProcessor() {
        return recipeProcessor;
    }

    public ItemUnit getCrucibleSlot() {
        return crucibleSlot;
    }

    public Optional<ElemCrucible> getCrucible() {
        return this.getCrucibleSlot().getResource().getItem() instanceof ElemCrucible crucible ? Optional.of(crucible) : Optional.empty();
    }

    public RecipeProcessorUnit<F> addRecipeProcessorUnit(
        @Nullable RepoBatch<ItemSpec> itemInputs, @Nullable RepoBatch<FluidSpec> fluidBatch,
        @Nullable RepoBatch<ItemSpec> itemOutputs, @Nullable RepoBatch<FluidSpec> fluidOutputs
    ) {
        EfrContainer container = EfrContainer.of(itemInputs, fluidBatch, itemOutputs, fluidOutputs);
        RecipeProcessorUnit<F> unit = new RecipeProcessorUnit<>(this.getRecipeType(), this.getCrucibleBatch(), container, this.getFuelUnit());
        this.getRecipeProcessor().add(unit);
        return unit;
    }

    protected void updateCrucible() {
        int tier = this.getCrucible().map(ElemCrucible::getTier).orElse(0);
        for (FluidUnit unit : crucibleBatch) {
            if (tier <= 0) {
                unit.setAcceptable(false);
                unit.setConsumable(false);
                continue;
            }
            if (unit.isAcceptable() && unit.isConsumable()) {
                tier--;
            }
        }
    }

    public void tick(MinecraftServer server) {
        this.updateCrucible();
        this.getRecipeProcessor().tick(server);
        if (this.getRecipeProcessor().isRunning()) {
            Objects.requireNonNull(this.getLevel()).setBlock(this.getBlockPos(), this.getBlockState().setValue(RUNNING, true), 3);
        } else {
            Objects.requireNonNull(this.getLevel()).setBlock(this.getBlockPos(), this.getBlockState().setValue(RUNNING, false), 3);
        }
    }
}
