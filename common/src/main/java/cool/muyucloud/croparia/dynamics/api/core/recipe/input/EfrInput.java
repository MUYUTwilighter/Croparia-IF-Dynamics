package cool.muyucloud.croparia.dynamics.api.core.recipe.input;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;

public class EfrInput implements Container {
    private final Iterable<Map.Entry<ItemSpec, Long>> items;
    private final Iterable<Map.Entry<FluidSpec, Long>> fluids;

    public EfrInput(Iterable<Map.Entry<ItemSpec, Long>> itemView, Iterable<Map.Entry<FluidSpec, Long>> fluidView) {
        this.items = itemView;
        this.fluids = fluidView;
    }

    public void visitItems(BiFunction<ItemSpec, Long, Boolean> visitor) {
        for (Map.Entry<ItemSpec, Long> entry : items) {
            if (!visitor.apply(entry.getKey(), entry.getValue())) {
                break;
            }
        }
    }

    public void visitFluids(BiFunction<FluidSpec, Long, Boolean> visitor) {
        for (Map.Entry<FluidSpec, Long> entry : fluids) {
            if (!visitor.apply(entry.getKey(), entry.getValue())) {
                break;
            }
        }
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
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
