package cool.muyucloud.croparia.dynamics.api.repo.item.forge;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemAgent;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ItemAgentImpl extends ItemAgent implements IItemHandler {
    public static ItemAgent of(Supplier<ItemRepo> repo) {
        return new ItemAgentImpl(repo);
    }

    public ItemAgentImpl(Supplier<ItemRepo> repo) {
        super(repo);
    }

    @Override
    public int getSlots() {
        return this.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return this.itemFor(i).toStack();
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack input, boolean simulate) {
        ItemSpec item = ItemSpec.from(input);
        ItemStack result = item.toStack();
        long amount = Math.min(this.spaceFor(i, item), input.getCount());
        if (simulate) {
            amount = this.canAccept(i, item, amount) ? amount : 0;
        } else {
            amount = this.accept(i, item, amount);
        }
        result.setCount((int) amount);
        return result;
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int amount, boolean simulate) {
        ItemSpec item = this.itemFor(i);
        ItemStack result = item.toStack();
        long consumed = Math.min(this.amountFor(item), amount);
        if (simulate) {
            consumed = this.canConsume(i, item, consumed) ? consumed : 0;
        } else {
            consumed = this.consume(i, item, consumed);
        }
        result.setCount((int) consumed);
        return result;
    }

    @Override
    public int getSlotLimit(int i) {
        ItemSpec item = this.itemFor(i);
        return (int) this.capacityFor(i, item);
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack input) {
        return this.canAccept(i, ItemSpec.from(input), input.getCount());
    }
}
