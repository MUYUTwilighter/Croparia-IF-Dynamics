package cool.muyucloud.croparia.dynamics.api.core.block.entity;

import cool.muyucloud.croparia.dynamics.api.core.block.ElemForgeBlock;
import cool.muyucloud.croparia.dynamics.api.core.item.ElemCrucible;
import cool.muyucloud.croparia.dynamics.api.core.item.ElenetAstrolabe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.input.EfrContainer;
import cool.muyucloud.croparia.dynamics.api.core.util.Constants;
import cool.muyucloud.croparia.dynamics.api.core.util.RecipeProcessor;
import cool.muyucloud.croparia.dynamics.api.core.util.RecipeProcessorUnit;
import cool.muyucloud.croparia.dynamics.api.elenet.ElenetPeer;
import cool.muyucloud.croparia.dynamics.api.elenet.ElenetPeerProvider;
import cool.muyucloud.croparia.dynamics.api.repo.ElenetRepo;
import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.repo.RepoBatch;
import cool.muyucloud.croparia.dynamics.api.repo.RepoUnit;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.CrucibleBatch;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemUnit;
import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public abstract class ElemForgeBlockEntity<F extends ResourceType> extends BlockEntity implements ElenetPeerProvider {
    private final RecipeProcessor<F> recipeProcessor = new RecipeProcessor<>();
    private final transient RecipeType<? extends ElemForgeRecipe> recipeType;
    private final CrucibleBatch crucibleBatch = new CrucibleBatch();
    private final ItemUnit crucibleSlot = new ItemUnit(item -> item.isOf(Constants.TAG_CRUCIBLES), 1);
    private final RepoBatch<ItemSpec> astrolabeBatch = RepoBatch.of(ItemSpec.TYPE);
    private final ElenetPeer peer = new ElenetPeer();
    private final transient Map<TypeToken<?>, ElenetRepo<?>> elenetRepos = new HashMap<>();

    public ElemForgeBlockEntity(
        BlockEntityType<?> beType, BlockPos pos, BlockState state, RecipeType<? extends ElemForgeRecipe> recipeType
    ) {
        super(beType, pos, state);
        this.recipeType = recipeType;
        this.registerElenetRepo(ElenetRepo.of(this::elenetConsumableItems, this::elenetAcceptableItems));
        this.registerElenetRepo(ElenetRepo.of(this::elenetConsumableFluids, this::elenetAcceptableFluids));
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.recipeProcessor.load(nbt.getList("recipe_processor", CompoundTag.TAG_COMPOUND));
        this.crucibleBatch.load(nbt.getCompound("crucible_batch"));
        this.crucibleSlot.load(nbt.getCompound("crucible_slot"));
        ListTag astrolabeBatchTag = nbt.getList("astrolabe_batch", CompoundTag.TAG_COMPOUND);
        this.astrolabeBatch.add(ItemUnit.of(item -> item.getItem() instanceof ElenetAstrolabe, 1, astrolabeBatchTag.size()));
        this.astrolabeBatch.load(astrolabeBatchTag);
        this.peer.load(nbt.getCompound("peer"));
        for (RepoUnit<ItemSpec> unit : this.astrolabeBatch) {
            if (unit.getResource().getItem() instanceof ElenetAstrolabe<?> astrolabe && !unit.isEmpty()) {
                this.getPeer().setRepo(this.getElenetRepo(astrolabe.getType()));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ListTag recipeProcessorTag = new ListTag();
        compoundTag.put("recipe_processor", recipeProcessorTag);
        this.recipeProcessor.save(recipeProcessorTag);
        CompoundTag crucibleBatchTag = new CompoundTag();
        compoundTag.put("crucible_batch", crucibleBatchTag);
        this.crucibleBatch.save(crucibleBatchTag);
        CompoundTag crucibleSlotTag = new CompoundTag();
        compoundTag.put("crucible_slot", crucibleSlotTag);
        this.crucibleSlot.save(crucibleSlotTag);
        ListTag astrolabeBatchTag = new ListTag();
        compoundTag.put("astrolabe_batch", astrolabeBatchTag);
        this.astrolabeBatch.save(astrolabeBatchTag);
        CompoundTag peerTag = new CompoundTag();
        compoundTag.put("peer", peerTag);
        this.peer.save(peerTag);
    }

    public void tick(MinecraftServer server) {
        this.updateCrucible();
        this.getRecipeProcessor().tick(server);
        if (this.getRecipeProcessor().isRunning()) {
            Objects.requireNonNull(this.getLevel()).setBlock(this.getBlockPos(), this.getBlockState().setValue(ElemForgeBlock.RUNNING, true), 3);
        } else {
            Objects.requireNonNull(this.getLevel()).setBlock(this.getBlockPos(), this.getBlockState().setValue(ElemForgeBlock.RUNNING, false), 3);
        }
    }

    public ElenetPeer getPeer() {
        return peer;
    }

    public void registerElenetRepo(ElenetRepo<?> elenetRepo) {
        this.elenetRepos.put(elenetRepo.getType(), elenetRepo);
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceType> ElenetRepo<T> getElenetRepo(TypeToken<T> token) {
        return (ElenetRepo<T>) this.elenetRepos.get(token);
    }

    public boolean isElenetEnabled(TypeToken<?> token) {
        for (RepoUnit<ItemSpec> unit : this.getCompassBatch()) {
            if (unit.getResource().getItem() instanceof ElenetAstrolabe<?> astrolabe) {
                if (astrolabe.getType() == token) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addCompass(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ElenetAstrolabe<?> astrolabe) {
            if (this.isElenetEnabled(astrolabe.getType())) {
                return false;
            }
            stack.shrink(1);
            ItemUnit unit = new ItemUnit(itemSpec -> itemSpec.getItem() instanceof ElenetAstrolabe, 1);
            unit.accept(ItemSpec.from(item), 1);
            this.getPeer().setRepo(this.getElenetRepo(astrolabe.getType()));
            return true;
        }
        return false;
    }

    public ItemStack removeCompass(TypeToken<?> token) {
        int i = 0;
        ItemStack result = ItemStack.EMPTY;
        for (RepoUnit<ItemSpec> unit : this.getCompassBatch()) {
            if (unit.getResource().getItem() instanceof ElenetAstrolabe<?> astrolabe) {
                if (astrolabe.getType() == token) {
                    ItemStack stack = unit.getResource().toStack(1);
                    break;
                }
            }
        }
        if (!result.isEmpty()) {
            this.getCompassBatch().remove(i);
        }
        return result;
    }

    public RepoBatch<ItemSpec> elenetAcceptableItems() {
        RepoBatch<ItemSpec> acceptable = new RepoBatch<>(ItemSpec.TYPE);
        if (!isElenetEnabled(ItemSpec.TYPE)) {
            return acceptable;
        }
        for (RecipeProcessorUnit<F> unit : this.getRecipeProcessor()) {
            RepoBatch<ItemSpec> inputs = unit.getContainer().getItemInputs();
            if (inputs != null) {
                acceptable.add(inputs);
            }
        }
        return acceptable;
    }

    public RepoBatch<ItemSpec> elenetConsumableItems() {
        RepoBatch<ItemSpec> consumable = new RepoBatch<>(ItemSpec.TYPE);
        if (!isElenetEnabled(ItemSpec.TYPE)) {
            return consumable;
        }
        for (RecipeProcessorUnit<F> unit : this.getRecipeProcessor()) {
            RepoBatch<ItemSpec> outputs = unit.getContainer().getItemOutputs();
            if (outputs != null) {
                consumable.add(outputs);
            }
        }
        return consumable;
    }

    public RepoBatch<FluidSpec> elenetAcceptableFluids() {
        RepoBatch<FluidSpec> acceptable = RepoBatch.of(FluidSpec.TYPE);
        if (!isElenetEnabled(FluidSpec.TYPE)) {
            return acceptable;
        }
        for (RecipeProcessorUnit<F> unit : this.getRecipeProcessor()) {
            RepoBatch<FluidSpec> inputs = unit.getContainer().getFluidInputs();
            if (inputs != null) {
                acceptable.add(inputs);
            }
        }
        for (FluidUnit unit : this.getCrucibleBatch()) {
            if (unit.isAcceptable()) {
                acceptable.add(unit);
            }
        }
        return acceptable;
    }

    public RepoBatch<FluidSpec> elenetConsumableFluids() {
        RepoBatch<FluidSpec> consumable = RepoBatch.of(FluidSpec.TYPE);
        if (!isElenetEnabled(FluidSpec.TYPE)) {
            return consumable;
        }
        for (RecipeProcessorUnit<F> unit : this.getRecipeProcessor()) {
            RepoBatch<FluidSpec> outputs = unit.getContainer().getFluidOutputs();
            if (outputs != null) {
                consumable.add(outputs);
            }
        }
        return consumable;
    }

    public abstract FuelUnit<F> getFuelUnit();

    public RepoBatch<ItemSpec> getCompassBatch() {
        return astrolabeBatch;
    }

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
}
