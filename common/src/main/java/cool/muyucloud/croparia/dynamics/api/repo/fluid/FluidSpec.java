package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class FluidSpec {
    public static final MapCodec<FluidSpec> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(fluid -> fluid.getFluid().arch$registryName()),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(fluid -> Optional.ofNullable(fluid.getNbt()))
    ).apply(instance, (id, nbt) -> new FluidSpec(BuiltInRegistries.FLUID.get(id), nbt.orElse(null))));
    public static final FluidSpec EMPTY = new FluidSpec(Fluids.EMPTY, null);

    private Fluid fluid;
    private CompoundTag nbt;

    public FluidSpec(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
        this.fluid = fluid;
        this.nbt = nbt;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setNbt(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public boolean isEmpty() {
        return this.getFluid() == Fluids.EMPTY;
    }
}
