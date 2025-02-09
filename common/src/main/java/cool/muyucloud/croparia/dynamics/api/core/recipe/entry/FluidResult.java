package cool.muyucloud.croparia.dynamics.api.core.recipe.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cool.muyucloud.croparia.dynamics.api.resource.type.FluidSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidResult {
    public static final MapCodec<FluidResult> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(FluidResult::getId),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FluidResult::getNbt),
        Codec.LONG.optionalFieldOf("amount").forGetter(result -> Optional.of(result.getAmount()))
    ).apply(instance, (id, nbt, amount) -> new FluidResult(id, nbt.orElse(null), amount.orElse(20250L))));

    @NotNull
    private final ResourceLocation id;
    @Nullable
    private final CompoundTag nbt;
    private final long amount;
    @NotNull
    private final transient FluidSpec fluidSpec;

    public FluidResult(@NotNull ResourceLocation id, @Nullable CompoundTag nbt, long amount) {
        this.id = id;
        this.nbt = nbt;
        this.amount = amount;
        if (this.amount <= 0) throw new IllegalArgumentException("amount must be greater than 0");
        this.fluidSpec = new FluidSpec(BuiltInRegistries.FLUID.get(id), nbt);
        if (this.fluidSpec.isEmpty()) throw new IllegalArgumentException("fluid cannot be empty");
    }

    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    public Optional<CompoundTag> getNbt() {
        return Optional.ofNullable(nbt);
    }

    public long getAmount() {
        return amount;
    }

    public @NotNull FluidSpec getFluidSpec() {
        return fluidSpec;
    }
}
