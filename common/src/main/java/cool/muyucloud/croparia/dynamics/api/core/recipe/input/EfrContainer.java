package cool.muyucloud.croparia.dynamics.api.core.recipe.input;

import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemResult;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidBatch;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemBatch;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EfrContainer implements Container {
    @Nullable
    private final ItemBatch itemInputs;
    @Nullable
    private final FluidBatch fluidInputs;
    @Nullable
    private final ItemBatch itemOutputs;
    @Nullable
    private final FluidBatch fluidOutputs;

    public EfrContainer(
        @Nullable ItemBatch itemInputs, @Nullable FluidBatch fluidInputs,
        @Nullable ItemBatch itemOutputs, @Nullable FluidBatch fluidOutputs
    ) {
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
    }

    public void consumeItem(ItemEntry entry) {
        if (itemInputs == null) return;
        long required = entry.getAmount();
        for (int i = 0; i < itemInputs.size(); i++) {
            ItemSpec resource = itemInputs.resourceFor(i);
            if (entry.match(resource)) {
                required -= itemInputs.consume(i, resource, required);
            }
            if (required <= 0) break;
        }
    }

    public void consumeFluid(FluidEntry entry) {
        if (fluidInputs == null) return;
        long required = entry.getAmount();
        for (int i = 0; i < fluidInputs.size(); i++) {
            FluidSpec resource = fluidInputs.resourceFor(i);
            if (entry.match(resource)) {
                required -= fluidInputs.consume(i, resource, required);
            }
            if (required <= 0) break;
        }
    }

    public boolean canConsumeItems(Collection<ItemEntry> items) {
        if (this.itemInputs == null || this.itemInputs.size() < items.size()) return false;
        Map<Integer, Long> remains = new HashMap<>();
        for (ItemEntry entry : items) {
            long required = entry.getAmount();
            for (int i = 0; i < this.itemInputs.size(); i++) {
                if (remains.getOrDefault(i, 1L) <= 0) continue;
                ItemSpec resource = this.itemInputs.resourceFor(i);
                if (entry.match(resource)) {
                    long remain = remains.computeIfAbsent(i, k -> itemInputs.amountFor(k, resource));
                    long consumed = itemInputs.simConsume(i, resource, Math.min(required, remain));
                    remain -= consumed;
                    required -= consumed;
                    remains.put(i, remain);
                }
                if (required <= 0) break;
            }
            if (required > 0) return false;
        }
        return true;
    }

    public boolean canConsumeFluids(Collection<FluidEntry> fluids) {
        if (this.fluidInputs == null || this.fluidInputs.size() < fluids.size()) return false;
        Map<Integer, Long> remains = new HashMap<>();
        for (FluidEntry entry : fluids) {
            long required = entry.getAmount();
            for (int i = 0; i < this.fluidInputs.size(); i++) {
                if (remains.getOrDefault(i, 1L) <= 0) continue;
                FluidSpec resource = this.fluidInputs.resourceFor(i);
                if (entry.match(resource)) {
                    long remain = remains.computeIfAbsent(i, k -> fluidInputs.amountFor(k, resource));
                    long consumed = fluidInputs.simConsume(i, resource, Math.min(required, remain));
                    remain -= consumed;
                    required -= consumed;
                    remains.put(i, remain);
                }
                if (required <= 0) break;
            }
            if (required > 0) return false;
        }
        return true;
    }

    public void acceptItems(Collection<ItemResult> items) {
        if (this.itemOutputs == null) return;
        for (ItemResult result : items) {
            this.itemOutputs.accept(result.getItemSpec(), result.getAmount());
        }
    }

    public void acceptFluids(Collection<FluidResult> fluids) {
        if (this.fluidOutputs == null) return;
        for (FluidResult result : fluids) {
            this.fluidOutputs.accept(result.getFluidSpec(), result.getAmount());
        }
    }

    public boolean canAcceptItems(Collection<ItemResult> items) {
        if (this.itemOutputs == null || this.itemOutputs.size() < items.size()) return false;
        Map<Integer, Long> occupations = new HashMap<>();
        for (ItemResult result : items) {
            long required = result.getAmount();
            for (int i = 0; i < this.itemOutputs.size(); i++) {
                long accepted = this.itemOutputs.simAccept(i, result.getItemSpec(), occupations.computeIfAbsent(i, k -> 0L) + required);
                long occupied = occupations.computeIfAbsent(i, k -> 0L);
                required -= Math.max(0, accepted - occupied);
                if (required <= 0) break;
            }
            if (required > 0) return false;
        }
        return true;
    }

    public boolean canAcceptFluids(Collection<FluidResult> fluids) {
        if (this.fluidOutputs == null || this.fluidOutputs.size() < fluids.size()) return false;
        Map<Integer, Long> occupations = new HashMap<>();
        for (FluidResult result : fluids) {
            long required = result.getAmount();
            for (int i = 0; i < this.fluidOutputs.size(); i++) {
                long accepted = this.fluidOutputs.simAccept(i, result.getFluidSpec(), occupations.computeIfAbsent(i, k -> 0L) + required);
                long occupied = occupations.computeIfAbsent(i, k -> 0L);
                required -= Math.max(0, accepted - occupied);
                if (required <= 0) break;
            }
            if (required > 0) return false;
        }
        return true;
    }

    public boolean shouldUpdateRecipe() {
        if (itemInputs != null && itemInputs.isEmpty() && fluidInputs != null && fluidInputs.isEmpty()) return false;
        return itemInputs != null && itemInputs.shouldUpdateRecipe()
            || fluidInputs != null && fluidInputs.shouldUpdateRecipe()
            || itemOutputs != null && itemOutputs.shouldUpdateRecipe()
            || fluidOutputs != null && fluidOutputs.shouldUpdateRecipe();
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return (itemInputs == null || itemInputs.isEmpty()) && (fluidInputs == null || fluidInputs.isEmpty());
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }
}
