package cool.muyucloud.croparia.dynamics.api.repo.item.forge;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemAgent;
import cool.muyucloud.croparia.dynamics.api.resource.type.ItemSpec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ItemAgentImpl extends ItemAgent implements IItemHandler {
    public static ItemAgent of(Supplier<Repo<ItemSpec>> repo) {
        return new ItemAgentImpl(repo);
    }

    public ItemAgentImpl(Supplier<Repo<ItemSpec>> repo) {
        super(repo);
    }

    @Override
    public int getSlots() {
        return this.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return this.resourceFor(i).toStack();
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack input, boolean simulate) {
        ItemSpec item = this.resourceFor(i);
        ItemStack result = item.toStack();
        long accepted;
        if (simulate) {
            accepted = this.simConsume(i, item, input.getCount());
        } else {
            accepted = this.consume(i, item, input.getCount());
        }
        result.setCount((int) accepted);
        return result;
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int amount, boolean simulate) {
        ItemSpec item = this.resourceFor(i);
        ItemStack result = item.toStack();
        long consumed;
        if (simulate) {
            consumed = this.simConsume(i, item, amount);
        } else {
            consumed = this.consume(i, item, amount);
        }
        result.setCount((int) consumed);
        return result;
    }

    @Override
    public int getSlotLimit(int i) {
        ItemSpec item = this.resourceFor(i);
        return (int) this.capacityFor(i, item);
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack input) {
        return this.simAccept(i, ItemSpec.from(input), input.getCount()) >= input.getCount();
    }
}
