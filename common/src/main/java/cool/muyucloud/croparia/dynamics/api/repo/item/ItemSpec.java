package cool.muyucloud.croparia.dynamics.api.repo.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class ItemSpec {
    public static final MapCodec<ItemSpec> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(item -> item.getItem().arch$registryName()),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(item -> Optional.ofNullable(item.getNbt()))
    ).apply(instance, (id, nbt) -> new ItemSpec(BuiltInRegistries.ITEM.get(id), nbt.orElse(null))));
    public static final ItemSpec EMPTY = new ItemSpec(Items.AIR, null);

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

    public boolean isEmpty() {
        return this.getItem() == Items.AIR;
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
