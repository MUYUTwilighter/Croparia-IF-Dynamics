package cool.muyucloud.croparia.dynamics.api.repo.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ItemSpec {
    private Item item;
    private CompoundTag nbt;

    public static ItemSpec from(ItemStack stack) {
        return new ItemSpec(stack.getItem(), stack.getTag());
    }

    public ItemSpec(@NotNull Item item, @Nullable CompoundTag nbt) {
        this.item = item;
        this.nbt = nbt;
    }

    public Item getItem() {
        return item;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setNbt(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public ItemStack toStack(long amount) {
        ItemStack stack = new ItemStack(this.getItem());
        stack.setTag(this.getNbt());
        stack.setCount((int) Math.min(Integer.MAX_VALUE, amount));
        return stack;
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameTags(stack, this.toStack());
    }
}
