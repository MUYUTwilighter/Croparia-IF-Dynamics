package cool.muyucloud.croparia.dynamics.api.repo.item.forge;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import cool.muyucloud.croparia.dynamics.api.repo.item.PlatformItemAgent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;

public class ForgeItemAgent implements PlatformItemAgent {
    public static ForgeItemAgent of(IItemHandler handler) {
        return new ForgeItemAgent(handler);
    }

    private final IItemHandler handler;

    public ForgeItemAgent(IItemHandler handler) {
        this.handler = handler;
    }

    public IItemHandler get() {
        return this.handler;
    }

    @Override
    public Optional<ItemRepo> peel() {
        return this.get() instanceof ItemRepo repo ? Optional.of(repo) : Optional.empty();
    }

    @Override
    public int size() {
        return this.get().getSlots();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.get().getStackInSlot(i).isEmpty();
    }

    @Override
    public long simConsume(int i, ItemSpec item, long amount) {
        ItemStack stored = this.get().getStackInSlot(i);
        if (item.matches(stored)) {
            return this.get().extractItem(i, (int) Math.min(amount, stored.getCount()), true).getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long consume(int i, ItemSpec item, long amount) {
        ItemStack stored = this.get().getStackInSlot(i);
        if (item.matches(stored)) {
            return this.get().extractItem(i, (int) Math.min(amount, stored.getCount()), false).getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long simAccept(int i, ItemSpec item, long amount) {
        return this.get().insertItem(i, item.toStack(amount), true).getCount();
    }

    @Override
    public long accept(int i, ItemSpec item, long amount) {
        return this.get().insertItem(i, item.toStack(amount), false).getCount();
    }

    @Override
    public long capacityFor(int i, ItemSpec item) {
        ItemStack stored = this.get().getStackInSlot(i);
        if ((stored.isEmpty() && this.get().insertItem(i, item.toStack(1), true).getCount() == 1)
            || item.matches(stored)) {
            return this.get().getSlotLimit(i);
        } else {
            return 0;
        }
    }

    @Override
    public long amountFor(int i, ItemSpec item) {
        ItemStack stored = this.get().getStackInSlot(i);
        if (item.matches(stored)) {
            return stored.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public ItemSpec resourceFor(int i) {
        return ItemSpec.from(this.get().getStackInSlot(i));
    }
}
