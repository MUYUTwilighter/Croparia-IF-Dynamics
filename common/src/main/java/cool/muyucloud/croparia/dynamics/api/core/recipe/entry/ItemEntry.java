package cool.muyucloud.croparia.dynamics.api.core.recipe.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.core.util.Util;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemSpec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class ItemEntry {
    public static final MapCodec<ItemEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.optionalFieldOf("id").forGetter(ItemEntry::getId),
        TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(ItemEntry::getTag),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemEntry::getNbt),
        Codec.LONG.fieldOf("amount").forGetter(ItemEntry::getAmount),
        Codec.BOOL.fieldOf("effect").forGetter(ItemEntry::canEffect)
    ).apply(instance, (id, tag, nbt, amount, elemEffect) -> new ItemEntry(id.orElse(null), tag.orElse(null), nbt.orElse(null), amount, elemEffect)));

    @Nullable
    private final ResourceLocation id;
    @Nullable
    private final TagKey<Item> tag;
    @Nullable
    private final CompoundTag nbt;
    private final long amount;
    private final boolean effect;

    public ItemEntry(@Nullable ResourceLocation id, @Nullable TagKey<Item> tag, @Nullable CompoundTag nbt, long amount, boolean elemEffect) {
        this.id = id;
        this.tag = tag;
        if (this.id == null && this.tag == null)
            throw new IllegalArgumentException("id and tag cannot be null at the same time");
        if (this.id != null && this.tag != null)
            throw new IllegalArgumentException("id and tag cannot be set at the same time");
        this.nbt = nbt;
        this.amount = amount;
        if (this.amount <= 0) throw new IllegalArgumentException("amount must be greater than 0");
        this.effect = elemEffect;
    }

    public Optional<ResourceLocation> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<TagKey<Item>> getTag() {
        return Optional.ofNullable(tag);
    }

    public Optional<CompoundTag> getNbt() {
        return Optional.ofNullable(nbt);
    }

    public long getAmount() {
        return amount;
    }

    /**
     * Whether to apply element effect on this fluid entry
     */
    public boolean canEffect() {
        return effect;
    }

    public boolean match(ItemSpec item, long amount) {
        boolean itemMatch = this.match(item);
        boolean nbtMatch = item.getNbt().isEmpty() ? this.getNbt().isEmpty() : Util.matchNbt(this.getNbt().orElse(null), item.getNbt().orElse(null));
        boolean amountMatch = this.getAmount() <= amount;
        return itemMatch && nbtMatch && amountMatch;
    }

    public boolean match(ItemSpec item) {
        if (this.getId().isPresent()) {
            return this.getId().get().equals(item.getItem().arch$registryName());
        } else if (this.getTag().isPresent()) {
            return Util.isIn(this.getTag().get(), item.getItem());
        } else {
            return false;
        }
    }
}
