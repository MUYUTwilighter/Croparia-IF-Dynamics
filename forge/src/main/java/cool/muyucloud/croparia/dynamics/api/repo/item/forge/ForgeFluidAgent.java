package cool.muyucloud.croparia.dynamics.api.repo.item.forge;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ForgeFluidAgent implements ItemRepo {
    public static ForgeFluidAgent of(IItemHandler handler) {
        return new ForgeFluidAgent(handler);
    }

    private final IItemHandler handler;

    public ForgeFluidAgent(IItemHandler handler) {
        this.handler = handler;
    }

    public IItemHandler get() {
        return this.handler;
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
    public boolean canConsume(int i, ItemSpec item, long amount) {
        ItemStack stored = this.get().getStackInSlot(i);
        if (item.matches(stored) || stored.isEmpty()) {
            return this.get().extractItem(i, (int) Math.min(amount, stored.getCount()), true).getCount() >= amount;
        } else {
            return false;
        }
    }

    @Override
    public boolean canAccept(int i, ItemSpec item, long amount) {
        return this.get().insertItem(i, item.toStack(amount), true).getCount() >= amount;
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
    public long accept(int i, ItemSpec item, long amount) {
        return this.get().insertItem(i, item.toStack(amount), false).getCount();
    }

    @Override
    public long spaceFor(int i, ItemSpec item) {
        return this.get().insertItem(i, item.toStack(Integer.MAX_VALUE), true).getCount();
    }

    @Override
    public long capacityFor(ItemSpec item) {
        return ItemRepo.super.capacityFor(item);
    }

    @Override
    public long capacityFor(int i, ItemSpec item) {
        ItemStack stored = this.get().getStackInSlot(i);
        if (item.matches(stored) || stored.isEmpty()) {
            return stored.getMaxStackSize();
        } else {
            return 0;
        }
    }

    @Override
    public long amountFor(ItemSpec item) {
        return ItemRepo.super.amountFor(item);
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
    public ItemSpec itemFor(int i) {
        return ItemSpec.from(this.get().getStackInSlot(i));
    }
}
