package cool.muyucloud.croparia.dynamics.api.core.recipe.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.core.util.Util;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidSpec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidEntry {
    public static final MapCodec<FluidEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.optionalFieldOf("id").forGetter(FluidEntry::getId),
        TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(FluidEntry::getTag),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FluidEntry::getNbt),
        Codec.LONG.fieldOf("amount").forGetter(FluidEntry::getAmount),
        Codec.BOOL.fieldOf("elem_effect").forGetter(FluidEntry::canEffect)
    ).apply(instance, (id, tag, nbt, amount, elemEffect) -> new FluidEntry(id.orElse(null), tag.orElse(null), nbt.orElse(null), amount, elemEffect)));

    @Nullable
    private final ResourceLocation id;
    @Nullable
    private final TagKey<Fluid> tag;
    @Nullable
    private final CompoundTag nbt;
    private final long amount;
    private final boolean effect;

    public FluidEntry(@Nullable ResourceLocation id, @Nullable TagKey<Fluid> tag, @Nullable CompoundTag nbt, long amount, boolean elemEffect) {
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

    public Optional<TagKey<Fluid>> getTag() {
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

    public boolean match(FluidSpec fluid, long amount) {
        boolean fluidMatch;
        if (this.getId().isPresent()) {
            fluidMatch = this.getId().get().equals(fluid.getFluid().arch$registryName());
        } else if (this.getTag().isPresent()) {
            fluidMatch = Util.isIn(this.getTag().get(), fluid.getFluid());
        } else {
            return false;
        }
        boolean nbtMatch = fluid.getNbt().isEmpty() ? this.getNbt().isEmpty() : Util.matchNbt(this.getNbt().orElse(null), fluid.getNbt().orElse(null));
        boolean amountMatch = this.getAmount() <= amount;
        return fluidMatch && nbtMatch && amountMatch;
    }
}
